package com.medicineprof.registration.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import com.medicineprof.registration.model.Contact;

import java.io.Serializable;
import java.util.List;

public class RegistrationService extends IntentService {

	public static final String METHOD_GET = "GET";

	public static final String RESOURCE_TYPE_EXTRA = "com.medicineprof.registration.service.RESOURCE_TYPE_EXTRA";
    public static final String PHONE_NUMBER_EXTRA = "com.medicineprof.registration.service.PHONE_NUMBER_EXTRA";
    public static final String USER_NAME_EXTRA = "com.medicineprof.registration.service.USER_NAME_EXTRA";
    public static final String REGISTRATION_CODE_EXTRA = "com.medicineprof.registration.service.REGISTRATION_CODE_EXTRA";
    public static final String CONTACTS_EXTRA = "com.medicineprof.registration.service.CONTACT_PHONES_EXTRA";
    public static final String CONTACT_PHONES_EXTRA = "com.medicineprof.registration.service.CONTACTS_EXTRA";
    public static final String CONTACT_NAMES_EXTRA = "com.medicineprof.registration.service.CONTACT_NAMES_EXTRA";

	public static final int RESOURCE_TYPE_REQUEST_REGISTRATION_CODE = 1;
    public static final int RESOURCE_TYPE_VERIFY_REGISTRATION_CODE = 2;
    public static final int RESOURCE_TYPE_REQUEST_CONTACTS = 3;

	public static final String SERVICE_CALLBACK = "com.medicineprof.registration.service.SERVICE_CALLBACK";


	public static final String ORIGINAL_INTENT_EXTRA = "com.medicineprof.registration.service.ORIGINAL_INTENT_EXTRA";

	private static final int REQUEST_INVALID = -1;

	private ResultReceiver mCallback;

	private Intent mOriginalRequestIntent;

	public RegistrationService() {
		super("RegistrationService");
	}

	@Override
	protected void onHandleIntent(Intent requestIntent) {

		mOriginalRequestIntent = requestIntent;

		// Get request data from Intent
		int resourceType = requestIntent.getIntExtra(RegistrationService.RESOURCE_TYPE_EXTRA, -1);
		mCallback = requestIntent.getParcelableExtra(RegistrationService.SERVICE_CALLBACK);

		switch (resourceType) {
		    case RESOURCE_TYPE_REQUEST_REGISTRATION_CODE: {
                String phone = requestIntent.getStringExtra(PHONE_NUMBER_EXTRA);
                RegistrationCodeProcessor processor = new RegistrationCodeProcessor(getApplicationContext());
                processor.requestRegistrationCode(phone, makeProfileProcessorCallback());
            }
                break;

            case RESOURCE_TYPE_VERIFY_REGISTRATION_CODE: {
                String phone = requestIntent.getStringExtra(PHONE_NUMBER_EXTRA);
                String code = requestIntent.getStringExtra(REGISTRATION_CODE_EXTRA);
                RegistrationCodeProcessor processor = new RegistrationCodeProcessor(getApplicationContext());
                processor.verifyRegistrationCode(phone, code, makeVerifyCodeCallback());
            }
                break;
            case RESOURCE_TYPE_REQUEST_CONTACTS: {
                String user = requestIntent.getStringExtra(USER_NAME_EXTRA);
                String code = requestIntent.getStringExtra(REGISTRATION_CODE_EXTRA);
                List<Contact> contacts = (List<Contact>)requestIntent.getSerializableExtra(CONTACTS_EXTRA);

                RegistrationCodeProcessor processor = new RegistrationCodeProcessor(getApplicationContext());
                processor.obtainContacts(user, code, contacts, makeObtainContactsCallback());
            }
            break;
		default:
			mCallback.send(REQUEST_INVALID, getOriginalIntentBundle());
			break;
		}

	}

	private RegistrationCodeCallback makeProfileProcessorCallback() {
		RegistrationCodeCallback callback = new RegistrationCodeCallback() {

			@Override
			public void send(int resultCode, String status) {
				if (mCallback != null) {
                    Bundle bundle = getOriginalIntentBundle();
                    bundle.putInt("resultCode", resultCode);
                    bundle.putString("status", status);
					mCallback.send(resultCode, bundle);
				}
			}
		};
		return callback;
	}

    private VerifyCodeCallback makeVerifyCodeCallback() {
        VerifyCodeCallback callback = new VerifyCodeCallback() {

            @Override
            public void send(int resultCode, String status, String user, String password) {
                if (mCallback != null) {
                    Bundle bundle = getOriginalIntentBundle();
                    bundle.putInt("resultCode", resultCode);
                    bundle.putString("status", status);
                    bundle.putString("user", user);
                    bundle.putString("password", password);
                    mCallback.send(resultCode, bundle);
                }
            }
        };
        return callback;
    }

    private RequestContactsCallback makeObtainContactsCallback() {
        RequestContactsCallback callback = new RequestContactsCallback() {

            @Override
            public void send(int resultCode, String status, List<Contact>contacts) {
                if (mCallback != null) {
                    Bundle bundle = getOriginalIntentBundle();
                    bundle.putInt("resultCode", resultCode);
                    bundle.putString("status", status);
                    bundle.putSerializable("contacts", (Serializable) contacts);
                    mCallback.send(resultCode, bundle);
                }
            }
        };
        return callback;
    }

	protected Bundle getOriginalIntentBundle() {
		Bundle originalRequest = new Bundle();
		originalRequest.putParcelable(ORIGINAL_INTENT_EXTRA, mOriginalRequestIntent);
		return originalRequest;
	}
}
