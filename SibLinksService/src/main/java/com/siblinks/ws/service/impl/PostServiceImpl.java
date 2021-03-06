/*
 * Copyright (c) 2016-2017, Tinhvan Outsourcing JSC. All rights reserved.
 *
 * No permission to use, copy, modify and distribute this software
 * and its documentation for any purpose is granted.
 * This software is provided under applicable license agreement only.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.siblinks.ws.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.siblinks.ws.dao.ObjectDao;
import com.siblinks.ws.filter.AuthenticationFilter;
import com.siblinks.ws.model.RequestData;
import com.siblinks.ws.response.Response;
import com.siblinks.ws.response.SimpleResponse;
import com.siblinks.ws.service.PostService;
import com.siblinks.ws.util.CommonUtil;
import com.siblinks.ws.util.Parameters;
import com.siblinks.ws.util.RandomString;
import com.siblinks.ws.util.SibConstants;
import com.siblinks.ws.util.StringUtil;

/**
 *
 *
 * @author hungpd
 * @version 1.0
 */
@RestController
@RequestMapping("/siblinks/services/post")
public class PostServiceImpl implements PostService {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private HttpServletRequest context;

    @Autowired
    ObjectDao dao;

    @Autowired
    private Environment environment;

    @SuppressWarnings("rawtypes")
    @Override
    @RequestMapping(value = "/createPost", method = RequestMethod.POST)
    public ResponseEntity<Response> createPost(
    		@RequestParam("content") final String content
    		, @RequestParam("userId") final String userId
    		,@RequestParam("file") final MultipartFile[]  files
    		, @RequestParam("subjectId") final String subjectId
    		) {
        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }
		long id = 0l;
		String error = validateFileImage(files);
		if(!StringUtil.isNull(error)){
			SimpleResponse reponse = new SimpleResponse("" + Boolean.FALSE, "post", "createPost", error);
			ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
			return entity;
		}
		try {
			

			MultipartFile file;
			String filePath = "";
			if(files!=null&&files.length>0){
				for (int i = 0; i < files.length; i++) {
					file = files[i];
					filePath += uploadFile(file,"directoryImageQuestion");
					if (i < files.length - 1) {
						filePath += ";";
					}
				}
			}
			
			id = dao.insertObject(
					SibConstants.SqlMapper.SQL_CREATE_QUESTION,
					new Object[] { userId, subjectId, content, filePath });

		} catch (IOException e1) {
			e1.printStackTrace();
		}

        SimpleResponse reponse = new SimpleResponse(
                                                    "" + (id > 0),
                                                    "post",
                                                    "createPost",
                                                    id);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings("rawtypes")
    @Override
    @RequestMapping(value = "/createAnswer", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Response> createAnswer(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

       

        long pid = 0l; 
        Object[] queryParamsAnswer = {
      	      request.getRequest_data().getAuthorID(),
      	      pid,
      	        request.getRequest_data().getContent()};
        pid = dao.insertObject(SibConstants.SqlMapper.SQL_CREATE_ANSWER, queryParamsAnswer);
       

        List<Object> readObject = null;
        Object[] queryParams ={
    		   request.getRequest_data().getUid(),
    		   request.getRequest_data().getAuthorID() ,
    		   "answerQuestion",
    		   "Answer to question",
    		   "answered a question: " + request.getRequest_data().getContent(),
    		   request.getRequest_data().getSubjectId(),
    		   request.getRequest_data().getTopicId(),
    		   request.getRequest_data().getPid()
    		   
       };
        boolean status = false;
        status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_CREATE_NOTIFICATION_QUESTION, queryParams);
		if (pid > 0 && status == true) {
			status = true;
		}
		SimpleResponse reponse = new SimpleResponse("" + status, request.getRequest_data_type(),
				request.getRequest_data_method(), status);
		ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
		return entity;
    }

    @Override
    @RequestMapping(value = "/editPost", method = RequestMethod.POST)
	public ResponseEntity<Response> editPost(@RequestParam("content") final String content,
			@RequestParam("qid") final String qid, @RequestParam("file") final MultipartFile[] files,
			@RequestParam("subjectId") final String subjectId,
			@RequestParam("oldImagePathEdited") final String oldImagePathEdited,
			@RequestParam("oldImagePath") final String oldImagePath) {


        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }
        String error = validateFileImage(files);
		if(!StringUtil.isNull(error)){
			SimpleResponse reponse = new SimpleResponse("" + Boolean.FALSE, "post", "createPost", error);
			ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
			return entity;
		}
        Boolean status = true;
        try {
        	MultipartFile file;
			String filePath = "";
        	String filePathEdited = getPathFile(oldImagePath, oldImagePathEdited,"directoryImageQuestion");
        	String maxLength = environment.getProperty("file.upload.image.length");
			
			if(files!=null&&files.length>0){
				if((files.length + filePathEdited.split(";").length) > Integer.parseInt(maxLength)){
					error = "You only upload " + maxLength +" image";
					Response reponse = new SimpleResponse("" + Boolean.FALSE, "post", "createPost", error);
					ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
					return entity;
				}
				for (int i = 0; i < files.length; i++) {
					file = files[i];
					filePath += uploadFile(file,"directoryImageQuestion");
					if (i < files.length - 1) {
						filePath += ";";
					}
				}
			}
			
			//imagePath = ?,content=?,subjectId = ? where pid= ?
			dao.insertUpdateObject(
					SibConstants.SqlMapper.SQL_POST_EDIT,
					new Object[] { fixFilePath(filePathEdited +filePath) ,content, subjectId, qid});

		} catch (IOException e1) {
			status = false;
			e1.printStackTrace();
		}



        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    status,
                                                    "Post",
                                                    "EditPost",
                                                    "");
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/getPosts", method = RequestMethod.POST)
    public ResponseEntity<Response> getPosts(@RequestBody final RequestData request) {

        Map<String, String> queryParams = new HashMap<String, String>();

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_POSTS, queryParams);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getPostInfo", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostInfo(@RequestBody final RequestData request) {

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("pid", request.getRequest_data().getPid());

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_POST_INFO, queryParams);

        List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);

        Map<String, Object> tags = null;

        try {
            if (readObject1 != null) {
                for (Object obj : readObject1) {
                    tags = (Map) obj;
                    Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pairs = it.next();
                        if (pairs.getKey().equals("pid")) {
                            it.remove();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        Map<String, Object> mymap = new HashMap<String, Object>();
        mymap.put("tags", readObject1);

        readObject.add(mymap);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/getPostList", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostList(@RequestBody final RequestData request) {

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("subjectId", request.getRequest_data().getSubjectId());
        String sqlMapper = "";
        if (request.getRequest_data().getSubjectId().equals("0")) {
            sqlMapper = SibConstants.SqlMapper.SQL_GET_POST_LIST;
        } else {
            sqlMapper = SibConstants.SqlMapper.SQL_GET_POST_SUBID_LIST;
        }

        List<Object> readObject = null;
        readObject = dao.readObjects(sqlMapper, queryParams);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }
    
    @Override
    @RequestMapping(value = "/getAllQuestions", method = RequestMethod.POST)
    public ResponseEntity<Response> getAllQuestions(@RequestBody final RequestData request) {
        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_ALL_QUESTION, new Object[]{});

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }
    

    @Override
    @RequestMapping(value = "/getPostById", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostById(@RequestBody final RequestData request) {

        List<Object> readObject = null;
        readObject = dao
            .readObjects(SibConstants.SqlMapper.SQL_GET_POST_BY_ID, new Object[] { request.getRequest_data().getPid() });

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/getAnswerById", method = RequestMethod.POST)
    public ResponseEntity<Response> getAnswerById(@RequestBody final RequestData request) {

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("pid", request.getRequest_data().getPid());
        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LASTEST, queryParams);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/getAnswerList", method = RequestMethod.POST)
    public ResponseEntity<Response> getAnswerList(@RequestBody final RequestData request) {

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("pid", request.getRequest_data().getPid());
        String sqlMapper = "";
        if (request.getRequest_data().getPid().equals("0")) {
            sqlMapper = SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LIST;
        } else {
            sqlMapper = SibConstants.SqlMapper.SQL_GET_POST_ANSWER_SUBID_LIST;
        }

        List<Object> readObject = null;
        readObject = dao.readObjects(sqlMapper, queryParams);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getPostListPN", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostListPN(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("authorId", request.getRequest_data().getUid());
        queryParams.put("subjectId", request.getRequest_data().getSubjectId());
        queryParams.put("tId", request.getRequest_data().getTopicId());
        queryParams.put("from", map.get("from"));
        queryParams.put("to", map.get("to"));

        List<Object> readObject = null;
        if (request.getRequest_data().getSubjectId().equals("0")) {
            readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_POST_LIST_PN, queryParams);
        } else {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBID_PN,
                new Object[] { request.getRequest_data().getSubjectId(), request.getRequest_data().getTopicId(), map
                    .get(Parameters.FROM), map.get(Parameters.TO) });

        }
        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map1 = (Map) object;
                String pid = (String) map1.get("pid");
                queryParams.put("pid", pid);
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);

                Map<String, Object> tags = null;

                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Map<String, Object> map2 = (Map) object;
                String pid1 = (String) map2.get("pid");
                queryParams.put("pid", pid1);
                List<Object> readObject2 = dao.readObjects(SibConstants.SqlMapper.SQL_ONE_ANSWER_FORUM, queryParams);

                Map<String, Object> answer = null;

                try {
                    if (readObject2 != null) {
                        for (Object obj : readObject2) {
                            answer = (Map) obj;
                            Iterator<Entry<String, Object>> it = answer.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                Map<String, Object> mymap2 = new HashMap<String, Object>();
                mymap2.put("answer", readObject2);
                resChildObject.add(map1);
                resChildObject.add(mymap2);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        String count = null;
        count = dao.getCount("GET_POST_LIST_PN_COUNT", new Object[] { request.getRequest_data().getSubjectId() });

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getPostListRelatedPN", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostListRelatedPN(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());
        List<Object> readObject = null;
        if (request.getRequest_data().getSubjectId().equals("0")) {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_PN,
                new Object[] { Integer.parseInt(map.get(Parameters.FROM)), Integer.parseInt(map.get(Parameters.TO)) });
        } else if (request.getRequest_data().getTopicId() != null) {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBID_RELATED_PN,
                new Object[] { request.getRequest_data().getSubjectId(), request.getRequest_data().getTopicId(), request
                    .getRequest_data()
                    .getPid(), Integer.parseInt(map.get(Parameters.FROM)), Integer.parseInt(map.get(Parameters.TO)) });
        } else {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_RELATED_PN,
                new Object[] { request.getRequest_data().getPid(), Integer.parseInt(map.get(Parameters.FROM)), Integer
                    .parseInt(map.get(Parameters.TO)) });
        }

        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map1 = (Map) object;
                String pid = "" + map1.get("pid");
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, new Object[] { pid });

                Map<String, Object> tags = null;

                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                resChildObject.add(map1);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        String count = null;

        if (request.getRequest_data().getTopicId() != null) {
            count = dao.getCount(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBID_PN_COUNT,
                new Object[] { request.getRequest_data().getSubjectId(), request.getRequest_data().getTopicId() });
        } else {
            count = dao.getCount(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_PN_COUNT,
                new Object[] { request.getRequest_data().getSubjectId() });
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getPostListSubjectPN", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostListSubjectPN(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        List<Object> readObject = null;
        if (request.getRequest_data().getSubjectId().equals("0")) {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_PN,
                new Object[] { map.get(Parameters.FROM), map.get(Parameters.TO) });
        } else {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBJECTID_PN,
                new Object[] { request.getRequest_data().getSubjectId(), Integer.parseInt(map.get(Parameters.FROM)), Integer
                    .parseInt(map.get(Parameters.TO)) });
        }
        List<List<Object>> resParentObject = new ArrayList<List<Object>>();
        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map1 = (Map) object;
                String pid = "" + map1.get("pid");
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, new Object[] { pid });

                Map<String, Object> tags = null;

                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> map2 = (Map) object;

                List<Object> readObject2 = getOneAnswerWithQuestion(map2);
                Object count = countAnswer(map2);

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                Map<String, Object> mymap2 = new HashMap<String, Object>();
                mymap2.put("answer", readObject2);
                mymap2.put("count", count);
                resChildObject.add(map1);
                resChildObject.add(mymap2);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        String count = null;
        count = dao.getCount(
            SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBJECTID_PN_COUNT,
            new Object[] { request.getRequest_data().getSubjectId() });

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getAnswerListPN", method = RequestMethod.POST)
    public ResponseEntity<Response> getAnswerListPN(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        String sqlMapper = "";
        List<Object> readObject = null;
        if (request.getRequest_data().getPid().equals("0")) {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LIST_PN,
                new Object[] { Integer.parseInt(map.get("from")), Integer.parseInt(map.get("to")) });
        } else {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LIST_SUBID_PN,
                new Object[] { request.getRequest_data().getPid(), Integer.parseInt(map.get("from")), Integer
                    .parseInt(map.get("to")) });
        }
        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map1 = (Map) object;
                String pid = "" + map1.get("pid");
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, new Object[] { pid });

                Map<String, Object> tags = null;

                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                resChildObject.add(map1);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        String count = null;

        count = dao.getCount(
            SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LIST_PN_COUNT,
            new Object[] { request.getRequest_data().getPid() });

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getPostListMobile", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostListMobile(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("subjectId", request.getRequest_data().getSubjectId());
        queryParams.put("from", map.get("from"));
        queryParams.put("to", map.get("to"));
        String sqlMapper = "";
        if (request.getRequest_data().getSubjectId().equals("0")) {
            sqlMapper = SibConstants.SqlMapper.SQL_GET_POST_LIST_PN;
        } else {
            sqlMapper = SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBID_MOBILE;
        }

        List<Object> readObject = null;
        readObject = dao.readObjects(sqlMapper, queryParams);

        String count = null;
        if ("true".equalsIgnoreCase(request.getRequest_data().getTotalCountFlag())) {
            count = dao.getCount(SibConstants.SqlMapper.SQL_GET_POST_LIST_MOBILE_COUNT, new Object[] {});

        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getStudentPostsPN", method = RequestMethod.POST)
    public ResponseEntity<Response> getStudentPostsPN(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("studentId", request.getRequest_data().getUid());
        queryParams.put("from", map.get("from"));
        queryParams.put("to", map.get("to"));

        List<List<Object>> resParentObject = new ArrayList<List<Object>>();
        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_STUDENT_POST_LIST_PN, queryParams);

        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map1 = (Map) object;
                String pid = (String) map1.get("pid");
                queryParams.put("pid", pid);
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);

                Map<String, Object> tags = null;

                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                resChildObject.add(map1);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        String count = null;
        if ("true".equalsIgnoreCase(request.getRequest_data().getTotalCountFlag())) {
            count = dao.getCount(
                SibConstants.SqlMapper.SQL_GET_STUDENT_POST_LIST_PN_COUNT,
                new Object[] { request.getRequest_data().getUid() });
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getPostInfoPN", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostInfoPN(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("pid", request.getRequest_data().getPid());
        queryParams.put("from", map.get("from"));
        queryParams.put("to", map.get("to"));

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_POST_INFO_PN, queryParams);

        List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);

        Map<String, Object> tags = null;

        try {
            if (readObject1 != null) {
                for (Object obj : readObject1) {
                    tags = (Map) obj;
                    Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pairs = it.next();
                        if (pairs.getKey().equals("pid")) {
                            it.remove();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        Map<String, Object> mymap = new HashMap<String, Object>();
        mymap.put("tags", readObject1);

        // TODO : Come back here when comments are needed
        String count = null;
        if ("true".equalsIgnoreCase(request.getRequest_data().getTotalCountFlag())) {
            count = dao
                .getCount(SibConstants.SqlMapper.SQL_GET_POST_INFO_PN_COUNT, new Object[] { request.getRequest_data().getPid() });
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/addPostComment", method = RequestMethod.POST)
    public ResponseEntity<Response> addPostComment(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("author", request.getRequest_data().getAuthor());
        queryParams.put("authorId", request.getRequest_data().getAuthorID());
        queryParams.put("content", request.getRequest_data().getContent());
        queryParams.put("image", request.getRequest_data().getImage());

        boolean status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_SIB_ADD_COMMENT, queryParams);
        Map<String, String> queryParamsIns = null;
        if (status) {
            Map<String, String> queryParams1 = new HashMap<String, String>();
            queryParams1.put("content", request.getRequest_data().getContent());
            queryParams1.put("authorId", request.getRequest_data().getAuthorID());

            List<Object> readObject = null;
            readObject = dao.readObjects(SibConstants.SqlMapper.SQL_SIB_LAST_INSERTED_COMMENT, queryParams1);
            if (readObject != null && readObject.size() > 0) {
                queryParamsIns = new HashMap<String, String>();
                queryParamsIns.put("cid", ((Map) readObject.get(0)).get("cid").toString());
                queryParamsIns.put("pid", request.getRequest_data().getVid());
                boolean flag = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_POST_INSERT_FORUM_COMMENT, queryParamsIns);
            }
        }

        int cid = Integer.valueOf(queryParamsIns.get("cid"));
        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    status,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    cid);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/getPostComments", method = RequestMethod.POST)
    public ResponseEntity<Response> getPostComments(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("pid", request.getRequest_data().getPid());

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_POST_READ_FORUM_COMMENT, queryParams);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/removePost", method = RequestMethod.POST)
	public ResponseEntity<Response> removePost(@RequestBody final RequestData request) {

		if (!AuthenticationFilter.isAuthed(context)) {
			ResponseEntity<Response> entity = new ResponseEntity<Response>(
					new SimpleResponse("" + false, "Authentication required."), HttpStatus.FORBIDDEN);
			return entity;
		}

		Object[] queryParams = { request.getRequest_data().getPid() };
		boolean status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_REMOVE_POST, queryParams);

		if (status) {
			status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_REMOVE_ANSWER_BY_POST, queryParams);
		}
		SimpleResponse reponse = new SimpleResponse("" + status, request.getRequest_data_type(),
				request.getRequest_data_method(), status);
		ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
		return entity;
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/searchPosts", method = RequestMethod.POST)
    public ResponseEntity<Response> searchPosts(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        String value = request.getRequest_data().getContent().trim();
        queryParams.put("content", value);
        queryParams.put("content1", value);

        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_SEARCH_POST, queryParams);
        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map = (Map) object;
                String pid = (String) map.get("pid");
                queryParams.put("pid", pid);
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);

                Map<String, Object> tags = null;

                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                resChildObject.add(map);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/searchPostsInSubcategory", method = RequestMethod.POST)
    public ResponseEntity<Response> searchPostsInSubcategory(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("subcategory", request.getRequest_data().getSubcategory().trim());

        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_SEARCH_POSTS_IN_SUBCATEGORY, queryParams);
        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map = (Map) object;
                String pid = (String) map.get("pid");
                queryParams.put("pid", pid);
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);
                Map<String, Object> tags = null;
                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                resChildObject.add(map);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/searchPostsWithTag", method = RequestMethod.POST)
    public ResponseEntity<Response> searchPostsWithTag(@RequestBody final RequestData request) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map1 = util.getLimit(request.getRequest_data().getPageno(), request.getRequest_data().getLimit());

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("subjectId", request.getRequest_data().getSubjectId());
        queryParams.put("subject", request.getRequest_data().getTag().trim());
        queryParams.put("tag", request.getRequest_data().getTag().trim());
        queryParams.put("content", request.getRequest_data().getTag().trim());
        queryParams.put("from", map1.get("from"));
        queryParams.put("to", map1.get("to"));

        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_SEARCH_POSTS_WITH_TAG, queryParams);

        if (readObject != null) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map = (Map) object;
                String pid = (String) map.get("pid");
                queryParams.put("pid", pid);
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_POST_GET_TAGS, queryParams);

                Map<String, Object> tags = null;
                try {
                    if (readObject1 != null) {
                        for (Object obj : readObject1) {
                            tags = (Map) obj;
                            Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = it.next();
                                if (pairs.getKey().equals("pid")) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }

                Map<String, Object> map2 = (Map) object;
                List<Object> readObject2 = getOneAnswerWithQuestion(map2);
                Object count = countAnswer(map2);

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("tags", readObject1);
                Map<String, Object> mymap2 = new HashMap<String, Object>();
                mymap2.put("answer", readObject2);
                mymap2.put("count", count);
                resChildObject.add(map);
                resChildObject.add(mymap2);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        String count = null;
        count = dao.getCount(
            SibConstants.SqlMapper.SQL_GET_POST_LIST_SUBJECT_PN_COUNT,
            new Object[] { request.getRequest_data().getTag().trim(), request.getRequest_data().getSubjectId() });
        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject,
                                                    count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings("rawtypes")
    @Override
    @RequestMapping(value = "/postAnswer", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Response> postAnswer(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("authorID", request.getRequest_data().getAuthorID());
        queryParams.put("pid", request.getRequest_data().getPid());
        queryParams.put("content", request.getRequest_data().getContent());

        boolean status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_CREATE_ANSWER, queryParams);
        if (status) {
            // increment the numRatings column in sib_videos table
            queryParams.put("pid", request.getRequest_data().getPid());
            status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_SIB_NUM_REPLIES_UPDATE, queryParams);
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    status,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    request.getRequest_data().getPid());
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/editAnswer", method = RequestMethod.POST)
    public ResponseEntity<Response> editAnswer(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("aid", request.getRequest_data().getAid());
        queryParams.put("content", request.getRequest_data().getContent());

        boolean status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_ANSWER_EDIT, queryParams);

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    status,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    request.getRequest_data().getAid());
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/removeAnswer", method = RequestMethod.POST)
    public ResponseEntity<Response> removeAnswer(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("aid", request.getRequest_data().getAid());

        boolean status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_REMOVE_ANSWER, queryParams);

        if (status) {
            status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_REMOVE_ANSWER_LIKE, queryParams);
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    status,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    request.getRequest_data().getAid());
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings("rawtypes")
    @Override
    @RequestMapping(value = "/addAnswerComment", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Response> addAnswerComment(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("author", request.getRequest_data().getAuthor());
        queryParams.put("authorId", request.getRequest_data().getAuthorID());
        queryParams.put("content", request.getRequest_data().getContent());
        queryParams.put("image", null);

        boolean status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_SIB_ADD_COMMENT, queryParams);
        Map<String, String> queryParamsIns = null;
        int cid = 0;
        if (status) {
            Map<String, String> queryParams1 = new HashMap<String, String>();

            queryParams1.put("content", request.getRequest_data().getContent());
            queryParams1.put("authorId", request.getRequest_data().getAuthorID());

            List<Object> readObject = null;
            readObject = dao.readObjects(SibConstants.SqlMapper.SQL_SIB_LAST_INSERTED_COMMENT, queryParams1);
            if (readObject != null && readObject.size() > 0) {
                queryParamsIns = new HashMap<String, String>();
                queryParamsIns.put("cid", ((Map) readObject.get(0)).get("cid").toString());
                queryParamsIns.put("aid", request.getRequest_data().getAid());
                boolean flag = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_SIB_INSERT_ANSWER_COMMENT, queryParamsIns);
                cid = Integer.valueOf(queryParamsIns.get("cid"));
            }
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    status,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    cid);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/searchPostWithAnswersComments", method = RequestMethod.POST)
    public ResponseEntity<Response> searchPostWithAnswersComments(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("pid", request.getRequest_data().getPid());

        List<List<Object>> resParentObject = new ArrayList<List<Object>>();

        List<Object> readObject = null;
        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_SEARCH_POST_ANSWERS, queryParams);
        if (readObject != null && readObject.size() > 0) {
            for (Object object : readObject) {
                List<Object> resChildObject = new ArrayList<Object>();
                Map<String, Object> map = (Map) object;
                String aid = (String) map.get("aid");
                queryParams.put("aid", aid);
                List<Object> readObject1 = dao.readObjects(SibConstants.SqlMapper.SQL_SEARCH_ANSWER_COMMENTS, queryParams);

                Map<String, Object> mymap = new HashMap<String, Object>();
                mymap.put("comments", readObject1);
                resChildObject.add(map);
                resChildObject.add(mymap);
                resParentObject.add(resChildObject);
            }
        }

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    resParentObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @Override
    @RequestMapping(value = "/updateViewQuestion", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Response> updateViewQuestion(@RequestBody final RequestData request) {

        if (!AuthenticationFilter.isAuthed(context)) {
            ResponseEntity<Response> entity = new ResponseEntity<Response>(
                                                                           new SimpleResponse(
                                                                                              "" +
                                                                                              false,
                                                                                              "Authentication required."),
                                                                           HttpStatus.FORBIDDEN);
            return entity;
        }

        Object[] queryParams = {request.getRequest_data().getPid()};

        boolean status = true;
        status = dao.insertUpdateObject(SibConstants.SqlMapper.SQL_UPDATE_VIEW_POST, queryParams);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getSubjectIdWithTopicId", method = RequestMethod.POST)
    public ResponseEntity<Response> getSubjectIdWithTopicId(@RequestBody final RequestData request) {

        List<Object> readObject = null;
        readObject = dao.readObjects(
            SibConstants.SqlMapper.SQL_GET_IDSUBJECT_WITH_IDCATEGORY,
            new Object[] { request.getRequest_data().getTopicId() });

        SimpleResponse reponse = new SimpleResponse(
                                                    "" +
                                                    Boolean.TRUE,
                                                    request.getRequest_data_type(),
                                                    request.getRequest_data_method(),
                                                    readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    public List<Object> getOneAnswerWithQuestion(final Map<String, Object> map) {
        Map<String, String> queryParams = new HashMap<String, String>();
        String pid1 = "" + map.get("pid");
        queryParams.put("pid", pid1);

        List<Object> readObject = null;

        readObject = dao.readObjects(SibConstants.SqlMapper.SQL_ONE_ANSWER_FORUM, queryParams);

        Map<String, Object> answer = null;

        try {
            if (readObject != null) {
                for (Object obj : readObject) {
                    answer = (Map) obj;
                    Iterator<Entry<String, Object>> it = answer.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pairs = it.next();
                        if (pairs.getKey().equals("pid")) {
                            it.remove();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return readObject;
    }

    public List<Object> countAnswer(final Map<String, Object> map) {
        List<Object> readObject = dao
            .readObjects(SibConstants.SqlMapper.SQL_COUNT_ANSWER_POST_INDEX, new Object[] { map.get("pid") });
        return readObject;
    }

    /*
     * This is method get newest question by user id
     */
    @RequestMapping(value = "/getStudentPosted", method = RequestMethod.GET)
    @ResponseBody
    @Override
    public ResponseEntity<Response> getStudentPosted(@RequestParam(value = "uid") final long id,
            @RequestParam final String limit, @RequestParam final String offset,@RequestParam final String orderType,@RequestParam final String oldQid) {

        Object[] queryParams = { };
        Map<String, Object> result_data = new HashMap<String, Object>();
        List<Object> readObject = null;
        String whereClause = "";
        
        if(id != -1l) {
        	whereClause += " AND U.userid = " + id;
        }
		if(Parameters.ANSWERED.equalsIgnoreCase(orderType)){
			whereClause += " AND P.NUMREPLIES > 0";
		}
		if(Parameters.UNANSWERED.equalsIgnoreCase(orderType)){
			whereClause += " AND P.NUMREPLIES = 0";
		}
		
		if (!"-1".equals(oldQid)) {
			whereClause += " AND P.PID > " + oldQid;
		}
		
		whereClause += " ORDER BY P.TIMESTAMP DESC";
		
		if (!StringUtil.isNull(limit)) {
			whereClause += " LIMIT " + limit;
		}
		if (!StringUtil.isNull(offset)) {
			whereClause += " OFFSET " + offset;
		}
        readObject = dao.readObjectsWhereClause(SibConstants.SqlMapper.SQL_GET_STUDENT_POSTED, whereClause,queryParams);
        List<Object> readObjectAnswers = new ArrayList<>();
        result_data.put("question", readObject);
        int length = readObject.size();
        for (int i = 0; i < length; i++) {
            String result = readObject.get(i).toString();
            String temp = result.substring(0, result.indexOf(","));
            long _id = Long.parseLong(temp.substring(temp.lastIndexOf("=") + 1));
            Object objAnwser = getAnswersList(_id, limit, offset);
            if (objAnwser == null) {
                readObjectAnswers.add(null);
            } else {
                readObjectAnswers.add(objAnwser);
            }
        }
        result_data.put("answers", readObjectAnswers);
        SimpleResponse reponse = new SimpleResponse("" + Boolean.TRUE, "posted", "getStudentPosted", result_data);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }
    
    @RequestMapping(value = "/countQuestions", method = RequestMethod.GET)
    @ResponseBody
    @Override
    public ResponseEntity<Response> countQuestions(@RequestParam(value = "uid") final long id, @RequestParam final String orderType) {

        Object[] queryParams = { };
        String whereClause = "";
        if(id != -1l) {
        	whereClause += " AND P.authorID =  " + id;
        }
        if(Parameters.ANSWERED.equalsIgnoreCase(orderType)){
			whereClause += " AND P.NUMREPLIES > 0";
		}
		if(Parameters.UNANSWERED.equalsIgnoreCase(orderType)){
			whereClause += " AND P.NUMREPLIES = 0";
		}
		
        List<Object> readObject = dao.readObjectsWhereClause(SibConstants.SqlMapper.SQL_GET_STUDENT_POSTED_COUNT,whereClause,queryParams);
       
        SimpleResponse reponse = new SimpleResponse("" + Boolean.TRUE, "get", "countQuestions", readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = "/getNewestQuestion")
    @ResponseBody
    public ResponseEntity<Response> getNewestQuestion(@RequestParam(value = "page") final String from,
            @RequestParam(value = "limit") final String limit) {
        CommonUtil util = CommonUtil.getInstance();
        Map<String, String> limitSelect = util.getLimit(from, limit);

        Object[] queryParams = { Integer.parseInt(limitSelect.get("from")), Integer.parseInt(limitSelect.get("to")) };
        List<Object> readObject = dao.readObjects(SibConstants.SqlMapper.SQL_GET_NEWEST_QUESTION, queryParams);
        String count = String.valueOf(readObject.size());
        SimpleResponse response = new SimpleResponse("" + Boolean.TRUE, "get", "getNewestQuestion", readObject, count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(response, HttpStatus.OK);
        return entity;
    }

    public Object getAnswersList(final long id, final String limit, final String offset) {

        CommonUtil util = CommonUtil.getInstance();

        Map<String, String> map = util.getOffset(limit, offset);

        String sqlMapper = "";
        List<Object> readObject = null;
        if (id == 0) {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LIST_PN,
                new Object[] { Integer.parseInt(map.get("limit")), Integer.parseInt(map.get("offset")) });
        } else {
            readObject = dao.readObjects(
                SibConstants.SqlMapper.SQL_GET_POST_ANSWER_LIST_SUBID_PN,
                new Object[] { id, Integer.parseInt(map.get("limit")), Integer.parseInt(map.get("offset")) });
        }
        if (readObject.isEmpty()) {
            return null;
        }

        SimpleResponse reponse = new SimpleResponse("" + Boolean.TRUE, "get answer", "getAnswersList", readObject);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = "/getNewestQuestionBySubject")
    @ResponseBody
    public ResponseEntity<Response> getNewestQuestionBySubject(@RequestParam final long uid, @RequestParam final String limit,
            @RequestParam final String offset) {

        Object[] paramQueryGetSubjects = { uid };
        String subjectIdResult = dao.readObjects(SibConstants.SqlMapper.SQL_GET_SUBJECT_REG, paramQueryGetSubjects).toString();

        String subjects = subjectIdResult.substring(subjectIdResult.indexOf("=") + 1, subjectIdResult.lastIndexOf("}"));

        CommonUtil cmUtils = CommonUtil.getInstance();

        Map<String, String> pageLimit = cmUtils.getOffset(limit, offset);

        Object[] params = { uid, Integer.parseInt(pageLimit.get("limit")), Integer.parseInt(pageLimit.get("offset")) };

        String whereClause = "WHERE P.subjectId IN (" + subjects + ") ORDER BY P.timeStamp DESC LIMIT ? OFFSET ?;";

        List<Object> readObjectTemp = dao
            .readObjectsWhereClause(SibConstants.SqlMapper.SQL_GET_NEWEST_QUESTION_SUBJECT, whereClause, params);
        String count = String.valueOf(readObjectTemp.size());
        SimpleResponse response = new SimpleResponse(
                                                     "" +
                                                     Boolean.TRUE,
                                                     "GET",
                                                     "getNewestQuestionBySubject",
                                                     readObjectTemp,
                                                     count);
        ResponseEntity<Response> entity = new ResponseEntity<Response>(response, HttpStatus.OK);
        return entity;
    }

    public String uploadFile(final MultipartFile uploadfile, final String path) throws FileNotFoundException {

        String filename = "";
        String filepath = "";
        String directory = environment.getProperty(path);
       
        if (directory != null) {
            try {
                RandomString randomName = new RandomString();
                filename = randomName.random();
                
                filepath = Paths.get(directory, filename + "." + "png").toString();
                // Save the file locally
                File file = new File(filepath);
                
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                stream.write(uploadfile.getBytes());
                stream.close();
            }

            catch (Exception e) {
                e.printStackTrace();
            }

        }
        return filename;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @RequestMapping(value = "/getAnswerByQid", method = RequestMethod.POST)
    public ResponseEntity<Response> getAnswerByQid(@RequestBody final RequestData request) {

    	String limit = request.getRequest_data().getLimit();
		String offset = request.getRequest_data().getOffset();
		String qid = request.getRequest_data().getPid();
		String type = request.getRequest_data().getType();
		Object[] queryParams = { qid };

		String entityName = SibConstants.SqlMapper.SQL_GET_ANSWER_BY_QID;

		List<Object> readObject = null;
		String whereClause = "";
		if(StringUtil.isNull(type)){
		
		whereClause += " ORDER BY X.TIMESTAMP DESC";
		}
		else if(Parameters.LIKE.equals(type)){
			whereClause += " BY X.numlike DESC";
		}
		else {//oldest
			whereClause += " BY X.TIMESTAMP  ASC";
		}
		if (!StringUtil.isNull(limit)) {
			whereClause += " LIMIT " + limit;
		}
		if (!StringUtil.isNull(offset)) {
			whereClause += " OFFSET " + offset;
		}
		
		readObject = dao.readObjectsWhereClause(entityName, whereClause, queryParams);

		SimpleResponse reponse = new SimpleResponse("" + Boolean.TRUE, request.getRequest_data_type(),
				request.getRequest_data_method(), readObject);
		ResponseEntity<Response> entity = new ResponseEntity<Response>(reponse, HttpStatus.OK);
		return entity;
        }
    
	private String getPathFile(final String oldImagePath, final String oldImagePathEdited, final String pathFile) {
		String filePathEdited = "";
		
		if (!StringUtil.isNull(oldImagePath)) {
			for (String path : oldImagePath.split(";")) {
				if (!StringUtil.isNull(oldImagePathEdited)) {
					for (String pathEdit : oldImagePathEdited.split(";")) {
						if (path.equals(pathEdit)) {
							filePathEdited += path;
							filePathEdited += ";";
						} 
//						else {
//							delefile(pathEdit,pathFile);
//						}
					}
				}
				else {
					return oldImagePath+";";
				}
			}
		}

		return filePathEdited;
	}
	private void delefile(final String filename,final String path) {
		String directory = environment.getProperty(path);
		 File file = new File(Paths.get(directory, filename + "." + "png").toString());
		 file.delete();
	}
	
	private String fixFilePath(String str) {
		String result = "";
		if (StringUtil.isNull(str)) {
			return "";
		}
		String path = "";
		for (int i = 0; i < str.split(";").length; i++) {
			path = str.split(";")[i];
			if (!StringUtil.isNull(path)) {
				result += path;
				if (i < str.split(";").length-1) {
					result += ";";
				}
			}
			
		}
		return result;
	}
	
	private String validateFileImage(MultipartFile[] files) {
		String error = "";
		String name = "";
		String sample = environment.getProperty("file.upload.image.type");
		String limitSize = environment.getProperty("file.upload.image.size");
		String maxLength = environment.getProperty("file.upload.image.length");
		long totalSize = 0;
		if (files != null && files.length > 0) {
			if (files.length > Integer.parseInt(maxLength)) {
				return "You only upload " + maxLength +" image";
			}
			for (MultipartFile file : files) {
				name = file.getOriginalFilename();
				if (StringUtil.isNull(name)) {
					String nameExt = FilenameUtils.getExtension(name.toLowerCase());
					boolean status = sample.contains(nameExt);
					if (!status) {
						return "Error Format";
					}
				}
				totalSize += file.getSize();
			}
			if(totalSize > Long.parseLong(limitSize)){
				error = "File over 10M";
			}
		}
		return error;
	}
}
