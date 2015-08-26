package com.medicineprof.registration.provider;

import com.medicineprof.registration.provider.TimelineProviderContract.TimelineTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This creates, updates, and opens the database.  Opening is handled by the superclass, we handle 
 * the create & upgrade steps
 */
public class ProviderDbHelper extends SQLiteOpenHelper {

	public final String TAG = getClass().getSimpleName();

	//Name of the database file
	private static final String DATABASE_NAME = "restdroid.db";
	private static final int DATABASE_VERSION = 1;

	public ProviderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// CREATE TIMELINE TABLE
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE " + TimelineTable.TABLE_NAME + " (");
		sqlBuilder.append(TimelineTable._ID + " INTEGER, ");
		sqlBuilder.append(TimelineTable._STATUS + " TEXT, ");
		sqlBuilder.append(TimelineTable._RESULT + " INTEGER, ");
		sqlBuilder.append(TimelineTable.AUTHOR + " TEXT, ");
		sqlBuilder.append(TimelineTable.TWEET_TEXT + " TEXT, ");
		sqlBuilder.append(TimelineTable.CREATED + " INTEGER, ");
		sqlBuilder.append(");");
		String sql = sqlBuilder.toString();
		Log.i(TAG, "Creating DB table with string: '" + sql + "'");
		db.execSQL(sql);
		
		// CREATE PROFILE TABLE
		sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE " + ProfileConstants.TABLE_NAME + " (");
		sqlBuilder.append(ResourceTable._ID + " INTEGER, ");
		sqlBuilder.append(ResourceTable._STATUS + " TEXT, ");
		sqlBuilder.append(ResourceTable._RESULT + " INTEGER, ");
		sqlBuilder.append(ProfileConstants.NAME + " TEXT, ");
		
		sql = sqlBuilder.toString();
		Log.i(TAG, "Creating DB table with string: '" + sql + "'");
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Gets called when the database is upgraded, i.e. the version number changes
	}

}
