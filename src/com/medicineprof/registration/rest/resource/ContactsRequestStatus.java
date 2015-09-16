package com.medicineprof.registration.rest.resource;

import com.medicineprof.registration.model.Contact;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ContactsRequestStatus implements Resource {

	private String status;
    private List<Contact> contacts;

	public ContactsRequestStatus(JSONObject profileJson) throws JSONException {
		this.status = profileJson.getString("status");
        JSONArray contactsArray = profileJson.getJSONArray("contacts");
        contacts = new ArrayList<Contact>();
        for(int i = 0 ; i < contactsArray.length(); i++){
            Contact contact = new Contact();
            contact.setPhone(contactsArray.getJSONObject(i).getString("phone"));
            contact.setName(contactsArray.getJSONObject(i).getString("name"));
            contact.setJabberUsername(contactsArray.getJSONObject(i).getString("jabberUsername"));
            contact.setContactAdded(contactsArray.getJSONObject(i).getBoolean("contactAdded"));
            contact.setContactExists(contactsArray.getJSONObject(i).getBoolean("contactExists"));
            contacts.add(contact);
        }

	}

	public String getStatus() {
		return status;
	}
    public List<Contact> getContacts(){return contacts;}
}
