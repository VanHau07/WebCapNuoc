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
import vn.webcapnuoc.hddt.utils.SystemParams;

@Controller
@RequestMapping({
	"/admin/photoCreate"
	, "/admin/photoDetail"
	, "/admin/photoEdit"
	, "/admin/photoActive"
	, "/admin/photoDeActive"
	, "/admin/photoDelete"
	, "/admin/photoHotnews"
	, "/admin/photoReadOnline"
	
	
})
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class PhotoAdminCRUDController extends AbstractController{
	private static final Logger log = LogManager.getLogger(PhotoAdminCRUDController.class);
	@Autowired RestAPIUtility restAPI;
	@Autowired RestTemplate restTemplate;
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	private String title;
	private String summaryContent;
	private String attachFileName;
	private String attachFileNameSystem;
	
	
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
			msgParam.setParam("DMphoto");
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
		switch (transaction) {
		case "photoCreate":
			header = "Thêm mới tác phẩm ảnh";
			action = "CREATE";
			isEdit = true;
			break;
		case "photoDetail":
			header = "Chi tiết tác phẩm ảnh";
			action = "DETAIL";
			isEdit = false;
			break;
		case "photoEdit":
			header = "Thay đổi nội dung tác phẩm ảnh";
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

		
		return "admin/content/photo_Crud";
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
		MsgRsp rsp = restAPI.callAPINormal("/photo/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			
			req.setAttribute("Title", commons.getTextJsonNode(jsonData.at("/Title")));			
			req.setAttribute("SummaryContent", commons.getTextJsonNode(jsonData.at("/SummaryContent")));			
			req.setAttribute("ImageLogo", commons.getTextJsonNode(jsonData.at("/ImageLogo")));
			req.setAttribute("ImageLogoOriginalFilename",commons.getTextJsonNode(jsonData.at("/ImageLogoOriginalFilename")));
			
		}else {
			errorDesc = rspStatus.getErrorDesc();
		}
	}
	
	public BaseDTO checkDataToAccept(HttpServletRequest req, HttpSession session, String transaction
			, CurrentUserProfile cup) throws Exception{
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);
		
		_id = commons.getParameterFromRequest(req, "_id").replaceAll("\\s", "");
		title = commons.getParameterFromRequest(req, "title").trim().replaceAll("\\s+", " ");
		
		summaryContent = commons.getParameterFromRequest(req, "summaryContent").trim().replaceAll("\\s+", " ");
		
		attachFileName = commons.getParameterFromRequest(req, "attachFileName").trim().replaceAll("\\s+", " ");
		attachFileNameSystem = commons.getParameterFromRequest(req, "attachFileNameSystem").trim().replaceAll("\\s+", " ");
		
		
		
	
		File file = null;
		switch (transaction) {
		case "photoCreate":
			if("".equals(title)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề loại tác phẩm ảnh này.");
			}
			if("".equals(attachFileName) || "".equals(attachFileNameSystem)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng kiểm tra lại ảnh tác phẩm ảnh.");
			}else {
				file = new File(SystemParams.DIR_TMP, attachFileNameSystem);
				if(!file.exists() || !file.isFile()) {
					dto.setErrorCode(1);
					dto.getErrorMessages().add("Tập tin ảnh không tồn tại.");
				}
			}
			
		
			
			
			break;
		case "photoEdit":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID loại tác phẩm ảnh này.");
			}
			if("".equals(title)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề loại tác phẩm ảnh này.");
			}
			
			break;
		case "photoActive":
		case "photoDeActive":
		case "photoDelete":
		case "photoHotnews":
		case "photoReadOnline":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID loại tác phẩm ảnh này.");
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
		String messageConfirm = "Bạn có chắc chắn muốn thêm mới tác phẩm ảnh này không?";
		
		switch (transaction) {
		case "photoCreate":
			messageConfirm = "Bạn có chắc chắn muốn thêm mới tác phẩm ảnh này không?";
			break;
		case "photoEdit":
			messageConfirm = "Bạn có chắc chắn muốn thay đổi tác phẩm ảnh này không?";
			break;
		case "photoActive":
			messageConfirm = "Bạn có chắc chắn muốn kích hoạt tác phẩm ảnh này không?";
			break;
		case "photoDeActive":
			messageConfirm = "Bạn có chắc chắn muốn hủy kích hoạt tác phẩm ảnh này không?";
			break;
		case "photoDelete":
			messageConfirm = "Bạn có chắc chắn muốn xóa tác phẩm ảnh này không?";
			break;
		case "photoHotnews":
			messageConfirm = "Bạn có chắc chắn muốn thực hiện không?";
			break;
		case "photoReadOnline":
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
		case "photoCreate": actionCode = Constants.MSG_ACTION_CODE.CREATED; break;
		case "photoEdit": actionCode = Constants.MSG_ACTION_CODE.MODIFY; break;
		case "photoActive": actionCode = Constants.MSG_ACTION_CODE.ACTIVE; break;
		case "photoDeActive": actionCode = Constants.MSG_ACTION_CODE.DEACTIVE; break;
		case "photoDelete": actionCode = Constants.MSG_ACTION_CODE.DELETE; break;
		case "photoHotnews": actionCode = Constants.MSG_ACTION_CODE.HOTNEWS; break;
		case "photoReadOnline": actionCode = Constants.MSG_ACTION_CODE.READONLINE; break;
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
		hData.put("Title", title);
		
		hData.put("SummaryContent", summaryContent);
		
		hData.put("AttachFileName", attachFileName);
		hData.put("AttachFileNameSystem", attachFileNameSystem);
		
	
		msg.setObjData(hData);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/photo/crud", cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			dtoRes.setErrorCode(0);
			switch (actionCode) {
			case Constants.MSG_ACTION_CODE.CREATED:
				dtoRes.setResponseData("Thêm mới tác phẩm ảnh thành công.");
				break;
			case Constants.MSG_ACTION_CODE.MODIFY:
				dtoRes.setResponseData("Thay đổi thông tin tác phẩm ảnh thành công.");
				break;
			case Constants.MSG_ACTION_CODE.ACTIVE:
				dtoRes.setResponseData("Kích hoạt tác phẩm ảnh thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DEACTIVE:
				dtoRes.setResponseData("Hủy kích hoạt tác phẩm ảnh thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DELETE:
				dtoRes.setResponseData("Thêm mới tác phẩm ảnh thành công.");
				break;
			case Constants.MSG_ACTION_CODE.HOTNEWS:
				dtoRes.setResponseData("Thực hiện thành công.");
				break;
			case Constants.MSG_ACTION_CODE.READONLINE:
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
	