package vn.webcapnuoc.hddt.controller.admin;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@RequestMapping(value = "/admin")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class MainController extends AbstractController {
	@Autowired RestAPIUtility restAPI;
	@RequestMapping(value = { "/main", "/main/{transaction}/{method}",
			"/main/{transaction}/{method}/{param1}" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String main(@PathVariable(name = "transaction", required = false, value = "") String transaction,
			@PathVariable(name = "method", required = false, value = "") String method,
			@PathVariable(name = "param1", required = false, value = "") String param1, HttpServletRequest request) throws Exception {
		
		if (null == transaction)
			transaction = "";
		if (null == method)
			method = "";
		if (null == param1)
			param1 = "";
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		
		if(cup==null) {
			return "redirect:/admin";
		}
		request.setAttribute("transaction", transaction);
		request.setAttribute("method", method);

		
		if (!("".equals(transaction) || "".equals(method))) {
			request.setAttribute(Constants.HAVE_ACTION_NAME, "OK");
			if (!"".equals(param1))
				return "forward:/admin/" + transaction + "/" + method + "/" + param1;
			return "forward:/admin/" + transaction + "/" + method;
		}	

		
		//THONG TIN LOGO
		BaseDTO dtoRes = new BaseDTO(request);
		Msg msg = dtoRes.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		HashMap<String, String> hInput = new HashMap<>();
		msg.setObjData(hInput);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/main/getLogo",cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		JsonNode rows = null;
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
			for(JsonNode row: rows) {
			request.setAttribute("TitleLogo_", commons.getTextJsonNode(row.at("/Title")));
			request.setAttribute("ImageLogo_", commons.getTextJsonNode(row.at("/ImageLogo")));
			}
		}
	}
		//THONG TIN HEADER
		 dtoRes = new BaseDTO(request);
		 msg = dtoRes.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		 hInput = new HashMap<>();
		msg.setObjData(hInput);
		 root = new JSONRoot(msg);
		 rsp = restAPI.callAPINormal("/main/getHeader",cup.getLoginRes().getToken(), HttpMethod.POST, root);
		 rspStatus = rsp.getResponseStatus();
		 rows = null;
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
			for(JsonNode row: rows) {
			request.setAttribute("Phone_sp", commons.getTextJsonNode(row.at("/PhoneSP")));
			request.setAttribute("Email_sp", commons.getTextJsonNode(row.at("/EmailSP")));
			}
		}
	}
		//THONG TIN TITLE
		 dtoRes = new BaseDTO(request);
		 msg = dtoRes.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		 hInput = new HashMap<>();
		msg.setObjData(hInput);
		 root = new JSONRoot(msg);
		 rsp = restAPI.callAPINormal("/main/getTitle",cup.getLoginRes().getToken(), HttpMethod.POST, root);
		 rspStatus = rsp.getResponseStatus();
		 rows = null;
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
			for(JsonNode row: rows) {
			request.setAttribute("Title_user", commons.getTextJsonNode(row.at("/Title_user")));
			request.setAttribute("Title_admin", commons.getTextJsonNode(row.at("/Title_admin")));
			}
		}
	}
		 
	return "admin/main";	
	}
}
