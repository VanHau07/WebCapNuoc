package vn.webcapnuoc.hddt.controller.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.dto.CurrentUserRole;
import vn.webcapnuoc.hddt.dto.LoginRes;
import vn.webcapnuoc.hddt.dto.req.UserLoginReq;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
@RequestMapping("/admin")
public class LoginController extends AbstractController{
	@Autowired private RestAPIUtility restAPIUtility;
	private static final Logger log = LogManager.getLogger(LoginController.class);
	@Autowired RestAPIUtility restAPI;
	@RequestMapping(value = {"", "/"}, method = {RequestMethod.GET, RequestMethod.POST})
	public String login(Model model, HttpServletRequest request, HttpSession session, 
			@ModelAttribute("logout") String isLogout,
			@ModelAttribute("message") String message) throws Exception {
		HashMap<String, Object> hTmp = null;
		Document r10 = null;
		String messageLogin = "";
		List<HashMap<String, Object>> rowsL = null;
		if("true".equals(isLogout)) {
			messageLogin = "Bạn vừa đăng xuất. Vui lòng đăng nhập lại để thực hiện giao dịch.";
		}else if(null != message && !"".equals(message)) {
			messageLogin = message;
		}
		if(!"".equals(messageLogin)) {
			request.setAttribute("messageLogin", messageLogin);
		}
		request.setAttribute("_header", Constants.PREFIX_TITLE + " - Đăng nhập");
	
		BaseDTO dtoRes = new BaseDTO();
		Msg msg = dtoRes.createMsgPass();
		HashMap<String, String> hInput = new HashMap<>();
		msg.setObjData(hInput);
		JSONRoot root = new JSONRoot(msg);
		
		//THONG TIN LOGO
				MsgRsp rsp = restAPI.callAPIPass("/index/getLogo", HttpMethod.POST, root);
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
		return "admin/user/login";
	}
	

	
	@RequestMapping(value = "/login/authenticate", method = RequestMethod.POST
			, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	@ResponseBody
	public BaseDTO executeLogin(HttpServletRequest req, HttpServletResponse resp, HttpSession session
			, RedirectAttributes redirectAttributes) throws Exception{
		BaseDTO dtoRes = new BaseDTO();
		
		String userName = commons.getParameterFromRequest(req, "j_username").toUpperCase().replaceAll("\\s", "");
		String password = commons.getParameterFromRequest(req, "j_password");
		String captcha = commons.getParameterFromRequest(req, "j_captcha");
		
		String captchaSession = null == session.getAttribute(Constants.SESSION_CAPTCHA)? "": session.getAttribute(Constants.SESSION_CAPTCHA).toString();
		session.removeAttribute(Constants.SESSION_CAPTCHA);
		if("".equals(userName) || "".equals(password)) {
			dtoRes = new BaseDTO(1, Constants.MAP_ERROR.get(1));
			return dtoRes;
		}
		
		if(!captchaSession.equals(captcha)) {
			dtoRes = new BaseDTO(1, Constants.MAP_ERROR.get(2));
			return dtoRes;
		}
		

		
		Authentication auth = null;
		CurrentUserProfile cup = new CurrentUserProfile();
		
		List<CurrentUserRole> grantedAuthoritiesList = new ArrayList<CurrentUserRole>();
        CurrentUserRole role = new CurrentUserRole();
        
        UserLoginReq userLoginReq = new UserLoginReq(
        		userName, 
        		password, 
        		null != req.getHeader("X-Forwarded-For")? req.getHeader("X-Forwarded-For"): req.getRemoteAddr(),
				req.getHeader("User-Agent")
    		);
		
        LoginRes res = restAPIUtility.callAPILogin("/auth", HttpMethod.POST, userLoginReq);
        if(0 == res.getStatusCode()) {
        	StringBuilder sbRights = new StringBuilder("|");
        	
        	sbRights.append("path|pathCreate|create|");
        	sbRights.append("change-color|change-color-edit|");
        	cup.setUsername(userName);
        	cup.setPassword(password);
        	cup.setLoginRes(res);
        	cup.setAllRights(sbRights.toString());
        	
        
            grantedAuthoritiesList.add(role);	            
            cup.setAuthorities(grantedAuthoritiesList);
            
            auth = new UsernamePasswordAuthenticationToken(cup, password, grantedAuthoritiesList);
            
            SecurityContext sc = new SecurityContextImpl();
            sc.setAuthentication(auth);
            SecurityContextHolder.setContext(sc);
            
            session.setMaxInactiveInterval(60 * 60 * 1);
			
			dtoRes = new BaseDTO(0, Constants.MAP_ERROR.get(0));
        }else {
        	dtoRes = new BaseDTO(res.getStatusCode(), res.getStatusText());
        }
		return dtoRes;
	}
	
	@RequestMapping(value = {"/logout"})
	public String logoutPage(HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		
		SecurityContextHolder.getContext().setAuthentication(null);
		new SecurityContextLogoutHandler().logout(request, response, auth);
		
		redirectAttributes.addFlashAttribute("logout", "true");
		return "redirect:/admin";
			
	}
	
}
