package vn.webcapnuoc.hddt.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

import vn.webcapnuoc.hddt.admin.dao.MainDAO;
import vn.webcapnuoc.hddt.utility.Commons;

@RestController
@RequestMapping(value = "/main")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class MainController {
	Commons commons = new Commons();
	@Autowired private MainDAO dao;
	
	@RequestMapping(value = "/getLogo", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getLogo(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.getLogo(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	@RequestMapping(value = "/getHeader", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getHeader(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.getHeader(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	
	@RequestMapping(value = "/getTitle", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getTitle(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.getTitle(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	@RequestMapping(value = "/list", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> list(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.list(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	
}

