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
	"/admin/ebookCreate"
	, "/admin/ebookDetail"
	, "/admin/ebookEdit"
	, "/admin/ebookActive"
	, "/admin/ebookDeActive"
	, "/admin/ebookDelete"
	, "/admin/ebookHotnews"
	, "/admin/ebookReadOnline"
	
	
})
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class EBookAdminCRUDController extends AbstractController{
	private static final Logger log = LogManager.getLogger(EBookAdminCRUDController.class);
	@Autowired RestAPIUtility restAPI;
	@Autowired RestTemplate restTemplate;
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	private String title;
	private String loai_sach;
	private String loai_sach_text;
	private String content;
	private String summaryContent;
	private String price;
	private String tacgia;
	private String attachFileName;
	private String attachFileNameSystem;
	
	private String attachFileName1;
	private String attachFileNameSystem1;
	
	private String sotrang;
	private String year;


	
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
	private void LoadStatus(CurrentUserProfile cup, Locale locale, HttpServletRequest req, String action) {
		try {	
			LinkedHashMap<String, String> hItem = null;
			hItem = new LinkedHashMap<String, String>();
			hItem.put("1", "Đang thực hiện");
			hItem.put("2", "Đã đấu giá thành");
			req.setAttribute("map_status", hItem);
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
		LoadStatus(cup, locale, req, "");

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
		case "ebookCreate":
			header = "Thêm mới sách";
			action = "CREATE";
			isEdit = true;
			break;
		case "ebookDetail":
			header = "Chi tiết sách";
			action = "DETAIL";
			isEdit = false;
			break;
		case "ebookEdit":
			header = "Thay đổi nội dung loại sách";
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

		
		return "admin/content/ebook_Crud";
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
		MsgRsp rsp = restAPI.callAPINormal("/ebook/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			
			req.setAttribute("Title", commons.getTextJsonNode(jsonData.at("/Title")));
			req.setAttribute("Content", commons.decodeURIComponent(commons.getTextJsonNode(jsonData.at("/Content"))));
			req.setAttribute("LoaiSach",commons.getTextJsonNode(jsonData.at("/LoaiSach")));
			req.setAttribute("LoaiSachText", commons.getTextJsonNode(jsonData.at("/LoaiSachText")));
			req.setAttribute("SummaryContent", commons.getTextJsonNode(jsonData.at("/SummaryContent")));
			req.setAttribute("Price",commons.getTextJsonNode(jsonData.at("/Price")));
			req.setAttribute("TacGia", commons.getTextJsonNode(jsonData.at("/TacGia")));
			req.setAttribute("SoTrang", commons.getTextJsonNode(jsonData.at("/SoTrang")));
			req.setAttribute("Year", commons.getTextJsonNode(jsonData.at("/Year")));
			req.setAttribute("ImageLogo", commons.getTextJsonNode(jsonData.at("/ImageLogo")));
			req.setAttribute("ImageLogoOriginalFilename",commons.getTextJsonNode(jsonData.at("/ImageLogoOriginalFilename")));
			
			req.setAttribute("ImageLogo1", commons.getTextJsonNode(jsonData.at("/ImageLogo1")));
			req.setAttribute("ImageLogoOriginalFilename1",commons.getTextJsonNode(jsonData.at("/ImageLogoOriginalFilename1")));
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
		content = commons.getParameterFromRequest(req, "content").trim().replaceAll("\\s+", " ");
		loai_sach = commons.getParameterFromRequest(req, "loai-sach").trim().replaceAll("\\s+", " ");
		loai_sach_text = commons.getParameterFromRequest(req, "loai-sach-text").trim().replaceAll("\\s+", " ");
		summaryContent = commons.getParameterFromRequest(req, "summaryContent").trim().replaceAll("\\s+", " ");
		price = commons.getParameterFromRequest(req, "price").trim().replaceAll("\\s+", " ");
		tacgia = commons.getParameterFromRequest(req, "tacgia").trim().replaceAll("\\s+", " ");
		sotrang = commons.getParameterFromRequest(req, "sotrang").trim().replaceAll("\\s+", " ");
		year = commons.getParameterFromRequest(req, "year").trim().replaceAll("\\s+", " ");
		attachFileName = commons.getParameterFromRequest(req, "attachFileName").trim().replaceAll("\\s+", " ");
		attachFileNameSystem = commons.getParameterFromRequest(req, "attachFileNameSystem").trim().replaceAll("\\s+", " ");
		
		attachFileName1 = commons.getParameterFromRequest(req, "attachFileName1").trim().replaceAll("\\s+", " ");
		attachFileNameSystem1 = commons.getParameterFromRequest(req, "attachFileNameSystem1").trim().replaceAll("\\s+", " ");
		
		
	
		File file = null;
		switch (transaction) {
		case "ebookCreate":
			if("".equals(title)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề loại sách này.");
			}
			if("".equals(attachFileName) || "".equals(attachFileNameSystem)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng kiểm tra lại ảnh sách.");
			}else {
				file = new File(SystemParams.DIR_TMP, attachFileNameSystem);
				if(!file.exists() || !file.isFile()) {
					dto.setErrorCode(1);
					dto.getErrorMessages().add("Tập tin ảnh không tồn tại.");
				}
			}
			
			if("".equals(attachFileName1) || "".equals(attachFileNameSystem1)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng kiểm tra lại file.");
			}else {
				file = new File(SystemParams.DIR_TMP, attachFileNameSystem1);
				if(!file.exists() || !file.isFile()) {
					dto.setErrorCode(1);
					dto.getErrorMessages().add("Tập tin không tồn tại.");
				}
			}
			
			if("".equals(loai_sach)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng chọn loại sách.");
			}
			break;
		case "ebookEdit":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID loại sách này.");
			}
			if("".equals(title)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề loại sách này.");
			}
			if("".equals(loai_sach)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng chọn loại sách.");
			}
			break;
		case "ebookActive":
		case "ebookDeActive":
		case "ebookDelete":
		case "ebookHotnews":
		case "ebookReadOnline":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID loại sách này.");
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
		String messageConfirm = "Bạn có chắc chắn muốn thêm mới loại sách này không?";
		
		switch (transaction) {
		case "ebookCreate":
			messageConfirm = "Bạn có chắc chắn muốn thêm mới loại sách này không?";
			break;
		case "ebookEdit":
			messageConfirm = "Bạn có chắc chắn muốn thay đổi loại sách này không?";
			break;
		case "ebookActive":
			messageConfirm = "Bạn có chắc chắn muốn kích hoạt loại sách này không?";
			break;
		case "ebookDeActive":
			messageConfirm = "Bạn có chắc chắn muốn hủy kích hoạt loại sách này không?";
			break;
		case "ebookDelete":
			messageConfirm = "Bạn có chắc chắn muốn xóa loại sách này không?";
			break;
		case "ebookHotnews":
			messageConfirm = "Bạn có chắc chắn muốn thực hiện không?";
			break;
		case "ebookReadOnline":
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
		case "ebookCreate": actionCode = Constants.MSG_ACTION_CODE.CREATED; break;
		case "ebookEdit": actionCode = Constants.MSG_ACTION_CODE.MODIFY; break;
		case "ebookActive": actionCode = Constants.MSG_ACTION_CODE.ACTIVE; break;
		case "ebookDeActive": actionCode = Constants.MSG_ACTION_CODE.DEACTIVE; break;
		case "ebookDelete": actionCode = Constants.MSG_ACTION_CODE.DELETE; break;
		case "ebookHotnews": actionCode = Constants.MSG_ACTION_CODE.HOTNEWS; break;
		case "ebookReadOnline": actionCode = Constants.MSG_ACTION_CODE.READONLINE; break;
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
		hData.put("Content", content);
		hData.put("LoaiSach", loai_sach);
		hData.put("LoaiSachText", loai_sach_text);
		hData.put("SummaryContent", summaryContent);
		hData.put("Price", price);
		hData.put("TacGia", tacgia);
		hData.put("SoTrang", sotrang);
		hData.put("AttachFileName", attachFileName);
		hData.put("AttachFileNameSystem", attachFileNameSystem);
		
		hData.put("AttachFileName1", attachFileName1);
		hData.put("AttachFileNameSystem1", attachFileNameSystem1);
		
		hData.put("Year", year);
				
		msg.setObjData(hData);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/ebook/crud", cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			dtoRes.setErrorCode(0);
			switch (actionCode) {
			case Constants.MSG_ACTION_CODE.CREATED:
				dtoRes.setResponseData("Thêm mới loại sách thành công.");
				break;
			case Constants.MSG_ACTION_CODE.MODIFY:
				dtoRes.setResponseData("Thay đổi thông tin loại sách thành công.");
				break;
			case Constants.MSG_ACTION_CODE.ACTIVE:
				dtoRes.setResponseData("Kích hoạt loại sách thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DEACTIVE:
				dtoRes.setResponseData("Hủy kích hoạt loại sách thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DELETE:
				dtoRes.setResponseData("Thêm mới loại sách thành công.");
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
	