package vn.webcapnuoc.hddt.controller.admin;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgPage;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.dto.JsonGridDTO;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@RequestMapping("/admin/introduce")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class IntroduceCompanyController extends AbstractController {
	@Autowired RestAPIUtility restAPI;
	@RequestMapping(value = "/init", method = { RequestMethod.POST, RequestMethod.GET })
	public String main(Locale locale, Principal principal, HttpServletRequest request) throws Exception {
		request.setAttribute("title", "Quản lý giới thiệu");
		List<HashMap<String, Object>> rows7 = null;
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		if(cup==null) {
			return "redirect:/admin";
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
		return "admin/content/introduce_company";
	}

	private String title;

	private BaseDTO checkDataSearch(Locale locale, HttpServletRequest req, HttpSession session) {
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);

		title = commons.getParameterFromRequest(req, "title").trim().replaceAll("\\s+", " ");

		return dto;
	}

	@RequestMapping(value = "/search",  produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public BaseDTO execSearch(Locale locale, HttpServletRequest req, HttpSession session) throws Exception{
		JsonGridDTO grid = new JsonGridDTO();
		
		BaseDTO baseDTO = checkDataSearch(locale, req, session);
		if(0 != baseDTO.getErrorCode()) {
			grid.setErrorCode(baseDTO.getErrorCode());
			grid.setErrorMessages(baseDTO.getErrorMessages());
			grid.setResponseData(Constants.MAP_ERROR.get(999));
			return grid;
		}
		
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();		
		baseDTO = new BaseDTO(req);
		Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.SEARCH);
		HashMap<String, Object> hData = new HashMap<>();
		hData.put("Title", title);
		msg.setObjData(hData);
		
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/introduce/list", cup.getLoginRes().getToken(),HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			MsgPage page = rsp.getMsgPage();
			grid.setTotal(page.getTotalRows());
			
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			JsonNode rows = null;
			HashMap<String, String> hItem = null;
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
				for(JsonNode row: rows) {
					hItem = new HashMap<String, String>();
					String check = commons.getTextJsonNode(row.at("/ActiveFlag"));
					
					String hot_new = commons.getTextJsonNode(row.at("/HotNews"));
					String hot = "";
						if(hot_new.equals("false")||hot_new.equals("")||hot_new.equals(null)) {
								hot = "N";
							}else {
								hot = "Y";
							}
					String trangthai = "";
					String active = "";
					if(check.equals("false")) {
						active = "N";
						trangthai = "Chưa kích hoạt";
					}else {
						active = "Y";
						trangthai = "Đã kích hoạt";
					}
					hItem.put("_id", commons.getTextJsonNode(row.at("/_id")));					
					hItem.put("Title",commons.getTextJsonNode(row.at("/Title")));				
					hItem.put("HotNewsDesc", hot);
					hItem.put("ActiveFlag",active);
					hItem.put("StatusDesc", trangthai);
					hItem.put("CreateDate", 
							commons.convertLocalDateTimeToString(commons.convertLongToLocalDate(row.at("/InfoCreated/CreateDate").asLong()), Constants.FORMAT_DATE.FORMAT_DATE_WEB)
						);
					hItem.put("CreateUserFullName",commons.getTextJsonNode(row.at("/InfoCreated/CreateUserFullName")));
					grid.getRows().add(hItem);
				}
			}
			
		}else {
			grid = new JsonGridDTO();
			grid.setErrorCode(rspStatus.getErrorCode());
			grid.setResponseData(rspStatus.getErrorDesc());
		}
		
		return grid;
	}

	
}
