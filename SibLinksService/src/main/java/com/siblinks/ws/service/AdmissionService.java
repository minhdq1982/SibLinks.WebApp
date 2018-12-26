package com.siblinks.ws.service;

import org.springframework.http.ResponseEntity;

import com.siblinks.ws.model.RequestData;
import com.siblinks.ws.response.Response;

public interface AdmissionService {

	public ResponseEntity<Response> getAdmission(RequestData request);

	public ResponseEntity<Response> getSubAdmission(RequestData request);

	public ResponseEntity<Response> getTopicSubAdmission(RequestData request);

	public ResponseEntity<Response> getAllTopicSubAdmission(RequestData request);
}
