package com.medicineprof.registration.rest;

import com.medicineprof.registration.model.Contact;
import com.medicineprof.registration.provider.ProfileConstants;

import android.content.UriMatcher;
import android.content.Context;

import java.util.List;

public class RestMethodFactory {

	private static RestMethodFactory instance;
	private static Object lock = new Object();
	private UriMatcher uriMatcher;
	private Context mContext;

	private static final int PROFILE = 1;

	private RestMethodFactory(Context context) {
		mContext = context.getApplicationContext();
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(ProfileConstants.AUTHORITY, ProfileConstants.TABLE_NAME, PROFILE);
	}

	public static RestMethodFactory getInstance(Context context) {
		synchronized (lock) {
			if (instance == null) {
				instance = new RestMethodFactory(context);
			}
		}

		return instance;
	}

	public RestMethod getCreateRegistrationCodeRestMethod(String phone) {
        return new CreateRegistrationCodeRestMethod(mContext, phone);
	}
    public RestMethod getVerifyCodeRestMethod(String phone, String code) {
        return new VerifyCodeRestMethod(mContext, phone, code);
    }
    public RestMethod getObtainContactsRestMethod(String user,String code,
                                                  List<Contact> contacts){
        return new ObtainContactsRestMethod(mContext, user, code, contacts);
    }
	public enum Method {
		GET, POST, PUT, DELETE
	}

}
