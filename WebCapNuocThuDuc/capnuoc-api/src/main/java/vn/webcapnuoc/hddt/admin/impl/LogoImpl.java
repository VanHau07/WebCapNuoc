package vn.webcapnuoc.hddt.admin.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

import vn.webcapnuoc.hddt.admin.dao.LogoDAO;
import vn.webcapnuoc.hddt.user.dao.AbstractDAO;
import vn.webcapnuoc.hddt.user.service.JPUtils;
import vn.webcapnuoc.hddt.user.service.TCTNService;
import vn.webcapnuoc.hddt.utility.Constants;
import vn.webcapnuoc.hddt.utility.Json;
import vn.webcapnuoc.hddt.utility.SystemParams;

@Repository
@Transactional
public class LogoImpl extends AbstractDAO implements LogoDAO {
	private static final Logger log = LogManager.getLogger(LogoImpl.class);
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	TCTNService tctnService;

	@Autowired
	JPUtils jpUtils;
	Document docUpsert = null;
	LocalDateTime time  = LocalDateTime.now();

	
	public MsgRsp crud(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();

		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} else {
			throw new Exception("Lỗi dữ liệu đầu vào");
		}

		String actionCode = header.getActionCode();
		String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
		String title = commons.getTextJsonNode(jsonData.at("/Title")).replaceAll("\\s", " ");
		String attachFileName = commons.getTextJsonNode(jsonData.at("/AttachFileName")).replaceAll("\\s", "");
		String attachFileNameSystem = commons.getTextJsonNode(jsonData.at("/AttachFileNameSystem")).replaceAll("\\s", "");
	
		MsgRsp rsp = new MsgRsp(header);
		rsp.setMsgPage(page);
		MspResponseStatus responseStatus = null;
		Document docTmp = null;
		Document docFind = null;
		Document docFind1 = null;
		Document docTmp1 = null;
		ObjectId objectId = null;
		FindOneAndUpdateOptions options = null;
		
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		Iterable<Document> cursor1 = null;
		Iterator<Document> iter1 = null;
		boolean activeFlag = false;
		
		boolean copyFile = false;
		File file = null;
		
		
		try {
			switch (actionCode) {	
			case Constants.MSG_ACTION_CODE.CREATED:		

				file = new File(SystemParams.DIR_TMP, attachFileNameSystem);
				/*COPY FILE LOGO QUA THU MUC CHINH*/
				if(file.exists() && file.isFile()) {
					copyFile = commons.copyFileUsingStream(file, new File(SystemParams.DIR_IMAGES, attachFileNameSystem));
				}
				/*END - COPY FILE LOGO QUA THU MUC CHINH*/

				docTmp = new Document("Title", title)	
						.append("ImageLogo", attachFileNameSystem)
						.append("ImageLogoOriginalFilename", attachFileName)	
					.append("ActiveFlag", false)
					.append("IsDelete", false)				
					.append("InfoCreated", 
						new Document("CreateDate", LocalDateTime.now())
						.append("CreateUserID", header.getUserId())
							.append("CreateUserName", header.getUserName())
							.append("CreateUserFullName", header.getUserFullName())
					);
				mongoTemplate.getCollection("Logo").insertOne(docTmp);

				responseStatus = new MspResponseStatus(0, Constants.MAP_ERROR.get(0));
				rsp.setResponseStatus(responseStatus);
				return rsp;
			case Constants.MSG_ACTION_CODE.MODIFY:
				objectId = null;
				try {
					objectId = new ObjectId(_id);
				}catch(Exception e) {}
				
				docFind = new Document("_id", objectId)
						.append("IsDelete", new Document("$ne", true));
				cursor = mongoTemplate.getCollection("Logo").find(docFind);
				iter = cursor.iterator();
				if(iter.hasNext()) {
					docTmp = iter.next();
				}
				if(null == docTmp){
				
					responseStatus = new MspResponseStatus(9999, "Bài viết không tồn tại trong hệ thống.");
					rsp.setResponseStatus(responseStatus);
					return rsp;
				}
				
				if(docTmp.getBoolean("ActiveFlag", false)) {			
					responseStatus = new MspResponseStatus(9999, "Bài viết đã được kích hoạt. Không thể thực hiện thay đổi.");
					rsp.setResponseStatus(responseStatus);
					return rsp;
				}
				
			
				
				options = new FindOneAndUpdateOptions();
				options.upsert(false);
				options.maxTime(5000, TimeUnit.MILLISECONDS);
				options.returnDocument(ReturnDocument.AFTER);
				file = new File(SystemParams.DIR_TMP, attachFileNameSystem);
				/*COPY FILE LOGO QUA THU MUC CHINH*/
				if(file.exists() && file.isFile()) {
					copyFile = commons.copyFileUsingStream(file, new File(SystemParams.DIR_IMAGES, attachFileNameSystem));
				}
				docTmp = mongoTemplate.getCollection("Logo").findOneAndUpdate(
						docTmp
						, new Document("$set", 
							new Document("Title", title)		
							.append("ImageLogo", attachFileNameSystem)
							.append("ImageLogoOriginalFilename", attachFileName)
							.append("InfoUpdated", 
								new Document("UpdatedDate", LocalDateTime.now())
								.append("UpdatedUserID", header.getUserId())
								.append("UpdatedUserName", header.getUserName())
								.append("UpdatedUserFullName", header.getUserFullName())
							)
						)
						, options
					);
				
				responseStatus = new MspResponseStatus(0, Constants.MAP_ERROR.get(0));
				rsp.setResponseStatus(responseStatus);
				return rsp;
			case Constants.MSG_ACTION_CODE.ACTIVE:
			case Constants.MSG_ACTION_CODE.DEACTIVE:
			case Constants.MSG_ACTION_CODE.DELETE:
			case Constants.MSG_ACTION_CODE.HOTNEWS:
				objectId = null;
				try {
					objectId = new ObjectId(_id);
				}catch(Exception e) {}
				docFind = new Document("_id", objectId).append("IsDelete", new Document("$ne", true));
				
				cursor = mongoTemplate.getCollection("Logo").find(docFind);
		
				docTmp = null;
				iter = cursor.iterator();
				if(iter.hasNext()) {
					docTmp = iter.next();
				}
			
				if(null == docTmp){				
					responseStatus = new MspResponseStatus(9999, "Bài viết không tồn tại trong hệ thống.");
					rsp.setResponseStatus(responseStatus);
					return rsp;
				}
				
				activeFlag = false;
				if(null != docTmp.get("ActiveFlag") && docTmp.get("ActiveFlag") instanceof Boolean) {
					activeFlag = docTmp.getBoolean("ActiveFlag", false);
				}
				
				if((actionCode.equals(Constants.MSG_ACTION_CODE.ACTIVE) || actionCode.equals(Constants.MSG_ACTION_CODE.DELETE)) 
						&& activeFlag) {				
					responseStatus = new MspResponseStatus(9999, "Bài viết này đã được kích hoạt.");
					rsp.setResponseStatus(responseStatus);
					return rsp;
				}else if(actionCode.equals(Constants.MSG_ACTION_CODE.DEACTIVE) && !activeFlag) {				
					responseStatus = new MspResponseStatus(9999, "Bài viết này chưa được kích hoạt.");
					rsp.setResponseStatus(responseStatus);
					return rsp;
				}
				
				boolean isHotNews = false;
				if(null != docTmp.get("HotNews") && docTmp.get("HotNews") instanceof Boolean) {
					isHotNews = docTmp.getBoolean("HotNews", false);
				}
				
				options = new FindOneAndUpdateOptions();
				options.upsert(false);
				options.maxTime(5000, TimeUnit.MILLISECONDS);
				options.returnDocument(ReturnDocument.AFTER);
				
				switch (actionCode) {
				case Constants.MSG_ACTION_CODE.ACTIVE:
					mongoTemplate.getCollection("Logo").findOneAndUpdate(
							docFind
							, new Document("$set", 
									new Document("ActiveFlag", true)
									.append("InfoActived", 
											new Document("ActivedDate", LocalDateTime.now())
												.append("ActivedUserID", header.getUserId())
												.append("ActivedUserName", header.getUserName())
												.append("ActivedUserFullName", header.getUserFullName())
											)
									)
							, options);
					break;
				case Constants.MSG_ACTION_CODE.DEACTIVE:
					mongoTemplate.getCollection("Logo").findOneAndUpdate(
							docFind
							, new Document("$set", 
									new Document("ActiveFlag", false)
									.append("InfoDeActived", 
											new Document("DeActivedDate", LocalDateTime.now())
												.append("DeActivedUserID",  header.getUserId())
												.append("DeActivedUserName", header.getUserName())
												.append("DeActivedUserFullName", header.getUserFullName())
											)
									)
							, options);
					break;
				case Constants.MSG_ACTION_CODE.DELETE:
					mongoTemplate.getCollection("Logo").findOneAndUpdate(
							docFind
							, new Document("$set", 
									new Document("IsDelete", true)
									.append("InfoDeleted", 
											new Document("DeletedDate", LocalDateTime.now())
												.append("DeletedUserID", header.getUserId())
												.append("DeletedUserName", header.getUserName())
												.append("DeletedUserFullName", header.getUserFullName())
											)
									)
							, options);
					break;
				case Constants.MSG_ACTION_CODE.HOTNEWS:
					mongoTemplate.getCollection("Logo").findOneAndUpdate(
							docFind
							, new Document("$set", 
									new Document("HotNews", !isHotNews)
									.append("InfoHotNews", 
											new Document("HotNewsDate", LocalDateTime.now())
												.append("HotNewsUserID", header.getUserId())
												.append("HotNewsUserName", header.getUserName())
												.append("HotNewsUserFullName", header.getUserFullName())
											)
									)
							, options);
					break;
				default:
					break;
				}
				
				responseStatus = new MspResponseStatus(0, Constants.MAP_ERROR.get(0));
				rsp.setResponseStatus(responseStatus);
				return rsp;
				
			default:
				responseStatus = new MspResponseStatus(999, Constants.MAP_ERROR.get(0));
				rsp.setResponseStatus(responseStatus);
				return rsp;
				
			}
		}catch(Exception e) {
			throw e;
		}finally {
		}
	}

	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();

		MsgRsp rsp = new MsgRsp(header);
		rsp.setMsgPage(page);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		try {
			objectId = new ObjectId(_id);
		} catch (Exception e) {
		}

		Document docFind = new Document("IsDelete", new Document("$ne", true))
				.append("_id", objectId);

		Document docTmp = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("Logo").find(docFind);
		Iterator<Document> iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}

		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
		
		rsp.setObjData(docTmp);
		responseStatus = new MspResponseStatus(0, Constants.MAP_ERROR.get(0));
		rsp.setResponseStatus(responseStatus);
		return rsp;
	}

	
	@Override
	public MsgRsp list(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();

		
		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(objData);

		}
		
		String actionCode = header.getActionCode();
		String title = commons.getTextJsonNode(jsonData.at("/Title")).replaceAll("\\s", " ");

		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		List<Document> pipeline = new ArrayList<Document>();

		Document docMatch = new Document("IsDelete",
				new Document("$ne", true));
		docMatch.append("Title", new Document("$regex", commons.regexEscapeForMongoQuery(title)).append("$options", "i"));
	
		pipeline = new ArrayList<Document>();
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docMatch));
		pipeline.add(new Document("$sort",
				new Document("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("Logo").aggregate(pipeline).allowDiskUse(true);
		iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}

		page.setTotalRows(docTmp.getInteger("total", 0));
		rsp.setMsgPage(page);

		List<Document> rows = null;
		if (docTmp.get("data") != null && docTmp.get("data") instanceof List) {
			rows = docTmp.getList("data", Document.class);
		}

		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
		if (null != rows) {
			for (Document doc : rows) {
				objectId = (ObjectId) doc.get("_id");

				hItem = new HashMap<String, Object>();
				hItem.put("_id", objectId.toString());
				hItem.put("Title", doc.get("Title"));		
				hItem.put("ActiveFlag", doc.get("ActiveFlag"));				
				hItem.put("InfoCreated", doc.get("InfoCreated"));
				hItem.put("HotNewsDesc", doc.get("HotNewsDesc"));
				hItem.put("HotNews", doc.get("HotNews"));
				rowsReturn.add(hItem);
			}
		}
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}

}
