package vn.webcapnuoc.hddt.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

import vn.webcapnuoc.hddt.user.dao.PhotoDAO;
import vn.webcapnuoc.hddt.utility.Commons;

@RestController
@RequestMapping(value = "/tac-pham-anh")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class PhotoUserController {
	Commons commons = new Commons();
	@Autowired private PhotoDAO dao;
	
	
	
	@RequestMapping(value = "/getPhoto", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getPhoto(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.getPhoto(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	
	@RequestMapping(value = "/detail/{_id}", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> detail(@RequestBody JSONRoot jsonRoot
			, @PathVariable(name = "_id", required = false) String _id) throws Exception{
		MsgRsp rsp = dao.detail(jsonRoot, _id);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}

}

