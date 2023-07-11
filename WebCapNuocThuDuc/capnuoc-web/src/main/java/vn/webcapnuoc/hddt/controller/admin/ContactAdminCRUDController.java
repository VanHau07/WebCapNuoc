package vn.webcapnuoc.hddt.controller.admin;

import java.io.File;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
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
@RequestMapping({
	"/admin/contactCreate"
	, "/admin/contactDetail"
	, "/admin/contactEdit"
	, "/admin/contactActive"
	, "/admin/contactDeActive"
	, "/admin/contactDelete"
	, "/admin/contactHotnews"
})
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class ContactAdminCRUDController extends AbstractController{
	private static final Logger log = LogManager.getLogger(ContactAdminCRUDController.class);
	@Autowired RestAPIUtility restAPI;
	@Autowired RestTemplate restTemplate;
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	private String name;
	private String description;
	private String address;
	private String phone;
	private String email;
	private String map;
	
	
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

		
		
		_id = commons.getParameterFromRequest(req, "_id");
		String header = "Thêm mới";
		String action = "CREATE";
		boolean isEdit = false;	
		
		req.setAttribute("NLap", commons.convertLocalDateTimeToString(LocalDate.now(), Constants.FORMAT_DATE.FORMAT_DATE_WEB));

		switch (transaction) {
		case "contactCreate":
			header = "Thêm mới thông tin liên hệ";
			action = "CREATE";
			isEdit = true;
			break;
		case "contactDetail":
			header = "Chi tiết";
			action = "DETAIL";
			isEdit = false;
			break;
		case "contactEdit":
			header = "Thay đổi nội dung liên hệ";
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

		
		return "admin/content/ContactCRUD";
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
		MsgRsp rsp = restAPI.callAPINormal("/contact/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			
			req.setAttribute("Title", commons.getTextJsonNode(jsonData.at("/Title")));
			req.setAttribute("Name", commons.getTextJsonNode(jsonData.at("/Name")));
			req.setAttribute("Description", commons.getTextJsonNode(jsonData.at("/Description")));
			req.setAttribute("Phone", commons.getTextJsonNode(jsonData.at("/Phone")));
			req.setAttribute("Address", commons.getTextJsonNode(jsonData.at("/Address")));
			req.setAttribute("Email", commons.getTextJsonNode(jsonData.at("/Email")));	
			req.setAttribute("Map", commons.getTextJsonNode(jsonData.at("/Map")));	
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
		description = commons.getParameterFromRequest(req, "description").trim().replaceAll("\\s+", " ");
		address = commons.getParameterFromRequest(req, "address").trim().replaceAll("\\s+", " ");
		phone = commons.getParameterFromRequest(req, "phone").trim().replaceAll("\\s+", " ");
		email = commons.getParameterFromRequest(req, "email").trim().replaceAll("\\s+", " ");
		map = commons.getParameterFromRequest(req, "map").trim().replaceAll("\\s+", " ");
	
		File file = null;
		switch (transaction) {
		case "contactCreate":
			if("".equals(name)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề liên hệ.");
			}
			if("".equals(name)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng kiểm tra thông tin vị trí.");			
			}
			if(!"".equals(email) && !commons.isValidEmailAddress(email)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Địa chỉ email không đúng định dạng.");
			}
		
			break;
		case "contactEdit":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID tin tức.");
			}
			if("".equals(name)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào tiêu đề tin tức.");
			}
			if("".equals(address)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập địa chỉ.");			
			}
			if(!"".equals(email) && !commons.isValidEmailAddress(email)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Địa chỉ email không đúng định dạng.");
			}
			break;
		case "contactActive":
		case "contactDeActive":
		case "contactDelete":
		case "contactHotnews":
			if("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID tin tức.");
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
		case "contactCreate":
			messageConfirm = "Bạn có chắc chắn muốn thêm mới không?";
			break;
		case "contactEdit":
			messageConfirm = "Bạn có chắc chắn muốn thay đổi không?";
			break;
		case "contactActive":
			messageConfirm = "Bạn có chắc chắn muốn kích hoạt không?";
			break;
		case "contactDeActive":
			messageConfirm = "Bạn có chắc chắn muốn hủy kích hoạt không?";
			break;
		case "contactDelete":
			messageConfirm = "Bạn có chắc chắn muốn xóa không?";
			break;
		case "contactHotnews":
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
		case "contactCreate": actionCode = Constants.MSG_ACTION_CODE.CREATED; break;
		case "contactEdit": actionCode = Constants.MSG_ACTION_CODE.MODIFY; break;
		case "contactActive": actionCode = Constants.MSG_ACTION_CODE.ACTIVE; break;
		case "contactDeActive": actionCode = Constants.MSG_ACTION_CODE.DEACTIVE; break;
		case "contactDelete": actionCode = Constants.MSG_ACTION_CODE.DELETE; break;
		case "contactHotnews": actionCode = Constants.MSG_ACTION_CODE.HOTNEWS; break;
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
		hData.put("Description", description);		
		hData.put("Address", address);
		hData.put("Phone", phone);
		hData.put("Email", email);
		hData.put("Map", map);
		msg.setObjData(hData);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/contact/crud", cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			dtoRes.setErrorCode(0);
			switch (actionCode) {
			case Constants.MSG_ACTION_CODE.CREATED:
				dtoRes.setResponseData("Thêm mới liên hệ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.MODIFY:
				dtoRes.setResponseData("Thay đổi thông tin liên hệ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.ACTIVE:
				dtoRes.setResponseData("Kích hoạt liên hệ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DEACTIVE:
				dtoRes.setResponseData("Hủy kích hoạt liên hệ thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DELETE:
				dtoRes.setResponseData("Thêm mới thành công.");
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
	