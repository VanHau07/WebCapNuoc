package vn.webcapnuoc.hddt.controller.admin.infoAccount;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
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
import vn.webcapnuoc.hddt.dto.LoginRes;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@RequestMapping("/admin/viewProfile")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class ProfileController extends AbstractController {
	@Autowired RestAPIUtility restAPI;
	
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	@RequestMapping(value = "/init", method = { RequestMethod.POST, RequestMethod.GET })
	public String init(Locale locale, HttpServletRequest req, HttpSession session
			, @RequestAttribute(name = "transaction", value = "", required = false) String transaction
			, @RequestAttribute(name = "method", value = "", required = false) String method
			) throws Exception{
		errorCode = "";
		errorDesc = "";
	
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
	
		LoginRes issu = cup.getLoginRes();
	

		BaseDTO baseDTO = new BaseDTO(req);
		Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		HashMap<String, String> hData = new HashMap<>();
		msg.setObjData(hData);

		
		
		_id = cup.getLoginRes().getUserId();
		String header = "Thêm mới";
		String action = "CREATE";
		boolean isEdit = false;	
		
		
		//THONG TIN LOGO
		BaseDTO dtoRes = new BaseDTO(req);
		 msg = dtoRes.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
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
				req.setAttribute("TitleLogo_", commons.getTextJsonNode(row.at("/Title")));
				req.setAttribute("ImageLogo_", commons.getTextJsonNode(row.at("/ImageLogo")));
			}
		}
	}
		//THONG TIN HEADER
		 dtoRes = new BaseDTO(req);
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
				req.setAttribute("Phone_sp", commons.getTextJsonNode(row.at("/PhoneSP")));
				req.setAttribute("Email_sp", commons.getTextJsonNode(row.at("/EmailSP")));
			}
		}
	}
		//THONG TIN TITLE
		 dtoRes = new BaseDTO(req);
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
				req.setAttribute("Title_user", commons.getTextJsonNode(row.at("/Title_user")));
				req.setAttribute("Title_admin", commons.getTextJsonNode(row.at("/Title_admin")));
			}
		}
	}
		req.setAttribute("_header_", header);
		req.setAttribute("_action_", action);
		req.setAttribute("_isedit_", isEdit);
		req.setAttribute("_id", _id);
		
			inquiry(cup, locale, req, session, _id, action, transaction, method);
		
		return "admin/user/viewprofile";
	}

	private void inquiry(CurrentUserProfile cup, Locale locale, HttpServletRequest req, HttpSession session, String _id , String action, String transaction, String method) throws Exception{
		if("".equals(_id)) {
			errorCode = "NOT FOUND";
			errorDesc = "Không tìm thấy thông tin người dùng.";
			return;
		}
		BaseDTO baseDTO = new BaseDTO(req);
		Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		HashMap<String, String> hData = new HashMap<>();
		msg.setObjData(hData);
		
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/profile/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			
			req.setAttribute("UserName", commons.getTextJsonNode(jsonData.at("/UserName")));
			req.setAttribute("FullName", commons.getTextJsonNode(jsonData.at("/FullName")));
			req.setAttribute("Phone", commons.getTextJsonNode(jsonData.at("/Phone")));
			req.setAttribute("Phone", commons.getTextJsonNode(jsonData.at("/Phone")));
			req.setAttribute("Email", commons.getTextJsonNode(jsonData.at("/Email")));
			
		}else {
			errorDesc = rspStatus.getErrorDesc();
		}
	}

	
}
