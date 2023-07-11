package vn.webcapnuoc.hddt.user.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgHeader;
import com.api.message.MsgPage;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

import vn.webcapnuoc.hddt.dto.MailConfig;
import vn.webcapnuoc.hddt.user.dao.AbstractDAO;
import vn.webcapnuoc.hddt.user.dao.ForgotPassDAO;
import vn.webcapnuoc.hddt.user.service.JPUtils;
import vn.webcapnuoc.hddt.user.service.TCTNService;
import vn.webcapnuoc.hddt.utility.Json;
import vn.webcapnuoc.hddt.utility.MailUtils;

@Repository
@Transactional
public class ForgotPassImpl extends AbstractDAO implements ForgotPassDAO {
	private static final Logger log = LogManager.getLogger(ForgotPassImpl.class);
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	TCTNService tctnService;
	private MailUtils mailUtils = new MailUtils();
	@Autowired
	JPUtils jpUtils;
	Document docUpsert = null;
	LocalDateTime time  = LocalDateTime.now();

	
	public MsgRsp crud(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		Iterable<Document> cursor1 = null;
		Iterator<Document> iter1 = null;
		List<Document> pipeline = null;
		Document docR = null;
		Document docFind = null;
		Document docTmp = null;
		Document docTmp1 = null;
		JsonNode jsonData = null;
		MsgRsp rsp = new MsgRsp(header);
		rsp.setMsgPage(page);
		MspResponseStatus responseStatus = null;
		
		
		
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} else {
			throw new Exception("Lỗi dữ liệu đầu vào");
		}

		String ten = commons.getTextJsonNode(jsonData.at("/UserName"));
		String mail = commons.getTextJsonNode(jsonData.at("/Email"));
		if ("" == ten) {
			responseStatus = new MspResponseStatus(9999, "Tài khoản không được để trống.");
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
		if(!"".equals(mail) && !commons.isValidEmailAddress(mail)) {
			responseStatus = new MspResponseStatus(9999, "Vui lòng nhập mail đúng định dạng.");
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
		docFind = new Document("UserName",ten).append("Email",mail); 
		 docTmp = null;
		cursor = mongoTemplate.getCollection("Users").find(docFind);
		 iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, "Thông tin nhập vào chưa đúng. Vui lòng nhập lại thông tin chính xác!");
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
		
		ObjectId idUser  = 	(ObjectId) docTmp.get("_id");
		String pass = docTmp.getString("Password");
		String token = commons.csRandomAlphaNumbericString(50);
		String otp = commons.csRandomNumbericString(4);
		
		
		/*INSERT DATA IN SYSTEM-GETPASSWORD*/
		ObjectId objectId = null;
		objectId = new ObjectId();
		Document docUpsert = new Document("_id", objectId)
				.append("UserName", ten)
				.append("Email", mail)
				.append("Type", "USER")
				.append("IsActive", false)
				.append("Token", token)
				.append("OTP", otp)								
				.append("UserId", idUser.toString());				
		
		mongoTemplate.getCollection("system_getpassword").insertOne(docUpsert);
		/* END - INSERT DATA IN SYSTEM-GETPASSWORD */
		
		
	/*CAU HINH MAIL GUI*/	
		
		cursor1 = mongoTemplate.getCollection("ConfigEmailAdmin").find().limit(1);
		iter1 = cursor1.iterator();
		if(iter1.hasNext()) {
			docTmp1 = iter1.next();
		}	
		String Fullname = docTmp.getString("FullName");
		
		MailConfig mailConfig = new MailConfig(docTmp1);
 		mailConfig.setNameSend(docTmp.getEmbedded(Arrays.asList("UserName"), ""));

		/*THUC HIEN GUI MAIL*/
		String _title =  "Mã xác thực xác nhận quên mật khẩu " + ten;
		String _tmp = "";
		String tk = ten;
		String _content;
		StringBuilder message = new StringBuilder();
		message.setLength(0);
		message.append("<p><span style='font-family: times new roman;font-size: 13px;'>Kính gửi: <label style='font-weight: bold;'>" + ("".equals(Fullname)? ten: Fullname) + "</label><o:p></o:p></span></p>\n");
		message.append("<p><span style='font-family: times new roman;font-size: 13px;'>Bạn vừa yêu cầu mã xác thực lấy lại mật khẩu.</span></p>\n");
		message.append("<p><span style='font-family: times new roman;font-size: 13px;'>Vui lòng nhập mã xác thực <strong>" + otp + "</strong> để lấy lại mật khẩu mới.</span></p>\n");
		message.append("<p><span style='font-family: times new roman;font-size: 13px;'>Trân trọng!</span></p><br />\n");
		message.append("<p style='margin-bottom: 3px;'><span style='font-family: Times New Roman;font-size: 13px;color:red;font-weight: bold;'>QUÝ CÔNG TY VUI LÒNG KHÔNG REPLY EMAIL NÀY!</span></p>");
		_content = message.toString();
		boolean boo = mailUtils.sendMail(mailConfig, _title, _content, mail, null, null, true);			
		responseStatus = new MspResponseStatus(0, token);
		rsp.setResponseStatus(responseStatus);
		return rsp;
	}

	public MsgRsp checktoken(JSONRoot jsonRoot) throws Exception {
		MsgRsp res = new MsgRsp();
		
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		List<Document> pipeline = null;
		Document docR = null;
		Document docFind = null;
		Document docTmp = null;
		JsonNode jsonData = null;
		Iterable<Document> cursor1 = null;
		Iterator<Document> iter1 = null;
		Document docTmp1 = null;
		MsgRsp rsp = new MsgRsp(header);
		rsp.setMsgPage(page);
		MspResponseStatus responseStatus = null;

		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} else {
			throw new Exception("Lỗi dữ liệu đầu vào");
		}
		String token = commons.getTextJsonNode(jsonData.at("/token"));
		String otp = commons.getTextJsonNode(jsonData.at("/otp"));					
		String sysOtp = "";
		String sysUserName = "";
		String sysFullName = "";
		String sysEmail = "";
		String IssuID = "";
		int sysErrorsCount = -1;
		boolean sysIsActive = false;
		long sysSecondCount = 0L;
		long agentId = 0L;
		
		docFind = new Document("Token",token).append("OTP", otp); 
		 docTmp = null;
		cursor = mongoTemplate.getCollection("system_getpassword").find(docFind);
		 iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, "Thông tin nhập vào chưa đúng. Vui lòng nhập lại OTP đúng!");
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
//		TIMESTAMPDIFF(SECOND, s.SystemDate, NOW())
		sysOtp = docTmp.getString("otp");
		sysUserName = docTmp.getString("UserName");
		sysEmail = docTmp.getString("Email");
		sysIsActive = docTmp.getBoolean("IsActive");
		IssuID = docTmp.getString("UserId");
//		sysSecondCount = docTmp.getLong("SecondCount");
		
		
		
		
		/*THUC HIEN RESET MAT KHAU*/
		String password = "";
		String 	secureKey = commons.csRandomString(8);	
		String passwordInput = commons.generateSHA(sysUserName + secureKey, false).toUpperCase();
		int size = passwordInput.length();
		if(size < 128) {
		password = "0"+ passwordInput;
		}else {
		password = passwordInput;
		}
		/*CAP NHAT THONG TIN MAT KHAU*/
		FindOneAndUpdateOptions options = null;
		options = new FindOneAndUpdateOptions();
		options.upsert(false);
		options.maxTime(5000, TimeUnit.MILLISECONDS);
		options.returnDocument(ReturnDocument.AFTER);
		
		Document docFind1 = new Document("Email",sysEmail).append("UserName",sysUserName); 
		docR = mongoTemplate.getCollection("Users").findOneAndUpdate(
				docFind1,
				new Document("$set", 
						new Document("Password", password)
				),
			options
		);
		
		
		/*CAP NHAT TRANG THAI TOKEN DA THUC HIEN*/
		FindOneAndUpdateOptions options1 = null;
		options1 = new FindOneAndUpdateOptions();
		options1.upsert(false);
		options1.maxTime(5000, TimeUnit.MILLISECONDS);
		options1.returnDocument(ReturnDocument.AFTER);
		
		Document docFind2 = new Document("Email",sysEmail).append("UserName",sysUserName); 
		docR = mongoTemplate.getCollection("system_getpassword").findOneAndUpdate(
				docFind2,
				new Document("$set", 
						new Document("IsActive", true)
				),
			options1
		);
		/*CAU HINH MAIL GUI*/
		ObjectId idissu1  = (ObjectId) docTmp.get("_id");		
		docFind = null;	
		 docTmp = null;
		 pipeline = new ArrayList<Document>();
			pipeline.add(new Document("$match", docFind1));

		 
		
		cursor = mongoTemplate.getCollection("Users").aggregate(pipeline);
		iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}		
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(999, "Không tìm thấy thông tin tài khoản.");
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
		

		cursor1 = mongoTemplate.getCollection("ConfigEmailAdmin").find().limit(1);
		iter1 = cursor1.iterator();
		if(iter1.hasNext()) {
			docTmp1 = iter1.next();
		}
		MailConfig mailConfig = new MailConfig(docTmp1);
		mailConfig.setNameSend(docTmp.getEmbedded(Arrays.asList("UserName"), ""));

		/*THUC HIEN GUI MAIL*/
		String _title =  " Thông báo cấp lại mật khẩu";
		String _tmp = "";
		String tk = sysUserName;
		String _content;
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'>Kính gửi: <label style='font-weight: bold;'>" + ("".equals(_tmp)? "Quý khách hàng": _tmp) + "</label><o:p></o:p></span></p>\n");
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'><label style='font-weight: bold;'>" + _title + "</label> Xin chân thành cảm ơn Quý đơn vị đã tin tưởng và sử dụng sản phẩm/dịch vụ của chúng tôi!</span></p>\n");
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'>Chúng tôi vừa cập nhật lại mật khẩu của Quý đơn vị với thông tin như sau:</span></p>\n");
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'>1.  Tài khoản:  " + tk + "</span></p>\n");
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'>2.  Mật khẩu:  " + secureKey + "</span></p>\n");
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'>Quý đơn vị  vui lòng bảo mật thông tin và thay đổi mật khẩu sau khi đăng nhập.</span></p>\n");
		sb.append("<p><span style='font-family: Times New Roman;font-size: 13px;'>Trân trọng kính chào!</span></p>");
		sb.append("<hr style='margin: 5px 0 5px 0;'>");
		sb.append("<p style='margin-bottom: 3px;'><span style='font-family: Times New Roman;font-size: 13px;color:red;font-weight: bold;'>QUÝ CÔNG TY VUI LÒNG KHÔNG REPLY EMAIL NÀY!</span></p>");
		 _content = sb.toString();
		boolean boo = mailUtils.sendMail(mailConfig, _title, _content, sysEmail, null, null, true);

		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);
		return rsp;
	}

	
	
	
	
}
