package com.medicineprof.registration.service;

interface RegistrationCodeCallback {

	void send(int resultCode, String status);

}
