package vn.webcapnuoc.hddt.controller.user;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;

@Controller
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
@RequestMapping({
	"/forgotpass"
})
public class ForgotPassController extends AbstractController{
	private static final Logger log = LogManager.getLogger(ForgotPassController.class);
	@Autowired RestAPIUtility restAPI;
	@Autowired RestTemplate restTemplate;
	
	private String j_username;
	private String j_email;
	private String j_captcha;
	
	@RequestMapping(value = {"/", ""})
	public String forgotpass(Model model, HttpServletRequest req) throws Exception {
		req.setAttribute("_header", Constants.PREFIX_TITLE + " - QUÊN MẬT KHẨU");
		BaseDTO dtoRes = new BaseDTO();
		Msg msg = dtoRes.createMsgPass();
		HashMap<String, String> hInput = new HashMap<>();
		msg.setObjData(hInput);
		JSONRoot root = new JSONRoot(msg);

		return "admin/user/forgotpass";
	}
	
	public BaseDTO checkDataToAccept(Locale locale, HttpServletRequest req, HttpSession session) throws Exception{
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);
		
		j_username = null == req.getParameter("j_username") ? "" : req.getParameter("j_username").replaceAll("\\s", "");
		j_email = null == req.getParameter("j_email") ? "" : req.getParameter("j_email").replaceAll("\\s", "");
		j_captcha = null == req.getParameter("j_captcha") ? "" : req.getParameter("j_captcha").trim();
		
		if("".equals(j_username)) {
			dto.setErrorCode(1);
			dto.getErrorMessages().add("Vui lòng nhập vào tên đăng nhập.");
		}
		if("".equals(j_email)) {
			dto.setErrorCode(1);
			dto.getErrorMessages().add("Vui lòng nhập vào email người dùng.");
		}
		if(dto.getErrorCode() == 0) {
			String captchaSession = null == session.getAttribute(Constants.SESSION_CAPTCHA)? "": (String) session.getAttribute(Constants.SESSION_CAPTCHA);
			if("".equals(j_captcha) || !j_captcha.equals(captchaSession)) {
				dto.setErrorCode(1);
				dto.setResponseData("Mã captcha không đúng. Vui lòng nhập lại.");
			}
		}
		
		return dto;
	}
	
	
	
	@RequestMapping(value = "/checkDataToSend",  produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public BaseDTO execCheckDataToSave(Locale locale, HttpServletRequest req, HttpSession session){
		BaseDTO dto = new BaseDTO();
		try {
			dto = checkDataToAccept(locale, req, session);
			if(0 != dto.getErrorCode()) {
				if(null != dto.getResponseData() && !"".equals(dto.getResponseData())) {
				}else {
					dto.setErrorCode(999);
					dto.setResponseData(Constants.MAP_ERROR.get(999));
					return dto;
				}
				
				return dto;
			}
			
			dto.setErrorCode(0);
		}catch(Exception e) {
			dto.setErrorCode(999);
			dto.setResponseData(Constants.MAP_ERROR.get(999));
			return dto;
		}
		
		return dto;
	}
	
	
	
	@RequestMapping(value = {"/getToken"},  produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.POST})
	@ResponseBody
	public BaseDTO execGetToken(Locale locale, HttpServletRequest req, HttpSession session) {
				
		BaseDTO dtoRes = new BaseDTO();		
			try {		
			dtoRes = checkDataToAccept(locale, req, session);
			if(0 != dtoRes.getErrorCode()) {
				if(null != dtoRes.getResponseData() && !"".equals(dtoRes.getResponseData())) {
				}else {
					dtoRes.setErrorCode(999);
					dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
					return dtoRes;
				}
				return dtoRes;
			}

		dtoRes = new BaseDTO(req);
		Msg msg = dtoRes.createMsgPass();
		HashMap<String, String> hData = new HashMap<>();
		hData.put("UserName", j_username);
		hData.put("Email", j_email);

		msg.setObjData(hData);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPIPass("/forgotpass/crud", HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			dtoRes.setErrorCode(0);			
			dtoRes.setResponseData(rspStatus.getErrorDesc());
		}else {
			dtoRes.setErrorCode(rspStatus.getErrorCode());
			dtoRes.setResponseData(rspStatus.getErrorDesc());
		}
		}catch(Exception e) {
			dtoRes.setErrorCode(999);
			dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
			return dtoRes;
		}
		return dtoRes;	
	}
	
	@RequestMapping(value = "/confirmPassword", method = {RequestMethod.POST})
	public String confirmPassword(Locale locale, HttpServletRequest req, HttpSession session) {
		req.setAttribute("_header_", "Xác nhận lấy lại mật khẩu");
		req.setAttribute("tokenConfirm", req.getParameter("tokenConfirm"));		
		return "admin/user/resetPasswordConfirm";
	}
	
	private String otpConfirm;
	private String tokenConfirm;
	
	public BaseDTO checkDataToConfirm(Locale locale, HttpServletRequest req, HttpSession session) throws Exception{
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);
		
		otpConfirm = null == req.getParameter("otpConfirm")? "": req.getParameter("otpConfirm").replaceAll("\\s", "");
		tokenConfirm = null == req.getParameter("tokenConfirm")? "": req.getParameter("tokenConfirm").replaceAll("\\s", "");
		
		if("".equals(otpConfirm)) {
			dto.setErrorCode(1);
			dto.getErrorMessages().add("Vui lòng nhập vào mã xác thực.");
		}
		if("".equals(tokenConfirm)) {
			dto.setErrorCode(1);
			dto.getErrorMessages().add("Token xác nhận mật khẩu không hợp lệ.");
		}
		
		return dto;
	}
	
	@RequestMapping(value = "/checkDataToConfirm",  produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public BaseDTO execCheckDataToConfirm(Locale locale, HttpServletRequest req, HttpSession session) {
		BaseDTO dto = new BaseDTO();
		try {
			dto = checkDataToConfirm(locale, req, session);
			if(0 != dto.getErrorCode()) {
				if(null != dto.getResponseData() && !"".equals(dto.getResponseData())) {
				}else {
					dto.setErrorCode(999);
					dto.setResponseData(Constants.MAP_ERROR.get(999));
					return dto;
				}
				
				return dto;
			}
			
			dto.setErrorCode(0);
		}catch(Exception e) {
			dto.setErrorCode(999);
			dto.setResponseData(Constants.MAP_ERROR.get(999));
			return dto;
		}
		return dto;
	}
	
	@RequestMapping(value = "/getPassword",  produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public BaseDTO execGetPassword(Locale locale, HttpServletRequest req, HttpSession session) {
		BaseDTO dtoRes = new BaseDTO();
		try {
			dtoRes = checkDataToConfirm(locale, req, session);
			if(0 != dtoRes.getErrorCode()) {
				if(null != dtoRes.getResponseData() && !"".equals(dtoRes.getResponseData())) {
				}else {
					dtoRes.setErrorCode(999);
					dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
					return dtoRes;
				}
				return dtoRes;
			}
			
			dtoRes = new BaseDTO(req);
			Msg msg = dtoRes.createMsgPass();

			HashMap<String, String> hInput = new HashMap<>();
			hInput.put("token", tokenConfirm);
			hInput.put("otp", otpConfirm);

			msg.setObjData(hInput);
			JSONRoot root = new JSONRoot(msg);
			MsgRsp rsp = restAPI.callAPIPass("/forgotpass/check-token", HttpMethod.POST, root);
			MspResponseStatus rspStatus = rsp.getResponseStatus();
			if(rspStatus.getErrorCode() == 0) {
				dtoRes.setErrorCode(0);			
				dtoRes.setResponseData("Lấy mật khẩu thành công. <br/>Vui lòng kiểm tra Email để lấy mật khẩu mới.");
			}else {
				dtoRes.setErrorCode(rspStatus.getErrorCode());
				dtoRes.setResponseData(rspStatus.getErrorDesc());
			}					
			
		}catch(Exception e) {
			dtoRes.setErrorCode(999);
			dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
			return dtoRes;
		}
		return dtoRes;
	}
	
	
}
