package com.medicineprof.registration.rest;

import android.content.Context;
import com.medicineprof.registration.rest.RestMethodFactory.Method;
import com.medicineprof.registration.rest.resource.ContactsRequestStatus;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class ObtainContactsRestMethod extends AbstractRestMethod<ContactsRequestStatus> {

	private Context mContext;
    private String phone;
    private String password;
    private String[] contactPhones;
    private String[] contactNames;

	private static final URI PROFILE_URI = URI
			//.create("http://medicine-prof.com/index.php");
            .create("http://192.168.100.5/index.php");

	public ObtainContactsRestMethod(Context context, String phone, String password,
                                    String[] contactPhones, String[] contactNames) {
		mContext = context.getApplicationContext();
        this.phone = phone;
        this.password = password;
        this.contactPhones = contactPhones;
        this.contactNames = contactNames;
	}

	@Override
	protected Request buildRequest() {
        Request request = null;
        try {
            String requestBody = "option=com_openfire&task=get_contacts&phone=";
            requestBody = requestBody + URLEncoder.encode(phone, "UTF-8");
            requestBody = requestBody + "&password=" + URLEncoder.encode(password, "UTF-8");
            for(int i = 0 ; i < contactPhones.length; i++){
                requestBody = requestBody + "&contact_phones[]=" + URLEncoder.encode(contactPhones[i], "UTF-8");
            }
            for(int i = 0 ; i < contactNames.length; i++){
                requestBody = requestBody + "&contact_names[]=" + URLEncoder.encode(contactNames[i], "UTF-8");
            }
            request = new Request(Method.POST, PROFILE_URI, null, requestBody.getBytes());
        }catch(UnsupportedEncodingException e){

        }
        return request;
	}

	@Override
	protected ContactsRequestStatus parseResponseBody(String responseBody) throws Exception {

		JSONObject json = new JSONObject(responseBody);
		return new ContactsRequestStatus(json);

	}

	@Override
	protected Context getContext() {
		return mContext;
	}

    @Override
    protected boolean requiresAuthorization(){return false;}

}
