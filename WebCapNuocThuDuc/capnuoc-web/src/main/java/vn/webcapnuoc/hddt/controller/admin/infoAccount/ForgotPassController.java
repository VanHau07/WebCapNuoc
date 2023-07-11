package vn.webcapnuoc.hddt.controller.admin.infoAccount;
//package com.ses.website.admin.controller;
//
//import java.security.Principal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//import org.bson.Document;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Scope;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.ses.website.admin.dao.ForgotPassDAO;
//import com.ses.website.admin.dao.HeaderDAO;
//import com.ses.website.client.dao.IndexDAO;
//import com.ses.website.controller.AbstractController;
//import com.ses.website.dto.BaseDTO;
//import com.ses.website.dto.CurrentUserProfile;
//import com.ses.website.utility.Constants;
//
//
//
//@Controller
//@RequestMapping(value = "/forgotpass")
//@Scope(value = WebApplicationContext.SCOPE_REQUEST)
//public class ForgotPassController extends AbstractController {
//	@Autowired
//	@Qualifier(value = "AdminForgotPass")
//	private ForgotPassDAO dao;
//	@Autowired
//	@Qualifier("ClientIndex")
//	private IndexDAO dao1;
//	@Autowired
//	@Qualifier("AdminHeader")
//	private HeaderDAO dao2;
//	private String j_username;
//	private String j_email;
//	private String j_captcha;
//	private String tk_username;
//	private String certificate;
//
//	@RequestMapping(value = "", method = { RequestMethod.POST, RequestMethod.GET })
//	public String main(Locale locale, Principal principal, HttpServletRequest req) throws Exception {
//		req.setAttribute("title", "Quên mật khẩu");
//		List<HashMap<String, Object>> rows8 = null;
//		Document r8 = null;
//
//		try {
//			r8 = dao2.dashboardInfoHeader();
//			req.setAttribute("Phone_sp", r8.get("PhoneSP", ""));
//			req.setAttribute("Email_sp", r8.get("EmailSP", ""));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//////
//		Document r9 = null;
//		try {
//			r9 = dao1.dashboardInfoTitle();
//
//			req.setAttribute("Title_user", r9.get("Title_user", ""));
//			req.setAttribute("Title_admin", r9.get("Title_admin", ""));
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return "admin/user/forgotpass";
//	}
//
//	public BaseDTO checkDataToAccept(Locale locale, HttpServletRequest req, HttpSession session) throws Exception {
//		BaseDTO dto = new BaseDTO();
//		dto.setErrorCode(0);
//
//		j_username = null == req.getParameter("j_username") ? "" : req.getParameter("j_username").replaceAll("\\s", "");
//		j_email = null == req.getParameter("j_email") ? "" : req.getParameter("j_email").replaceAll("\\s", "");
//		j_captcha = null == req.getParameter("j_captcha") ? "" : req.getParameter("j_captcha").trim();
//
//		if ("".equals(j_username)) {
//			dto.setErrorCode(1);
//			dto.getErrorMessages().add("Vui lòng nhập vào tên đăng nhập.");
//		}
//		if ("".equals(j_email)) {
//			dto.setErrorCode(1);
//			dto.getErrorMessages().add("Vui lòng nhập vào email người dùng.");
//		}
//		if (dto.getErrorCode() == 0) {
//			String captchaSession = null == session.getAttribute(Constants.SESSION_CAPTCHA) ? ""
//					: (String) session.getAttribute(Constants.SESSION_CAPTCHA);
//			if ("".equals(j_captcha) || !j_captcha.equals(captchaSession)) {
//				dto.setErrorCode(1);
//				dto.setResponseData("Mã captcha không đúng. Vui lòng nhập lại.");
//			}
//		}
//
//		return dto;
//	}
//
//	@RequestMapping(value = "/checkDataToSend", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
//	@ResponseBody
//	public BaseDTO execCheckDataToSave(Locale locale, HttpServletRequest req, HttpSession session) {
//		BaseDTO dto = new BaseDTO();
//		try {
//			dto = checkDataToAccept(locale, req, session);
//			if (0 != dto.getErrorCode()) {
//				if (null != dto.getResponseData() && !"".equals(dto.getResponseData())) {
//				} else {
//					dto.setErrorCode(999);
//					dto.setResponseData(Constants.MAP_ERROR.get(999));
//					return dto;
//				}
//
//				return dto;
//			}
//
//			dto.setErrorCode(0);
//		} catch (Exception e) {
//			dto.setErrorCode(999);
//			dto.setResponseData(Constants.MAP_ERROR.get(999));
//			return dto;
//		}
//
//		return dto;
//	}
//
//	@RequestMapping(value = { "/getToken" }, produces = MediaType.APPLICATION_JSON_VALUE, method = {
//			RequestMethod.POST })
//	@ResponseBody
//	public BaseDTO execGetToken(Locale locale, HttpServletRequest req, HttpSession session) {
//
//		BaseDTO dtoRes = new BaseDTO();
//		try {
//			CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
//			dtoRes = checkDataToAccept(locale, req, session);
//			if (0 != dtoRes.getErrorCode()) {
//				if (null != dtoRes.getResponseData() && !"".equals(dtoRes.getResponseData())) {
//				} else {
//					dtoRes.setErrorCode(999);
//					dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
//					return dtoRes;
//				}
//				return dtoRes;
//			}
//
//			dtoRes = new BaseDTO(req);
//			HashMap<String, Object> hData = new HashMap<String, Object>();
//			hData.put("UserName", j_username);
//			hData.put("Email", j_email);
//
//			String actionCode = Constants.MSG_ACTION_CODE.CREATED;
//			BaseDTO res = dao.check_mail(cup, actionCode, hData);
//			if (0 == res.getErrorCode()) {
//				dtoRes = new BaseDTO();
//				dtoRes.setErrorCode(0);
//				dtoRes.setResponseData(res.getErrorDesc());
//			} else {
//				dtoRes = new BaseDTO();
//				dtoRes.setErrorCode(res.getErrorCode());
//				dtoRes.setResponseData(res.getErrorDesc());
//			}
//		} catch (Exception e) {
//			dtoRes.setErrorCode(999);
//			dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
//			return dtoRes;
//		}
//		return dtoRes;
//	}
//
//	@RequestMapping(value = "/confirmPassword", method = { RequestMethod.POST })
//	public String confirmPassword(Locale locale, HttpServletRequest req, HttpSession session) {
//		req.setAttribute("_header_", "Xác nhận lấy lại mật khẩu");
//		req.setAttribute("tokenConfirm", req.getParameter("tokenConfirm"));
//		return "admin/user/resetPasswordConfirm";
//	}
//
//	private String otpConfirm;
//	private String tokenConfirm;
//
//	public BaseDTO checkDataToConfirm(Locale locale, HttpServletRequest req, HttpSession session) throws Exception {
//		BaseDTO dto = new BaseDTO();
//		dto.setErrorCode(0);
//
//		otpConfirm = null == req.getParameter("otpConfirm") ? "" : req.getParameter("otpConfirm").replaceAll("\\s", "");
//		tokenConfirm = null == req.getParameter("tokenConfirm") ? ""
//				: req.getParameter("tokenConfirm").replaceAll("\\s", "");
//
//		if ("".equals(otpConfirm)) {
//			dto.setErrorCode(1);
//			dto.getErrorMessages().add("Vui lòng nhập vào mã xác thực.");
//		}
//		if ("".equals(tokenConfirm)) {
//			dto.setErrorCode(1);
//			dto.getErrorMessages().add("Token xác nhận mật khẩu không hợp lệ.");
//		}
//
//		return dto;
//	}
//
//	@RequestMapping(value = "/checkDataToConfirm", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
//	@ResponseBody
//	public BaseDTO execCheckDataToConfirm(Locale locale, HttpServletRequest req, HttpSession session) {
//		BaseDTO dto = new BaseDTO();
//		try {
//			dto = checkDataToConfirm(locale, req, session);
//			if (0 != dto.getErrorCode()) {
//				if (null != dto.getResponseData() && !"".equals(dto.getResponseData())) {
//				} else {
//					dto.setErrorCode(999);
//					dto.setResponseData(Constants.MAP_ERROR.get(999));
//					return dto;
//				}
//
//				return dto;
//			}
//
//			dto.setErrorCode(0);
//		} catch (Exception e) {
//			dto.setErrorCode(999);
//			dto.setResponseData(Constants.MAP_ERROR.get(999));
//			return dto;
//		}
//		return dto;
//	}
//
//	@RequestMapping(value = "/getPassword", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
//	@ResponseBody
//	public BaseDTO execGetPassword(Locale locale, HttpServletRequest req, HttpSession session) {
//		BaseDTO dtoRes = new BaseDTO();
//		try {
//			dtoRes = checkDataToConfirm(locale, req, session);
//			CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
//			if (0 != dtoRes.getErrorCode()) {
//				if (null != dtoRes.getResponseData() && !"".equals(dtoRes.getResponseData())) {
//				} else {
//					dtoRes.setErrorCode(999);
//					dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
//					return dtoRes;
//				}
//				return dtoRes;
//			}
//
//			dtoRes = new BaseDTO(req);
//
//			HashMap<String, String> hInput = new HashMap<>();
//			hInput.put("token", tokenConfirm);
//			hInput.put("otp", otpConfirm);
//
//			String actionCode = Constants.MSG_ACTION_CODE.CREATED;
//			BaseDTO res = dao.check_token(cup, actionCode, hInput);
//			if (0 == res.getErrorCode()) {
//				dtoRes = new BaseDTO();
//				dtoRes.setErrorCode(0);
//				dtoRes.setResponseData("Lấy mật khẩu thành công. <br/>Vui lòng kiểm tra Email để lấy mật khẩu mới.");
//			} else {
//				dtoRes = new BaseDTO();
//				dtoRes.setErrorCode(res.getErrorCode());
//				dtoRes.setResponseData(res.getErrorDesc());
//			}
//		} catch (Exception e) {
//			dtoRes.setErrorCode(999);
//			dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
//			return dtoRes;
//		}
//		return dtoRes;
//	}
//
//}
