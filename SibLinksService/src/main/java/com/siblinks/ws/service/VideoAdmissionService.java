package com.siblinks.ws.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.siblinks.ws.model.RequestData;
import com.siblinks.ws.response.Response;

public interface VideoAdmissionService {

	public ResponseEntity<Response> getVideoTopicSubAdmissionPN(RequestData request);

	public ResponseEntity<Response> getVideoTopicSubAdmission(RequestData request);
	
	public ResponseEntity<Response> getVideoAdmissionDetail(RequestData request);

	public ResponseEntity<Response> getVideoAdmissionCommentsPN(RequestData request);
	
	public ResponseEntity<Response> orderVideoAdmissionPN(RequestData request);

	public ResponseEntity<Response> updateViewVideoAdmission(RequestData request);

	public ResponseEntity<Response> deleteVideoAdmission(RequestData request);

	public ResponseEntity<Response> uploadFile(MultipartFile uploadfile)
			throws IOException;

	public ResponseEntity<Response> updateVideoAdmission(RequestData request);

	public ResponseEntity<Response> createVideoAdmission(RequestData request) throws FileNotFoundException;

	public ResponseEntity<byte[]> getImageVideoAdmission(String vId)
			throws IOException;

	public ResponseEntity<Response> updateVideoAdmissionWatched(RequestData request);

	public ResponseEntity<Response> getIdVideoAdmissionWatched(RequestData request);

	public ResponseEntity<Response> rateVideoAdmission(RequestData request);

	public ResponseEntity<Response> getRatingVideoAdmission(RequestData request);

	public ResponseEntity<Response> checkUserRatingVideoAdmission(RequestData request);

    /**
     * @param idAdmission
     * @return
     */
    public ResponseEntity<Response> getVideoTuttorialAdmission(String idAdmission);

    /**
     * @param idAdmission
     * @return
     */
    public ResponseEntity<Response> getArticleAdmission(String idAdmission);
}
