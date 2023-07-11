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
@RequestMapping({ "/admin/configCreate"
	, "/admin/configDetail"
	, "/admin/configEdit"
	, "/admin/configActive",
	"/admin/configDeActive"
	, "/admin/configDelete"
	, "/admin/configHotnews" })
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class ConfigMailCRUDController extends AbstractController{
	private static final Logger log = LogManager.getLogger(ConfigMailCRUDController.class);
	@Autowired RestAPIUtility restAPI;
	@Autowired RestTemplate restTemplate;
	
	private String errorCode;
	private String errorDesc;
	private String _id;
	private String checkAutoSend;
	private String checkSSL;
	private String checkTLS;
	private String smtpServer;
	private String smtpPort;
	private String emailAddress;
	private String emailPassword;
	
	
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
		case "configCreate":
			header = "Thêm mới thông tin cấu hình mail";
			action = "CREATE";
			isEdit = true;
			break;
		case "configDetail":
			header = "Chi tiết cấu hình mail";
			action = "DETAIL";
			isEdit = false;
			break;
		case "configEdit":
			header = "Thay đổi nội dung cấu hình mail";
			action = "EDIT";
			isEdit = true;
			break;
		default:
			break;
		}
		
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
		
		req.setAttribute("_header_", header);
		req.setAttribute("_action_", action);
		req.setAttribute("_isedit_", isEdit);
		req.setAttribute("_id", _id);
		

		if ("|DETAIL|EDIT|".indexOf("|" + action + "|") != -1)
			inquiry(cup, locale, req, session, _id, action, transaction, method);

		if (!"".equals(errorDesc))
			req.setAttribute("messageError", errorDesc);

		
		return "admin/content/config_mailCRUD";
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
		MsgRsp rsp = restAPI.callAPINormal("/configEmail/detail/" + _id, cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			
			req.setAttribute("Title", commons.getTextJsonNode(jsonData.at("/Title")));
			req.setAttribute("Path", commons.getTextJsonNode(jsonData.at("/Path")));
			req.setAttribute("CheckAutoSend", commons.getTextJsonNode(jsonData.at("/AutoSend")));
			req.setAttribute("CheckSSL", commons.getTextJsonNode(jsonData.at("/SSL")));
			req.setAttribute("CheckTLS", commons.getTextJsonNode(jsonData.at("/TLS")));
			req.setAttribute("SmtpServer", commons.getTextJsonNode(jsonData.at("/SmtpServer")));
			req.setAttribute("SmtpPort", commons.getTextJsonNode(jsonData.at("/SmtpPort")));
			req.setAttribute("EmailAddress", commons.getTextJsonNode(jsonData.at("/EmailAddress")));
			req.setAttribute("EmailPassword", commons.getTextJsonNode(jsonData.at("/EmailPassword")));
			req.setAttribute("_id", _id);
		}else {
			errorDesc = rspStatus.getErrorDesc();
		}
	}
	
	public BaseDTO checkDataToAccept(HttpServletRequest req, HttpSession session, String transaction
			, CurrentUserProfile cup) throws Exception{
		BaseDTO dto = new BaseDTO();
		dto.setErrorCode(0);
		
		_id = commons.getParameterFromRequest(req, "_id").replaceAll("\\s", "");
		checkAutoSend = commons.getParameterFromRequest(req, "check-auto-send").replaceAll("\\s", "");
		checkSSL = commons.getParameterFromRequest(req, "check-ssl").replaceAll("\\s", "");
		checkTLS = commons.getParameterFromRequest(req, "check-tls").replaceAll("\\s", "");
		smtpServer = commons.getParameterFromRequest(req, "smtp-server").replaceAll("\\s", "");
		smtpPort = commons.getParameterFromRequest(req, "smtp-port").replaceAll("\\s", "");
		emailAddress = commons.getParameterFromRequest(req, "email-address").replaceAll("\\s", "").toLowerCase();
		emailPassword = commons.getParameterFromRequest(req, "email-password");

		File file = null;
		switch (transaction) {
		case "configCreate":

			if ("".equals(smtpServer)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào địa chỉ SMTP Server.");
			}
			if ("".equals(smtpPort) || !commons.checkStringIsInt(smtpPort) || commons.stringToInteger(smtpPort) <= 0) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng kiểm tra lại Port SMTP Server.");
			}
			if ("".equals(emailAddress)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào địa chỉ email gửi.");
			} else if (!commons.isValidEmailAddress(emailAddress)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Địa chỉ email gửi không đúng định dạng.");
			}
			if ("".equals(_id) && "".equals(emailPassword)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào mật khẩu email gửi.");
			}

			break;
		case "configEdit":

			if ("".equals(smtpServer)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào địa chỉ SMTP Server.");
			}
			if ("".equals(smtpPort) || !commons.checkStringIsInt(smtpPort) || commons.stringToInteger(smtpPort) <= 0) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng kiểm tra lại Port SMTP Server.");
			}
			if ("".equals(emailAddress)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào địa chỉ email gửi.");
			} else if (!commons.isValidEmailAddress(emailAddress)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Địa chỉ email gửi không đúng định dạng.");
			}
			if ("".equals(_id) && "".equals(emailPassword)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Vui lòng nhập vào mật khẩu email gửi.");
			}

			break;
		case "configActive":
		case "configDeActive":
		case "configDelete":
		case "configHotnews":
			if ("".equals(_id)) {
				dto.setErrorCode(1);
				dto.getErrorMessages().add("Không tìm thấy ID.");
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
		case "configCreate":
			messageConfirm = "Bạn có chắc chắn muốn thêm mới không?";
			break;
		case "configEdit":
			messageConfirm = "Bạn có chắc chắn muốn thay đổi không?";
			break;
		case "configActive":
			messageConfirm = "Bạn có chắc chắn muốn kích hoạt không?";
			break;
		case "configDeActive":
			messageConfirm = "Bạn có chắc chắn muốn hủy kích hoạt không?";
			break;
		case "configDelete":
			messageConfirm = "Bạn có chắc chắn muốn xóa không?";
			break;
		case "configHotnews":
			messageConfirm = "Bạn có chắc chắn muốn thực hiện không không?";
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
		case "configCreate":
			actionCode = Constants.MSG_ACTION_CODE.CREATED;
			break;
		case "configEdit":
			actionCode = Constants.MSG_ACTION_CODE.MODIFY;
			break;
		case "configActive":
			actionCode = Constants.MSG_ACTION_CODE.ACTIVE;
			break;
		case "configDeActive":
			actionCode = Constants.MSG_ACTION_CODE.DEACTIVE;
			break;
		case "configDelete":
			actionCode = Constants.MSG_ACTION_CODE.DELETE;
			break;
		case "configHotnews":
			actionCode = Constants.MSG_ACTION_CODE.HOTNEWS;
			break;
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
		hData.put("CheckAutoSend", checkAutoSend);
		hData.put("CheckSSL", checkSSL);
		hData.put("CheckTLS", checkTLS);
		hData.put("SmtpServer", smtpServer);
		hData.put("SmtpPort", smtpPort);
		hData.put("EmailAddress", emailAddress);
		hData.put("EmailPassword", emailPassword);
		
		msg.setObjData(hData);
		JSONRoot root = new JSONRoot(msg);
		MsgRsp rsp = restAPI.callAPINormal("/configEmail/crud", cup.getLoginRes().getToken(), HttpMethod.POST, root);
		MspResponseStatus rspStatus = rsp.getResponseStatus();
		if(rspStatus.getErrorCode() == 0) {
			dtoRes.setErrorCode(0);
			switch (actionCode) {
			case Constants.MSG_ACTION_CODE.CREATED:
				dtoRes.setResponseData("Thêm mới thành công.");
				break;
			case Constants.MSG_ACTION_CODE.MODIFY:
				dtoRes.setResponseData("Thay đổi thông tin thành công.");
				break;
			case Constants.MSG_ACTION_CODE.ACTIVE:
				dtoRes.setResponseData("Kích hoạt thành công.");
				break;
			case Constants.MSG_ACTION_CODE.DEACTIVE:
				dtoRes.setResponseData("Hủy kích hoạt thành công.");
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
	