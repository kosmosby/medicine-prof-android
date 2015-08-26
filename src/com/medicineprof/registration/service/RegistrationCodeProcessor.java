package com.medicineprof.registration.service;

import com.medicineprof.registration.provider.ProfileConstants;
import com.medicineprof.registration.rest.RestMethod;
import com.medicineprof.registration.rest.RestMethodFactory;
import com.medicineprof.registration.rest.RestMethodResult;
import com.medicineprof.registration.rest.resource.RegistrationCodeRequestStatus;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import com.medicineprof.registration.rest.resource.VerifyCodeRequestStatus;


class RegistrationCodeProcessor {

	protected static final String TAG = RegistrationCodeProcessor.class.getSimpleName();

	private Context mContext;

	public RegistrationCodeProcessor(Context context) {
		mContext = context;
	}

	void requestRegistrationCode(String phone, RegistrationCodeCallback callback) {

		// (4) Insert-Update the ContentProvider with a status column and
		// results column
		// Look at ContentProvider example, and build a content provider
		// that tracks the necessary data.

		// (5) Call the REST method
		// Create a RESTMethod class that knows how to assemble the URL,
		// and performs the HTTP operation.

		@SuppressWarnings("unchecked")
		RestMethod<RegistrationCodeRequestStatus> requestRegistrationCodeMethod =
                RestMethodFactory.getInstance(mContext).getCreateRegistrationCodeRestMethod(phone);
		RestMethodResult<RegistrationCodeRequestStatus> result = requestRegistrationCodeMethod.execute();

		/*
		 * (8) Insert-Update the ContentProvider status, and insert the result
		 * on success Parsing the JSON response (on success) and inserting into
		 * the content provider
		 */

		//updateContentProvider(result);

		// (9) Operation complete callback to Service

        callback.send(result.getStatusCode(), result.getResource()!=null?result.getResource().getStatus():null);

	}

    void verifyRegistrationCode(String phone, String code, VerifyCodeCallback callback) {

        // (4) Insert-Update the ContentProvider with a status column and
        // results column
        // Look at ContentProvider example, and build a content provider
        // that tracks the necessary data.

        // (5) Call the REST method
        // Create a RESTMethod class that knows how to assemble the URL,
        // and performs the HTTP operation.

        @SuppressWarnings("unchecked")
        RestMethod<VerifyCodeRequestStatus> verifyRegistrationCodeMethod =
                RestMethodFactory.getInstance(mContext).getVerifyCodeRestMethod(phone, code);
        RestMethodResult<VerifyCodeRequestStatus> result = verifyRegistrationCodeMethod.execute();

		/*
		 * (8) Insert-Update the ContentProvider status, and insert the result
		 * on success Parsing the JSON response (on success) and inserting into
		 * the content provider
		 */

        //updateContentProvider(result);

        // (9) Operation complete callback to Service

        callback.send(result.getStatusCode(), result.getResource()!=null?result.getResource().getStatus():null,
                result.getResource()!=null?result.getResource().getUser():null,
                result.getResource()!=null?result.getResource().getPassword():null);

    }

	private void updateContentProvider(RestMethodResult<RegistrationCodeRequestStatus> result) {

		if (result != null && result.getResource() != null) {

			String name = result.getResource().getStatus();

			if (name != null) {

				ContentValues values = new ContentValues();
				values.put(ProfileConstants.NAME, name);

				Cursor cursor = mContext.getContentResolver().query(ProfileConstants.CONTENT_URI,
						null, null, null, null);
				if (cursor.moveToFirst()) {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
					mContext.getContentResolver().update(
							ContentUris.withAppendedId(ProfileConstants.CONTENT_URI, id), values,
							null, null);
				} else {
					mContext.getContentResolver().insert(ProfileConstants.CONTENT_URI, values);
				}
				cursor.close();
			}
		}

	}

}
