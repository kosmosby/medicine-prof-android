package com.medicineprof.registration.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import com.medicineprof.registration.rest.RestMethodFactory.Method;
import com.medicineprof.registration.rest.resource.RegistrationCodeRequestStatus;

import org.json.JSONObject;

import android.content.Context;

public class CreateRegistrationCodeRestMethod extends AbstractRestMethod<RegistrationCodeRequestStatus> {
	
	private Context mContext;
    private String phone;
    private String userName;

	private static final URI PROFILE_URI = URI
			.create("http://medicine-prof.com/index.php"); //192.168.100.5
	
	public CreateRegistrationCodeRestMethod(Context context, String phone, String userName) {
		mContext = context.getApplicationContext();
        this.phone = phone;
        this.userName = userName;
	}

	@Override
	protected Request buildRequest() {
        Request request = null;
        try {
            String requestBody = "option=com_openfire&task=register_phone&phone=";
            requestBody = requestBody + URLEncoder.encode(phone, "UTF-8");
            requestBody = requestBody + "&name=";
            requestBody = requestBody + URLEncoder.encode(userName, "UTF-8");
            request = new Request(Method.POST, PROFILE_URI, null, requestBody.getBytes());
        }catch(UnsupportedEncodingException e){

        }
        return request;
	}

	@Override
	protected RegistrationCodeRequestStatus parseResponseBody(String responseBody) throws Exception {

		JSONObject json = new JSONObject(responseBody);
		return new RegistrationCodeRequestStatus(json);

	}

	@Override
	protected Context getContext() {
		return mContext;
	}

    @Override
    protected boolean requiresAuthorization(){return false;}

}
