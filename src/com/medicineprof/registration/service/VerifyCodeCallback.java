package com.medicineprof.registration.service;

interface VerifyCodeCallback {

	void send(int resultCode, String status, String user, String password);

}
