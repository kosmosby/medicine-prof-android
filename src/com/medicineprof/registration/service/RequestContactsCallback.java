package com.medicineprof.registration.service;

import com.medicineprof.registration.model.Contact;

import java.util.List;

interface RequestContactsCallback {

	void send(int resultCode, String status, List<Contact> contacts);

}
