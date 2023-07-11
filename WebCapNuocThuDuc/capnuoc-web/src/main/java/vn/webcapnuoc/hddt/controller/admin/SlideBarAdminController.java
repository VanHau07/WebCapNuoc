package vn.webcapnuoc.hddt.controller.admin;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.api.message.MsgParam;
import com.api.message.MsgParams;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.dto.JsonGridDTO;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@RequestMapping("/admin/slidebar")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class SlideBarAdminController extends AbstractController {
	@Autowired RestAPIUtility restAPI;
	
	private void LoadParameter(CurrentUserProfile cup, Locale locale, HttpServletRequest req, String action) {
		try {
			BaseDTO baseDTO = new BaseDTO(req);
			Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.LOAD_PARAMS);
			
			/*DANH SACH THAM SO*/
			HashMap<String, String> hashConds = null;
			ArrayList<HashMap<String, String>> conds = null;
			MsgParam msgParam = null;
			MsgParams msgParams = new MsgParams();
			
			msgParam = new MsgParam();
			msgParam.setId("param01");
			msgParam.setParam("DMEBook");
			msgParams.getParams().add(msgParam);
			
			
			/*END: DANH SACH THAM SO*/
			msg.setObjData(msgParams);
			
			JSONRoot root = new JSONRoot(msg);
			MsgRsp rsp = restAPI.callAPINormal("/commons/get-full-params", cup.getLoginRes().getToken(), HttpMethod.POST, root);
			MspResponseStatus rspStatus = rsp.getResponseStatus();
			
			if(rspStatus.getErrorCode() == 0 && rsp.getObjData() != null) {
				LinkedHashMap<String, String> hItem = null;
				
				JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
				if(null != jsonData.at("/param01") && jsonData.at("/param01") instanceof ArrayNode) {
					hItem = new LinkedHashMap<String, String>();
					for(JsonNode o: jsonData.at("/param01")) {
						hItem.put(commons.getTextJsonNode(o.get("Code")), commons.getTextJsonNode(o.get("Name")));
					}
					req.setAttribute("map_dmLoaiSach", hItem);
				}
				
			}
			
		}catch(Exception e) {}
	}
//	private void LoadStatus(CurrentUserProfile cup, Locale locale, HttpServletRequest req, String action) {
//		try {	
//			LinkedHashMap<String, String> hItem = null;
//			hItem = new LinkedHashMap<String, String>();
//			hItem.put("1", "Đang thực hiện");
//			hItem.put("2", "Đã đấu giá thành");
//			req.setAttribute("map_status", hItem);
//		}catch(Exception e) {}	
//	}
	
	
	@RequestMapping(value = "/init", method = { RequestMethod.POST, RequestMethod.GET })
	public String main(Locale locale, Principal principal, HttpServletRequest request) throws Exception {
		request.setAttribute("title", "Quản lý slide bar");
		List<HashMap<String, Object>> rows7 = null;
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		if(cup==null) {
			return "redirect:/admin";
		}
		LoadParameter(cup, locale, request, "");
//		LoadStatus(cup, locale, request, "");
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
		return "admin/content/slide";
	}

	private String title;
	

	private BaseDTO checkDataSearch(Locale locale, HttpServletRequest req, HttpSession session) {
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);


	//	title = commons.getParameterFromRequest(req, "title").trim().replaceAll("\\s+", " ");
	//	loai_sach = commons.getParameterFromRequest(req, "loai-sach").trim().replaceAll("\\s+", " ");
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
	//	hData.put("Title", title);
	//	hData.put("LoaiSach", loai_sach);
		msg.setObjData(hData);
		
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/slide/list", cup.getLoginRes().getToken(),HttpMethod.POST, root);
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
					hItem.put("Title", commons.getTextJsonNode(row.at("/Title")));					
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
