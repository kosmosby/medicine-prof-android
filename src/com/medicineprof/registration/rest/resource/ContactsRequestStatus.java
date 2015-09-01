package com.medicineprof.registration.rest.resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ContactsRequestStatus implements Resource {

	private String status;
    private String[] contactPhones;
    private String[] contactNames;

	public ContactsRequestStatus(JSONObject profileJson) throws JSONException {
		this.status = profileJson.getString("status");
        JSONArray contacts = profileJson.getJSONArray("contacts");
        contactPhones = new String[contacts.length()];
        contactNames = new String[contacts.length()];
        for(int i = 0 ; i < contacts.length(); i++){
            contactPhones[i] = contacts.getJSONObject(i).getString("phone");
            contactNames[i] = contacts.getJSONObject(i).getString("name");
        }

	}

	public String getStatus() {
		return status;
	}
    public String[] getContactPhones(){return contactPhones;}
    public String[] getContactNames(){return contactNames;}
}
