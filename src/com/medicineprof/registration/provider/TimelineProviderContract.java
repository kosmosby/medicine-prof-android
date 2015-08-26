package com.medicineprof.registration.provider;


import android.net.Uri;

public final class TimelineProviderContract {
	
	

		public static final String AUTHORITY = "com.medicineprof.restfulandroid.timelineprovider";

		// TIMELINE TABLE CONTRACT
		public static final class TimelineTable implements ResourceTable {

			public static final String TABLE_NAME = "timeline";

			// URI DEFS
			static final String SCHEME = "content://";
			public static final String URI_PREFIX = SCHEME + AUTHORITY;
			private static final String URI_PATH_TIMELINE = "/" + TABLE_NAME;
			// Note the slash on the end of this one, as opposed to the URI_PATH_TIMELINE, which has no slash.
			private static final String URI_PATH_TWEET = "/" + TABLE_NAME + "/";
			public static final int TWEET_ID_PATH_POSITION = 1;

			// content://com.medicineprof.restfulandroid.timelineprovider/timeline
			public static final Uri CONTENT_URI = Uri.parse(URI_PREFIX + URI_PATH_TIMELINE);
			// content://com.medicineprof.restfulandroid.timelineprovider/timeline/ -- used for content provider insert() call
			public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + URI_PATH_TWEET);
			// content://com.medicineprof.restfulandroid.timelineprovider/timeline/#
			public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + URI_PATH_TWEET + "#");

			public static final String[] ALL_COLUMNS;
			public static final String[] DISPLAY_COLUMNS;

			static {
				ALL_COLUMNS = new String[] { 
						TimelineTable._ID, 
						TimelineTable._STATUS, 
						TimelineTable._RESULT, 					
						TimelineTable.AUTHOR, 
						TimelineTable.TWEET_TEXT,
						TimelineTable.CREATED				
				};
				
				DISPLAY_COLUMNS = new String[] { 
						TimelineTable._ID, 
						TimelineTable.AUTHOR, 
						TimelineTable.TWEET_TEXT,
				};
			}
			
			
			/**
			 * Column name for the tweet author
			 * <P>
			 * Type: TEXT
			 * </P>
			 */
			public static final String AUTHOR = "author";

			/**
			 * Column name for tweet content
			 * <P>
			 * Type: TEXT
			 * </P>
			 */
			public static final String TWEET_TEXT = "tweet_text";

			/**
			 * Column name for the creation date
			 * <P>
			 * Type: LONG  (UNIX timestamp)
			 * </P>
			 */
			public static final String CREATED = "timestamp";

			
			// Prevent instantiation of this class
			private TimelineTable() {
			}
		}

		private TimelineProviderContract() {
			// disallow instantiation
		}

}
