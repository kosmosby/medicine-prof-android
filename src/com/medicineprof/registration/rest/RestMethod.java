package com.medicineprof.registration.rest;

import com.medicineprof.registration.rest.resource.Resource;

public interface RestMethod<T extends Resource>{

	public RestMethodResult<T> execute();
}
