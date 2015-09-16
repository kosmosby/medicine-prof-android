package com.medicineprof.registration.rest;

import android.content.Context;
import com.medicineprof.registration.model.Contact;
import com.medicineprof.registration.rest.RestMethodFactory.Method;
import com.medicineprof.registration.rest.resource.ContactsRequestStatus;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

public class ObtainContactsRestMethod extends AbstractRestMethod<ContactsRequestStatus> {

	private Context mContext;
    private String user;
    private String password;
    private List<Contact>contacts;

	private static final URI PROFILE_URI = URI
			.create("http://medicine-prof.com/index.php");
            //.create("http://192.168.100.5/index.php");

	public ObtainContactsRestMethod(Context context, String user, String password,
                                    List<Contact> contacts) {
		mContext = context.getApplicationContext();
        this.user = user;
        this.password = password;
        this.contacts = contacts;
	}

	@Override
	protected Request buildRequest() {
        Request request = null;
        try {
            String requestBody = "option=com_openfire&task=get_contacts&user=";
            requestBody = requestBody + URLEncoder.encode(user, "UTF-8");
            requestBody = requestBody + "&password=" + URLEncoder.encode(password, "UTF-8");
            for(Contact contact:contacts){
                requestBody = requestBody + "&contact_phones[]=" + URLEncoder.encode(contact.getPhone(), "UTF-8");
                requestBody = requestBody + "&contact_names[]=" + URLEncoder.encode(contact.getName(), "UTF-8");
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
