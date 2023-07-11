package vn.webcapnuoc.hddt.controller.user.lienhe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Json;

@Controller
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
@RequestMapping("/")
public class LienHeController extends AbstractController{
	@Autowired private RestAPIUtility restAPIUtility;
	private static final Logger log = LogManager.getLogger(LienHeController.class);
	@Autowired RestAPIUtility restAPI;

	@RequestMapping(value = {"/lien-he" }, method = {RequestMethod.GET})
	public String login(CurrentUserProfile cup,Model model, HttpServletRequest request, HttpSession session, 
			@ModelAttribute("message") String message) throws Exception {
		HashMap<String, Object> hTmp = null;
		Document r10 = null;
		List<HashMap<String, Object>> rowsFooter = null;
		List<HashMap<String, Object>> rowsIntroduce = null;
		List<HashMap<String, Object>> rowsPhapLy = null;
		List<HashMap<String, Object>> rowsHoSoPhapLy = null;
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

		//THONG TIN HEADER
		dtoRes = new BaseDTO(request);
		 msg = dtoRes.createMsgPass();
		 hInput = new HashMap<>();
		msg.setObjData(hInput);
		 root = new JSONRoot(msg);
		 rsp = restAPI.callAPIPass("/index/getHeader", HttpMethod.POST, root);
		 rspStatus = rsp.getResponseStatus();
		 rows = null;

		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
			for(JsonNode row: rows) {	
			request.setAttribute("Time_header", commons.getTextJsonNode(row.at("/Time")));
			request.setAttribute("Address_header", commons.getTextJsonNode(row.at("/Address")));
			request.setAttribute("Phone_header", commons.getTextJsonNode(row.at("/Phone")));		
			request.setAttribute("Email_header", commons.getTextJsonNode(row.at("/Email")));
			request.setAttribute("Map_header", commons.getTextJsonNode(row.at("/Map")));
			}			
		}
	}
		
		
		
		//THONG TIN FOOTER
		 dtoRes = new BaseDTO(request);
		 msg = dtoRes.createMsgPass();
		 hInput = new HashMap<>();
		msg.setObjData(hInput);
		 root = new JSONRoot(msg);
		 rsp = restAPI.callAPIPass("/index/getFooter", HttpMethod.POST, root);
		 rspStatus = rsp.getResponseStatus();
		 rows = null;

		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
			rowsFooter = new ArrayList<HashMap<String,Object>>();
			for(JsonNode row: rows) {
			hTmp = new HashMap<String, Object>();
			hTmp.put("Title", commons.getTextJsonNode(row.at("/Title")));
			hTmp.put("Map", commons.getTextJsonNode(row.at("/Map")));
			hTmp.put("Description", commons.decodeURIComponent(commons.getTextJsonNode(row.at("/Description"))));
			hTmp.put("Copyright", commons.getTextJsonNode(row.at("/CopyRight")));			
			rowsFooter.add(hTmp);
			}
			request.setAttribute("_footer", rowsFooter);
			}
		}

		
		//THONG TIN TIEU DE
		dtoRes = new BaseDTO(request);
		 msg = dtoRes.createMsgPass();
		 hInput = new HashMap<>();
		msg.setObjData(hInput);
		 root = new JSONRoot(msg);
		 rsp = restAPI.callAPIPass("/index/getTitle", HttpMethod.POST, root);
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
		
		//THONG TIN PHONG CHU
		dtoRes = new BaseDTO(request);
		 msg = dtoRes.createMsgPass();
		 hInput = new HashMap<>();
		msg.setObjData(hInput);
		 root = new JSONRoot(msg);
		 rsp = restAPI.callAPIPass("/index/getFont", HttpMethod.POST, root);
		 rspStatus = rsp.getResponseStatus();
		 rows = null;

		if(rspStatus.getErrorCode() == 0) {
			JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
			if(!jsonData.at("/rows").isMissingNode()) {
				rows = jsonData.at("/rows");
			for(JsonNode row: rows) {	
			request.setAttribute("LoaiFontText", commons.getTextJsonNode(row.at("/LoaiFontText")));			
			}			
		}
	}	
		
		
		
		return "user/contact";
	}

}
