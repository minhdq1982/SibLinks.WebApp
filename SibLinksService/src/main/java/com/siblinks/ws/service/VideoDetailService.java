package com.siblinks.ws.service;

import org.springframework.http.ResponseEntity;

import com.siblinks.ws.model.RequestData;
import com.siblinks.ws.response.Response;

public interface VideoDetailService {
	public ResponseEntity<Response> getVideoDetailById(long id);

	public ResponseEntity<Response> getCommentVideoById(long vid);

	public ResponseEntity<Response> updateVideoHistory(RequestData request);
}
