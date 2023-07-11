package vn.webcapnuoc.hddt.controller.admin.infoAccount;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;

import cn.apiclub.captcha.Captcha;
import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.dto.LoginRes;
import vn.webcapnuoc.hddt.model.Users;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.CaptchaUtil;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@RequestMapping("/admin/changepass")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class ChangePassController extends AbstractController {
	@Autowired RestAPIUtility restAPI;
	
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	private String userName;
	private String pw;
	private String pw1;
	private String pw2;
	private int kt = 0;

	private String bien;
	private void getCaptcha(Users user) {
		Captcha captcha = CaptchaUtil.createCaptcha(240, 70);
		user.setHiddenCaptcha(captcha.getAnswer());
		user.setCaptcha("");
		user.setRealCaptcha(CaptchaUtil.encodeCaptcha(captcha));

	}
	
	@RequestMapping(value = "/init", method = {RequestMethod.GET })
	public String init(Users users,Locale locale, HttpServletRequest req, HttpSession session
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
		kt = 0;
		getCaptcha(users);
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
		req.setAttribute("userName", cup.getUsername());
		
			inquiry(cup, locale, req, session, _id, action, transaction, method);
		
		return "admin/user/changepass";
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
			req.setAttribute("Password", commons.getTextJsonNode(jsonData.at("/Password")));
			
		}else {
			errorDesc = rspStatus.getErrorDesc();
		}
	}

	@RequestMapping(value = "/init", method = { RequestMethod.POST })
	public String main(Model model, Users users, LoginRes us, HttpServletRequest req) throws Exception {
		req.setAttribute("title", "Thay đổi mật khẩu");
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		// us = cup.getLoginRes();
		req.setAttribute("userName", cup.getUsername());
		us.setPassword(cup.getPassword());
		int check = 0;
		bien = commons.getParameterFromRequest(req, "bien").replaceAll("\\s", "");
		if(bien.equals("")) {
			check = 0;
		}else {
			check = Integer.parseInt(bien);
		}
		if (check > 3) {
			
			return "redirect:/admin/logout";
		} else {
			if (users.getCaptcha().equals(users.getHiddenCaptcha())) {
				userName = cup.getUsername();
				
				_id = cup.getLoginRes().getUserId();
				BaseDTO baseDTO = new BaseDTO(req);
				Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
				HashMap<String, String> hData = new HashMap<>();
				msg.setObjData(hData);
				
				JSONRoot root = new JSONRoot(msg);
				MsgRsp rsp = restAPI.callAPINormal("/profile/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
				MspResponseStatus rspStatus = rsp.getResponseStatus();
				String pass_ = "";
				if(rspStatus.getErrorCode() == 0) {
					JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());				
					//req.setAttribute("UserName", commons.getTextJsonNode(jsonData.at("/UserName")));
					pass_ =commons.getTextJsonNode(jsonData.at("/Password"));
					
				}
				pw = commons.getParameterFromRequest(req, "pw");
				pw1 = commons.getParameterFromRequest(req, "pw1");
				pw2 = commons.getParameterFromRequest(req, "pw2");
				String pass = pass_;
				String pwuserinput = commons.generateSHA(userName + pw, false).toUpperCase();			
				String mkc2 = "";
				if(pwuserinput.length() < 128) {
					 mkc2 = 0 + pwuserinput;
				}
				else {
					mkc2 = 	pwuserinput;
				}
				boolean checkPassword = pass.equals(mkc2);
				if (!checkPassword) {
					model.addAttribute("message", "Mật khẩu cũ không đúng!");			
					kt = 1 + check;					
					req.setAttribute("bien", kt);
					getCaptcha(users);

				} else {
					if (!pw1.equals(pw2)) {
						req.setAttribute("message", "Xác nhận mật khẩu không trùng khớp!");
						kt = 1 + check;					
						req.setAttribute("bien", kt);
						getCaptcha(users);

					} else {
						// String serverinput = commons.encryptThisString(userName + pw1);
						String serverinput = commons.generateSHA(userName + pw1, false).toUpperCase();
						BaseDTO dtoRes = new BaseDTO();
						String actionCode = Constants.MSG_ACTION_CODE.MODIFY;
						dtoRes = new BaseDTO(req);					
//						hData.put("UserName", userName);
						hData.put("Password", serverinput);
						msg.setObjData(hData);
						
						
						msg.setObjData(hData);						
						 root = new JSONRoot(msg);
						 rsp = restAPI.callAPINormal("/profile/changepass/", cup.getLoginRes().getToken(), HttpMethod.POST, root);
						rspStatus = rsp.getResponseStatus();
						if (0 == rspStatus.getErrorCode()) {
							dtoRes = new BaseDTO();
							dtoRes.setErrorCode(0);
							dtoRes.setResponseData(rspStatus.getErrorDesc());
							getCaptcha(users);
							return "redirect:/admin/logout";
						} else {
							dtoRes = new BaseDTO();
							dtoRes.setErrorCode(rspStatus.getErrorCode());
							dtoRes.setResponseData(rspStatus.getErrorDesc());
							getCaptcha(users);
						}
					}

				}
				//THONG TIN LOGO
				BaseDTO dtoRes = new BaseDTO(req);
				 msg = dtoRes.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
				HashMap<String, String> hInput = new HashMap<>();
				msg.setObjData(hInput);
				 root = new JSONRoot(msg);
				 rsp = restAPI.callAPINormal("/main/getLogo",cup.getLoginRes().getToken(), HttpMethod.POST, root);
				 rspStatus = rsp.getResponseStatus();
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
				return "admin/user/changepass";

			} else {
				req.setAttribute("message", "Captcha không khớp. Vui lòng nhập lại");
				kt = 1 + check;					
				req.setAttribute("bien", kt);
				getCaptcha(users);
				//THONG TIN LOGO
				BaseDTO dtoRes = new BaseDTO(req);
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
				return "admin/user/changepass";
			}

		}
	}
	
	
}
