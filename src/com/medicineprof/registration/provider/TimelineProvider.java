package com.medicineprof.registration.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import com.medicineprof.registration.provider.TimelineProviderContract.TimelineTable;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class TimelineProvider extends ContentProvider {

	public static final String TAG = "TimelineProvider";

	// "projection" map of all the timeline table columns
	private static HashMap<String, String> TimelineProjectionMap;
	// URI matcher ID for the main timeline URI pattern
	private static final int MATCHER_TIMELINE = 1;
	// URI matcher ID for the single tweet ID pattern
	private static final int MATCHER_TWEET = 2;
	// URI matcher for validating URIs
	private static final UriMatcher uriMatcher;
	// Handle to our ProviderDbHelper.
	private ProviderDbHelper dbHelper;
	
	/**
	 * The MIME type of a timeline
	 */
	private static final String TIMELINE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.restfulandroid.timeline";

	/**
	 * The MIME type of a single tweet from the timeline
	 */
	private static final String TIMELINE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.restfulandroid.timeline";
	
	// static 'setup' block
	static {
		// Build up URI matcher
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		// Add a pattern to route URIs terminated with just "rageComics"
		uriMatcher.addURI(TimelineProviderContract.AUTHORITY, TimelineTable.TABLE_NAME, MATCHER_TIMELINE);
		// Add a pattern to route URIs terminated with comic IDs
		uriMatcher.addURI(TimelineProviderContract.AUTHORITY, TimelineTable.TABLE_NAME + "/#", MATCHER_TWEET);

		// Create and initialize a projection map that returns all columns,
		// This map returns a column name for a given string. The two are usually equal, but we need this structure
		// later, down in .query()
		TimelineProjectionMap = new HashMap<String, String>();
		for (String column : TimelineTable.ALL_COLUMNS) {
			TimelineProjectionMap.put(column, column);
		}
	}

	@Override
	public boolean onCreate() {
		this.dbHelper = new ProviderDbHelper(this.getContext());
		return true; // if there are any issues, they'll be reported as exceptions
	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereValues) {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		String finalWhere;
		int deletedRowsCount;

		// Perform the delete based on URI pattern
		db.beginTransaction();
		try {
			switch (uriMatcher.match(uri)) {
			case MATCHER_TIMELINE:
				// Delete all the rage comics matching the where column/value pairs
				deletedRowsCount = db.delete(TimelineTable.TABLE_NAME, whereClause, whereValues);
				break;

			case MATCHER_TWEET:
				//Delete the comic with the given ID
				String tweetId = uri.getPathSegments().get(TimelineTable.TWEET_ID_PATH_POSITION);
				finalWhere = TimelineTable._ID + " = " + tweetId;
				if (whereClause != null) {
					finalWhere = finalWhere + " AND " + whereClause;
				}

				// Perform the delete.
				deletedRowsCount = db.delete(TimelineTable.TABLE_NAME, finalWhere, whereValues);
				break;

			// If the incoming URI is invalid, throws an exception.
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} finally {
			db.endTransaction();
		}

		// Notify observers of the the change
		getContext().getContentResolver().notifyChange(uri, null);

		// Returns the number of rows deleted.
		return deletedRowsCount;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the incoming URI.
		if (uriMatcher.match(uri) != MATCHER_TIMELINE) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			throw new SQLException("ContentValues arg for .insert() is null, cannot insert row.");
		}

		long newRowId = this.dbHelper.getWritableDatabase().insert(TimelineTable.TABLE_NAME, null, values);

		if (newRowId > 0) { // if rowID is -1, it means the insert failed
			// Build a new Timeline URI with the new tweet's ID appended to it.
			Uri tweetUri = ContentUris.withAppendedId(TimelineTable.CONTENT_ID_URI_BASE, newRowId);
			// Notify observers that our data changed.
			getContext().getContentResolver().notifyChange(tweetUri, null);
			return tweetUri;
		}

		throw new SQLException("Failed to insert row into " + uri); // Insert failed: halt and catch fire.
	}

	@Override
	public Cursor query(Uri uri, String[] selectedColumns, String whereClause, String[] whereValues, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TimelineTable.TABLE_NAME);

		// Choose the projection and adjust the "where" clause based on URI pattern-matching.
		switch (uriMatcher.match(uri)) {
		case MATCHER_TIMELINE:
			qb.setProjectionMap(TimelineProjectionMap);
			break;

		// asking for a single comic - use the rage comics projection, but add a where clause to only return the one
		// comic
		case MATCHER_TWEET:
			qb.setProjectionMap(TimelineProjectionMap);
			// Find the comic ID itself in the incoming URI
			String id = uri.getPathSegments().get(TimelineTable.TWEET_ID_PATH_POSITION);
			qb.appendWhere(TimelineTable._ID + "=" + id);
			break;

		default:
			// If the URI doesn't match any of the known patterns, throw an exception.
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		// the two nulls here are 'grouping' and 'filtering by group'
		Cursor cursor = qb.query(db, selectedColumns, whereClause, whereValues, null, null, sortOrder);

		// Tell the Cursor about the URI to watch, so it knows when its source data changes
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues updateValues, String whereClause, String[] whereValues) {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		int updatedRowsCount;
		String finalWhere;

		db.beginTransaction();
		// Perform the update based on the incoming URI's pattern
		try {
			switch (uriMatcher.match(uri)) {

			case MATCHER_TIMELINE:
				// Perform the update and return the number of rows updated.
				updatedRowsCount = db.update(TimelineTable.TABLE_NAME, updateValues, whereClause, whereValues);
				break;

			case MATCHER_TWEET:
				String id = uri.getPathSegments().get(TimelineTable.TWEET_ID_PATH_POSITION);
				finalWhere = TimelineTable._ID + " = " + id;

				// if we were passed a 'where' arg, add that to our 'finalWhere'
				if (whereClause != null) {
					finalWhere = finalWhere + " AND " + whereClause;
				}
				updatedRowsCount = db.update(TimelineTable.TABLE_NAME, updateValues, finalWhere, whereValues);
				break;

			default:
				// Incoming URI pattern is invalid: halt & catch fire.
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} finally {
			db.endTransaction();
		}

		/*
		 * Gets a handle to the content resolver object for the current context,
		 * and notifies it that the incoming URI changed. The object passes this
		 * along to the resolver framework, and observers that have registered
		 * themselves for the provider are notified.
		 */
		if (updatedRowsCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		// Returns the number of rows updated.
		return updatedRowsCount;
	}

	//Default bulkInsert is terrible.  Make it better!
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		this.validateOrThrow(uri);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.beginTransaction();
		int insertedCount = 0;
		long newRowId = -1;
		try {
			for (ContentValues cv : values) {
				newRowId = this.insert(uri, cv, db);
				insertedCount++;
			}
			db.setTransactionSuccessful();
			// Build a new Node URI appended with the row ID of the last node to get inserted in the batch
			Uri nodeUri = ContentUris.withAppendedId(TimelineTable.CONTENT_ID_URI_BASE, newRowId);
			// Notify observers that our data changed.
			getContext().getContentResolver().notifyChange(nodeUri, null);
			return insertedCount;

		} finally {
			db.endTransaction();
		}
	}

	//Used by our implementation of builkInsert
	private long insert(Uri uri, ContentValues initialValues, SQLiteDatabase writableDb) {
		// NOTE: this method does not initiate a transaction - this is up to the caller!
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			throw new SQLException("ContentValues arg for .insert() is null, cannot insert row.");
		}

		long newRowId = writableDb.insert(TimelineTable.TABLE_NAME, null, values);
		if (newRowId == -1) { // if rowID is -1, it means the insert failed
			throw new SQLException("Failed to insert row into " + uri); // Insert failed: halt and catch fire.
		}
		return newRowId;
	}

	private void validateOrThrow(Uri uri) {
		// Validate the incoming URI.
		if (uriMatcher.match(uri) != MATCHER_TIMELINE) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case MATCHER_TIMELINE:
			return TIMELINE_CONTENT_TYPE;
		case MATCHER_TWEET:
			return TIMELINE_CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		File file = new File(this.getContext().getFilesDir(), uri.getPath());
		ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
		return parcel;
	}

//	public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts) {
//		try {
//			return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
//		} catch (FileNotFoundException e) {
//			Log.e(TAG, "OH NOES!  Can't find file: " + uri);
//		}
//		return null;
//	}
}
