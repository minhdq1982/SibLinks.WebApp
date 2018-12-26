package com.siblinks.ws.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.siblinks.ws.common.LoggedInChecker;
import com.siblinks.ws.dao.ObjectDao;
import com.siblinks.ws.filter.AuthenticationFilter;
import com.siblinks.ws.model.RequestData;
import com.siblinks.ws.response.Response;
import com.siblinks.ws.response.SimpleResponse;
import com.siblinks.ws.service.VideoDetailService;
import com.siblinks.ws.util.SibConstants;

@RestController
@RequestMapping("/siblinks/services/videodetail")
public class VideoDetailServiceImpl implements VideoDetailService {
	  private final Log logger = LogFactory.getLog(getClass());

	    @Autowired
	    private HttpServletRequest context;

	    @Autowired
	    private ObjectDao dao;

	    private final LoggedInChecker loggedInChecker;

	    @Autowired
	    VideoDetailServiceImpl(final LoggedInChecker loggedInChecker) {
	        this.loggedInChecker = loggedInChecker;
	    }

	    @Override
	    @RequestMapping(value = "/getVideoDetailById/{vid}", method = RequestMethod.GET)
	    public ResponseEntity<Response> getVideoDetailById(@PathVariable(value = "vid") final long vid) {
	        String entityName = SibConstants.SqlMapper.SQL_GET_VIDEODETAIL_BY_ID;
	        Object[] queryParams = { vid };

	        List<Object> readObject = dao.readObjects(entityName, queryParams);
	        SimpleResponse reponse = new SimpleResponse("" + true, "Video", "getVideoById", readObject);
	        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
	        return entity;
	    }
	    
	    @Override
	    @RequestMapping(value = "/getCommentVideoById/{vid}", method = RequestMethod.GET)
	    public ResponseEntity<Response> getCommentVideoById(@PathVariable(value = "vid") final long vid) {
	        String entityName = SibConstants.SqlMapper.SQL_GET_COMMENT_VIDEO_BY_VID;
	        Object[] queryParams = { vid };

	        List<Object> readObject = dao.readObjects(entityName, queryParams);
	        SimpleResponse reponse = new SimpleResponse("" + true, "Video", "getCommentVideoById", readObject);
	        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
	        return entity;
	    }
	    
	    @Override
	    @RequestMapping(value = "/updateVideoHistory", method = RequestMethod.POST)
	    public @ResponseBody ResponseEntity<Response> updateVideoHistory(@RequestBody final RequestData request) {

	        if (!AuthenticationFilter.isAuthed(context)) {
	            ResponseEntity<Response> entity = new ResponseEntity<Response>(
	                                                                           new SimpleResponse(
	                                                                                              "" +
	                                                                                              false,
	                                                                                              "Authentication required."),
	                                                                           HttpStatus.FORBIDDEN);
	            return entity;
	        }

	        Object[] queryParams = {request.getRequest_data().getUid(),request.getRequest_data().getVid()};

	        boolean status = false;
	        String entityName = SibConstants.SqlMapper.SQL_CHECK_USER_HISTORY_VIDEO;
	        List<Object> readObject = dao.readObjects(entityName, queryParams);
	        if (readObject.size() < 1) {
	            entityName = SibConstants.SqlMapper.SQL_INSERT_HISTORY_VIDEO;
	            status = dao.insertUpdateObject(entityName, queryParams);
	        }

	        String message = "";
	        if (status) {
	            message = "Done";
	        } else {
	            message = "Fail";
	        }

	        SimpleResponse reponse = new SimpleResponse(
	                                                    "" +
	                                                    status,
	                                                    request.getRequest_data_type(),
	                                                    request.getRequest_data_method(),
	                                                    message);
	        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);

	        return entity;
	    }
	    
}
