package com.medicineprof.registration.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;


public class RegistrationCodeRequestStatus implements Resource {

	private String status;

	public RegistrationCodeRequestStatus(JSONObject profileJson) throws JSONException {
		this.status = profileJson.getString("status");
	}

	public String getStatus() {
		return status;
	}
}
