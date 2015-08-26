package com.medicineprof.registration.provider;

import android.net.Uri;

public class ProfileConstants {

	public static final String TABLE_NAME = "profiles";
	
	public static final String TIMELINE_TABLE_NAME = "timeline";
	
	public static final String AUTHORITY = "com.medicineprof.restfulandroid.profiles";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

	// Columns in the Profiles database
	public static final String NAME = "name";

}
