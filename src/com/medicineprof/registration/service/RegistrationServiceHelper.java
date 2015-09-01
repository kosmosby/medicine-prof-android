package com.medicineprof.registration.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;


/**
 * User Registration API
 *
 * @author akanstantsinau
 */
public class RegistrationServiceHelper {

	public static String ACTION_REQUEST_RESULT = "REQUEST_RESULT";
	public static String EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID";
	public static String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";

	private static final String REQUEST_ID = "REQUEST_ID";
	private static final String registrationCodeHashkey = "REGISTRATION_CODE";
    private static final String verifyCodeHashkey = "VERIFY_CODE";
    private static final String requestContactsHashkey = "REQUEST_CONTACTS";

	private static Object lock = new Object();

	private static RegistrationServiceHelper instance;

	//TODO: refactor the key
	private Map<String,Long> pendingRequests = new HashMap<String,Long>();
	private Context ctx;

	private RegistrationServiceHelper(Context ctx){
		this.ctx = ctx.getApplicationContext();
	}

	public static RegistrationServiceHelper getInstance(Context ctx){
		synchronized (lock) {
			if(instance == null){
				instance = new RegistrationServiceHelper(ctx);
			}
		}

		return instance;		
	}

	public long requestRegistrationCode(String phoneNumber){

		if(pendingRequests.containsKey(registrationCodeHashkey)){
			return pendingRequests.get(registrationCodeHashkey);
		}

		long requestId = generateRequestID();
		pendingRequests.put(registrationCodeHashkey, requestId);

		ResultReceiver serviceCallback = new ResultReceiver(null){

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				handleRequestRegistrationCodeResponse(resultCode, resultData);
			}

		};

		Intent intent = new Intent(this.ctx, RegistrationService.class);
		intent.putExtra(RegistrationService.PHONE_NUMBER_EXTRA, phoneNumber);
		intent.putExtra(RegistrationService.RESOURCE_TYPE_EXTRA,
				RegistrationService.RESOURCE_TYPE_REQUEST_REGISTRATION_CODE);
		intent.putExtra(RegistrationService.SERVICE_CALLBACK, serviceCallback);
		intent.putExtra(REQUEST_ID, requestId);

		this.ctx.startService(intent);

		return requestId;
	}

    public long verifyRegistrationCode(String phoneNumber, String code){

        if(pendingRequests.containsKey(verifyCodeHashkey)){
            return pendingRequests.get(verifyCodeHashkey);
        }

        long requestId = generateRequestID();
        pendingRequests.put(verifyCodeHashkey, requestId);

        ResultReceiver serviceCallback = new ResultReceiver(null){

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                handleVerifyCodeResponse(resultCode, resultData);
            }

        };

        Intent intent = new Intent(this.ctx, RegistrationService.class);
        intent.putExtra(RegistrationService.PHONE_NUMBER_EXTRA, phoneNumber);
        intent.putExtra(RegistrationService.REGISTRATION_CODE_EXTRA, code);
        intent.putExtra(RegistrationService.RESOURCE_TYPE_EXTRA,
                RegistrationService.RESOURCE_TYPE_VERIFY_REGISTRATION_CODE);
        intent.putExtra(RegistrationService.SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);

        this.ctx.startService(intent);

        return requestId;
    }

    public long requestContacts(String phoneNumber, String code, String[] phones, String[] names){

        if(pendingRequests.containsKey(requestContactsHashkey)){
            return pendingRequests.get(requestContactsHashkey);
        }

        long requestId = generateRequestID();
        pendingRequests.put(requestContactsHashkey, requestId);

        ResultReceiver serviceCallback = new ResultReceiver(null){

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                handleRequestContactsResponse(resultCode, resultData);
            }

        };

        Intent intent = new Intent(this.ctx, RegistrationService.class);
        intent.putExtra(RegistrationService.PHONE_NUMBER_EXTRA, phoneNumber);
        intent.putExtra(RegistrationService.REGISTRATION_CODE_EXTRA, code);
        intent.putExtra(RegistrationService.CONTACT_PHONES_EXTRA, phones);
        intent.putExtra(RegistrationService.CONTACT_NAMES_EXTRA, names);

        intent.putExtra(RegistrationService.RESOURCE_TYPE_EXTRA,
                RegistrationService.RESOURCE_TYPE_REQUEST_CONTACTS);
        intent.putExtra(RegistrationService.SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);

        this.ctx.startService(intent);

        return requestId;
    }

	private long generateRequestID() {
		long requestId = UUID.randomUUID().getLeastSignificantBits();
		return requestId;
	}

	public boolean isRequestPending(long requestId){
		return this.pendingRequests.containsValue(requestId);
	}

	private void handleRequestRegistrationCodeResponse(int resultCode, Bundle resultData){


		Intent origIntent = (Intent)resultData.getParcelable(RegistrationService.ORIGINAL_INTENT_EXTRA);

		if(origIntent != null){
			long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

			pendingRequests.remove(registrationCodeHashkey);

			Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
			resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
			resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
            resultBroadcast.putExtra("type", "request_code");
            resultBroadcast.putExtra("status", resultData.getString("status"));
			ctx.sendBroadcast(resultBroadcast);

		}
	}

    private void handleVerifyCodeResponse(int resultCode, Bundle resultData){


        Intent origIntent = (Intent)resultData.getParcelable(RegistrationService.ORIGINAL_INTENT_EXTRA);

        if(origIntent != null){
            long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

            pendingRequests.remove(verifyCodeHashkey);

            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
            resultBroadcast.putExtra("type", "verify_code");
            resultBroadcast.putExtra("status", resultData.getString("status"));
            resultBroadcast.putExtra("user", resultData.getString("user"));
            resultBroadcast.putExtra("password", resultData.getString("password"));
            ctx.sendBroadcast(resultBroadcast);

        }
    }

    private void handleRequestContactsResponse(int resultCode, Bundle resultData){


        Intent origIntent = (Intent)resultData.getParcelable(RegistrationService.ORIGINAL_INTENT_EXTRA);

        if(origIntent != null){
            long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

            pendingRequests.remove(requestContactsHashkey);

            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
            resultBroadcast.putExtra("type", "request_contacts");
            resultBroadcast.putExtra("status", resultData.getString("status"));
            resultBroadcast.putExtra("phones", resultData.getStringArray("phones"));
            resultBroadcast.putExtra("names", resultData.getStringArray("contacts"));
            ctx.sendBroadcast(resultBroadcast);

        }
    }
}
