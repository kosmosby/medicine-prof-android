package com.medicineprof.registration.rest;

import android.content.Context;
import com.medicineprof.registration.rest.RestMethodFactory.Method;
import com.medicineprof.registration.rest.resource.RegistrationCodeRequestStatus;
import com.medicineprof.registration.rest.resource.VerifyCodeRequestStatus;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class VerifyCodeRestMethod extends AbstractRestMethod<VerifyCodeRequestStatus> {

	private Context mContext;
    private String phone;
    private String code;

	private static final URI PROFILE_URI = URI
			.create("http://medicine-prof.com/index.php");

	public VerifyCodeRestMethod(Context context, String phone, String code) {
		mContext = context.getApplicationContext();
        this.phone = phone;
        this.code = code;
	}

	@Override
	protected Request buildRequest() {
        Request request = null;
        try {
            String requestBody = "option=com_openfire&task=verify_code&phone=";
            requestBody = requestBody + URLEncoder.encode(phone, "UTF-8");
            requestBody = requestBody + "&code=" + URLEncoder.encode(code, "UTF-8");
            request = new Request(Method.POST, PROFILE_URI, null, requestBody.getBytes());
        }catch(UnsupportedEncodingException e){

        }
        return request;
	}

	@Override
	protected VerifyCodeRequestStatus parseResponseBody(String responseBody) throws Exception {

		JSONObject json = new JSONObject(responseBody);
		return new VerifyCodeRequestStatus(json);

	}

	@Override
	protected Context getContext() {
		return mContext;
	}

    @Override
    protected boolean requiresAuthorization(){return false;}

}
