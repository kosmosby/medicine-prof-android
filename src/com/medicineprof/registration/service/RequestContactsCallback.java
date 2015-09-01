package com.medicineprof.registration.service;

interface RequestContactsCallback {

	void send(int resultCode, String status, String[] phones, String[] names);

}
