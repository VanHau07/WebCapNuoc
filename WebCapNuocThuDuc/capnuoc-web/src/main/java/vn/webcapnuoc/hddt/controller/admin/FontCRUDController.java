package vn.webcapnuoc.hddt.controller.admin;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgParam;
import com.api.message.MsgParams;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.dto.LoginRes;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@RequestMapping({
	"/admin/fontCreate"
	, "/admin/fontDetail"
	, "/admin/fontEdit"
	, "/admin/fontActive"
	, "/admin/fontDeActive"
	, "/admin/fontDelete"
	, "/admin/fontHotnews"
	
	
})
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class FontCRUDController extends AbstractController{
	private static final Logger log = LogManager.getLogger(FontCRUDController.class);
	@Autowired RestAPIUtility restAPI;
	@Autowired RestTemplate restTemplate;
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	private String name;
	private String loai_font;
	private String loai_font_text;

	
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
			msgParam.setParam("DMFont");
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
					req.setAttribute("map_dmFont", hItem);
				}
				
			}
			
		}catch(Exception e) {}
	}
	
	@RequestMapping(value = "/init", method = { RequestMethod.POST, RequestMethod.GET })
	public String init(Locale locale, HttpServletRequest req, HttpSession session
			, @RequestAttribute(name = "transaction", value = "", required = false) String transaction
			, @RequestAttribute(name = "method", value = "", required = false) String method
			) throws Exception{
		errorCode = "";
		errorDesc = "";
		
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
	
		LoginRes issu = cup.getLoginRes();
	
		LoadParameter(cup, locale, req, "");
		BaseDTO baseDTO = new BaseDTO(req);
		Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		HashMap<String, String> hData = new HashMap<>();
		msg.setObjData(hData);

		
		
		_id = commons.getParameterFromRequest(req, "_id");
		String header = "Thêm mới";
		String action = "CREATE";
		boolean isEdit = false;	
		
		req.setAttribute("NLap", commons.convertLocalDateTimeToString(LocalDate.now(), Constants.FORMAT_DATE.FORMAT_DATE_WEB));
		req.setAttribute("TGia", "1");
		req.setAttribute("DVTTe", "VND");

		switch (transaction) {
		case "fontCreate":
			header = "Thêm mới kiểu chữ";
			action = "CREATE";
			isEdit = true;
			break;
		case "fontDetail":
			header = "Chi tiết kiểu chữ";
			action = "DETAIL";
			isEdit = false;
			break;
		case "fontEdit":
			header = "Thay đổi nội dung loại kiểu chữ";
			action = "EDIT";
			isEdit = true;
			break;
		default:
			break;
		}
		
		
		req.setAttribute("_header_", header);
		req.setAttribute("_action_", action);
		req.setAttribute("_isedit_", isEdit);
		req.setAttribute("_id", _id);
		

		if ("|DETAIL|EDIT|".indexOf("|" + action + "|") != -1)
			inquiry(cup, locale, req, session, _id, action, transaction, method);

		if (!"".equals(errorDesc))
			req.setAttribute("messageError", errorDesc);

		
		return "admin/content/fontCRUD";
	}

	private void inquiry(CurrentUserProfile cup, Locale locale, HttpServletRequest req, HttpSession session, String _id , String action, String transaction, String method) throws Exception{
		if("".equals(_id)) {
			errorCode = "NOT FOUND";
			errorDesc = "Không tìm thấy thông tin hóa đơn.";
			return;
		}
		BaseDTO baseDTO = new BaseDTO(req);
		Msg msg = baseDTO.createMsg(cup, Constants.MSG_ACTION_CODE.INQUIRY);
		HashMap<String, String> hData = new HashMap<>();
		msg.setObjData(hData);
		
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/font/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			
			req.setAttribute("Title", commons.getTextJsonNode(jsonData.at("/Title")));
			req.setAttribute("Name", commons.getTextJsonNode(jsonData.at("/Name")));		
			req.setAttribute("LoaiFont", commons.getTextJsonNode(jsonData.at("/LoaiFont")));
			req.setAttribute("LoaiFontText", commons.getTextJsonNode(jsonData.at("/LoaiFontText")));
		}else {
			errorDesc = rspStatus.getErrorDesc();
		}
	}
	
	public BaseDTO checkDataToAccept(HttpServletRequest req, HttpSession session, String transaction
			, CurrentUserProfile cup) throws Exception{
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);
		
		_id = commons.getParameterFromRequest(req, "_id").replaceAll("\\s", "");
		name = commons.getParameterFromRequest(req, "name").trim().replaceAll("\\s+", " ");	
		loai_font = commons.getParameterFromRequest(req, "loai-font").trim().replaceAll("\\s+", " ");
		loai_font_text = commons.getParameterFromRequest(req, "loai-font-text").trim().replaceAll("\\s+", " ");

	
		File file = null;
		switch (transaction) {
		case "fontCreate":
			if("".equals(name)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề loại kiểu chữ này.");
			}
			
			if("".equals(loai_font)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng chọn loại kiểu chữ.");
			}
			break;
		case "fontEdit":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID loại kiểu chữ này.");
			}
			if("".equals(name)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tên loại kiểu chữ này.");
			}
			if("".equals(loai_font)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng chọn loại kiểu chữ.");
			}
			break;
		case "fontActive":
		case "fontDeActive":
		case "fontDelete":
		case "fontHotnews":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID loại kiểu chữ này.");
			}
			break;

		default:
			break;
		}
		
		
		return dto;
	}
	
	@RequestMapping(value = "/checkDataToSave",  produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public BaseDTO execCheckDataToSave(Locale locale, HttpServletRequest req, HttpSession session
			, @RequestAttribute(name = "transaction", required = false, value = "") String transaction) throws Exception {
		String token = "";
		if (null != session.getAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE)) {
			token = session.getAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE).toString();
			session.removeAttribute(token);
		}
		session.removeAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE);
		
		BaseDTO dto = new BaseDTO();
		String messageConfirm = "Bạn có muốn thêm mới hóa đơn không?";
		switch (transaction) {
		case "fontCreate":
			messageConfirm = "Bạn có chắc chắn muốn thêm mới loại kiểu chữ này không?";
			break;
		case "fontEdit":
			messageConfirm = "Bạn có chắc chắn muốn thay đổi loại kiểu chữ này không?";
			break;
		case "fontActive":
			messageConfirm = "Bạn có chắc chắn muốn kích hoạt loại kiểu chữ này không?";
			break;
		case "fontDeActive":
			messageConfirm = "Bạn có chắc chắn muốn hủy kích hoạt loại kiểu chữ này không?";
			break;
		case "fontDelete":
			messageConfirm = "Bạn có chắc chắn muốn xóa loại kiểu chữnày không?";
			break;
		case "fontHotnews":
			messageConfirm = "Bạn có chắc chắn muốn thực hiện không?";
			break;

		default:
			dto = new BaseDTO();
			dto.setErrorCode(998);
			dto.setResponseData(Constants.MAP_ERROR.get(998));
			return dto;
		}
		
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		dto = checkDataToAccept(req, session, transaction, cup);
		if(0 != dto.getErrorCode()) {
			dto.setErrorCode(999);
			dto.setResponseData(Constants.MAP_ERROR.get(999));
			return dto;
		}
		

		token = commons.csRandomAlphaNumbericString(30);
		session.setAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE, token);
		
		HashMap<String, String> hInfo = new HashMap<String, String>();
		hInfo.put("CONFIRM", messageConfirm);
		hInfo.put("TOKEN", token);
		
		dto.setResponseData(hInfo);
		dto.setErrorCode(0);
		return dto;

	}
	
	@RequestMapping(value = "/saveData",  produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public BaseDTO execSaveData(HttpServletRequest req, HttpSession session
			, @RequestAttribute(name = "transaction", value = "", required = false) String transaction
			, @RequestParam(value = "tokenTransaction", required = false, defaultValue = "") String tokenTransaction) throws Exception{
		BaseDTO dtoRes = new BaseDTO();
		
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		dtoRes = checkDataToAccept(req, session, transaction, cup);
		if(0 != dtoRes.getErrorCode()) {
			dtoRes.setErrorCode(999);
			dtoRes.setResponseData(Constants.MAP_ERROR.get(999));
			return dtoRes;
		}
		
		/*CHECK TOKEN*/
		String token = session.getAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE) == null ? ""
				: session.getAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE).toString();
		session.removeAttribute(Constants.SESSION_TYPE.SESSION_TOKEN_EXECUTE);
		if ("".equals(token) || !tokenTransaction.equals(token)) {
			dtoRes.setErrorCode(1);
			dtoRes.setResponseData("Token giao dịch không hợp lệ.");
			return dtoRes;
		}
		/*END: CHECK TOKEN*/
	
		
		String actionCode = Constants.MSG_ACTION_CODE.CREATED;
		switch (transaction) {
		case "fontCreate": actionCode = Constants.MSG_ACTION_CODE.CREATED; break;
		case "fontEdit": actionCode = Constants.MSG_ACTION_CODE.MODIFY; break;
		case "fontActive": actionCode = Constants.MSG_ACTION_CODE.ACTIVE; break;
		case "fontDeActive": actionCode = Constants.MSG_ACTION_CODE.DEACTIVE; break;
		case "fontDelete": actionCode = Constants.MSG_ACTION_CODE.DELETE; break;
	
		default:
			dtoRes = new BaseDTO();
			dtoRes.setErrorCode(998);
			dtoRes.setResponseData(Constants.MAP_ERROR.get(998));
			return dtoRes;
		}
		dtoRes = new BaseDTO(req);
		Msg msg = dtoRes.createMsg(cup, actionCode);
		HashMap<String, Object> hData = new HashMap<>();
		
		hData.put("_id", _id);
		hData.put("Name", name);
		hData.put("LoaiFont", loai_font);
		hData.put("LoaiFontText", loai_font_text);
		
		msg.setObjData(hData);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/font/crud", cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			dtoRes.setErrorCode(0);
			switch (actionCode) {
			case Constants.MSG_ACTION_CODE.CREATED:
				dtoRes.setResponseData("Thêm mới loại kiểu chữ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.MODIFY:
				dtoRes.setResponseData("Thay đổi thông tin loại kiểu chữ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.ACTIVE:
				dtoRes.setResponseData("Kích hoạt loại kiểu chữthành công.");
				break;
			case Constants.MSG_ACTION_CODE.DEACTIVE:
				dtoRes.setResponseData("Hủy kích hoạt loại kiểu chữ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DELETE:
				dtoRes.setResponseData("Thêm mới loại kiểu chữ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.HOTNEWS:
				dtoRes.setResponseData("Thực hiện thành công.");
				break;
			default:
				break;
			}
		}else {
			dtoRes.setErrorCode(rspStatus.getErrorCode());
			dtoRes.setResponseData(rspStatus.getErrorDesc());
		}
		return dtoRes;
	}
	
	
}
	