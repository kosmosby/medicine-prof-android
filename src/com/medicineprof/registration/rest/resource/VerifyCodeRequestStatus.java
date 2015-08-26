package com.medicineprof.registration.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;


public class VerifyCodeRequestStatus implements Resource {

	private String status;
    private String user;
    private String password;

	public VerifyCodeRequestStatus(JSONObject profileJson) throws JSONException {
		this.status = profileJson.getString("status");
        this.user = profileJson.optString("user", "test");
        this.password = profileJson.optString("password", "test");
	}

	public String getStatus() {
		return status;
	}
    public String getUser(){return user;}
    public String getPassword(){return password;}
}
