package vn.webcapnuoc.hddt.user.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

import vn.webcapnuoc.hddt.user.dao.AbstractDAO;
import vn.webcapnuoc.hddt.user.dao.IndexDAO;
import vn.webcapnuoc.hddt.user.service.JPUtils;
import vn.webcapnuoc.hddt.user.service.TCTNService;
import vn.webcapnuoc.hddt.utility.Constants;
import vn.webcapnuoc.hddt.utility.Json;

@Repository
@Transactional
public class IndexImpl extends AbstractDAO implements IndexDAO {
	private static final Logger log = LogManager.getLogger(IndexImpl.class);
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	TCTNService tctnService;

	@Autowired
	JPUtils jpUtils;
	Document docUpsert = null;
	LocalDateTime time  = LocalDateTime.now();
	

	@Override
	public MsgRsp getLogo(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		List<Document> pipeline = new ArrayList<Document>();


		Document docFind = new Document("ActiveFlag", true).append("HotNews", true).append("IsDelete", false);
		
		cursor = mongoTemplate.getCollection("Logo").find(docFind).limit(1);
		iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				objectId = (ObjectId) docTmp.get("_id");
				hItem = new HashMap<String, Object>();
				hItem.put("_id", objectId.toString());
				hItem.put("Title", docTmp.get("Title", ""));
				hItem.put("ImageLogo", docTmp.get("ImageLogo", "img-logo"));				
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}


	@Override
	public MsgRsp getPath(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		Document docFind = new Document("ActiveFlag", true).append("IsDelete", false);
		
		cursor = mongoTemplate.getCollection("Path").find(docFind).limit(1);
		iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				hItem = new HashMap<String, Object>();	
				hItem.put("dir_images", docTmp.get("dir_images", ""));
				hItem.put("dir_tmp", docTmp.get("dir_tmp", ""));				
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}
	
	
	@Override
	public MsgRsp getFooter(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(
			new Document("$limit", 1)
		);
		pipeline.add(
			new Document("$project", new Document("_id", 1))
		);
		
		/*NEWS*/
		pipeline.add(
			new Document("$lookup", 
				new Document("from", "Footers")
				.append("pipeline", 
					Arrays.asList(
							new Document("$match", new Document("ActiveFlag", true).append("IsDelete", false))				
							, new Document("$sort", new Document("_id", -1))		
							, new Document("$limit", 1)
						)
					)
					.append("as", "HotFooter")
			)
		);
		pipeline.add(
				new Document("$unwind", new Document("path", "$HotFooter").append("preserveNullAndEmptyArrays", true)));
		/*END - NEWS*/
		
		 cursor = mongoTemplate.getCollection("Users").aggregate(pipeline).allowDiskUse(true);
		 iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				hItem = new HashMap<String, Object>();						
				hItem.put("Description", docTmp.getEmbedded(Arrays.asList("HotFooter","Description"), ""));
				hItem.put("CopyRight", docTmp.getEmbedded(Arrays.asList("HotFooter","CopyRight"), ""));
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}
	
	
	@Override
	public MsgRsp getHeader(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		
Document docFind = new Document("ActiveFlag", true).append("IsDelete", false);
		
		cursor = mongoTemplate.getCollection("Header").find(docFind).limit(1);
		iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				hItem = new HashMap<String, Object>();						
				hItem.put("Time", docTmp.get("Time",""));
				hItem.put("Address", docTmp.get("Address",""));
				hItem.put("Phone", docTmp.get("Phone",""));	
				hItem.put("Email", docTmp.get("Email",""));
				hItem.put("Map", docTmp.get("Map",""));
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}
	
	
	
	@Override
	public MsgRsp getIntroduce(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(
			new Document("$limit", 1)
		);
		pipeline.add(
			new Document("$project", new Document("_id", 1))
		);
		
		/*NEWS*/
		pipeline.add(
			new Document("$lookup", 
				new Document("from", "Introduce")
				.append("pipeline", 
					Arrays.asList(
							new Document("$match", new Document("ActiveFlag", true))			
							, new Document("$sort", new Document("_id", -1))
							, new Document("$limit", 1)
						)
					)
					.append("as", "IntroduceInfo")
			)
		);
		pipeline.add(
				new Document("$unwind", new Document("path", "$IntroduceInfo").append("preserveNullAndEmptyArrays", true)));
		/*END - NEWS*/
		
		 cursor = mongoTemplate.getCollection("Users").aggregate(pipeline).allowDiskUse(true);
		 iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				hItem = new HashMap<String, Object>();								
				hItem.put("Title", docTmp.getEmbedded(Arrays.asList("IntroduceInfo","Title"), ""));				
				hItem.put("Content", docTmp.getEmbedded(Arrays.asList("IntroduceInfo","Content"), ""));
				hItem.put("ImageLogo", docTmp.getEmbedded(Arrays.asList("IntroduceInfo","ImageLogo"), ""));
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}
	
	
	@Override
	public MsgRsp getTitle(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		
		Document docFind = new Document("ActiveFlag", true).append("IsDelete", false);
		
		cursor = mongoTemplate.getCollection("Titles").find(docFind).limit(1);
		iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				hItem = new HashMap<String, Object>();						
				hItem.put("Title_user", docTmp.get("Title_user",""));
				hItem.put("Title_admin", docTmp.get("Title_admin",""));		
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}
	
	@Override
	public MsgRsp getFont(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
	
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		
		Document docFind = new Document("ActiveFlag", true).append("IsDelete", false);
		
		cursor = mongoTemplate.getCollection("Font").find(docFind).limit(1);
		iter = cursor.iterator();
		if(iter.hasNext()) {
			docTmp = iter.next();
		}

		rsp = new MsgRsp(header);
		responseStatus = null;
		
		if (null == docTmp) {
			responseStatus = new MspResponseStatus(9999, Constants.MAP_ERROR.get(9999));
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}


		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
				hItem = new HashMap<String, Object>();						
				hItem.put("LoaiFontText", docTmp.get("LoaiFontText",""));
				
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;
	}
	
	
	@Override
	public MsgRsp getEBookHot(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
		page.setSize(1000);
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		List<Document> pipeline = new ArrayList<Document>();
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", new Document("ActiveFlag", true)
				.append("HotNews", true)
				.append("IsDelete", false)));
		pipeline.add(new Document("$sort",
				new Document("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("EBook").aggregate(pipeline).allowDiskUse(true);
		 iter = cursor.iterator();
		if(iter.hasNext()) {
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
				hItem.put("Title", doc.get("Title", ""));
				hItem.put("SummaryContent", doc.get("SummaryContent", ""));
				hItem.put("User_Asset", doc.get("User_Asset", ""));
				hItem.put("Price", doc.get("Price", ""));
				hItem.put("LoaiSachText", doc.get("LoaiSachText", ""));
				hItem.put("ImageLogo", doc.get("ImageLogo", "img-logo"));
				//hItem.put("_id", doc.get("_id", ""));			
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
	

	@Override
	public MsgRsp contact(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
		
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;
		if (objData != null) {
		jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} else {
			throw new Exception("Lỗi dữ liệu đầu vào");
		}
		
		String name = commons.getTextJsonNode(jsonData.at("/Name")).replaceAll("\\s", " ");
		String email = commons.getTextJsonNode(jsonData.at("/Email")).replaceAll("\\s", "");
		String phone = commons.getTextJsonNode(jsonData.at("/Phone")).replaceAll("\\s", "");
		String description = commons.getTextJsonNode(jsonData.at("/Description")).replaceAll("\\s", " ");

		ObjectId objectId = null;
		objectId = new ObjectId();
		LocalDateTime date = LocalDateTime.now();
		Document docUpsert = new Document("_id", objectId)
				.append("Name", name)
				.append("Email", email)
				.append("Phone", phone)
				.append("Description", description)
				.append("Date", date);
		/* END - LUU DU LIEU HD */
		mongoTemplate.getCollection("Contact").insertOne(docUpsert);
	
		responseStatus = new MspResponseStatus(0, Constants.MAP_ERROR.get(0));
		rsp.setResponseStatus(responseStatus);
		return rsp;
	}

	
	@Override
	public MsgRsp getBlogMain(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
		page.setSize(1000);
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		List<Document> pipeline = new ArrayList<Document>();
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", new Document("ActiveFlag", true)
				.append("HotNews", true)
				.append("IsDelete", false)));
		pipeline.add(new Document("$sort",
				new Document("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("Blog").aggregate(pipeline).allowDiskUse(true);
		 iter = cursor.iterator();
		if(iter.hasNext()) {
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
				hItem.put("Title", doc.get("Title", ""));
				hItem.put("SummaryContent", doc.get("SummaryContent", ""));
				hItem.put("User_Asset", doc.get("User_Asset", ""));
				hItem.put("ImageLogo", doc.get("ImageLogo", "img-logo"));
				//hItem.put("_id", doc.get("_id", ""));			
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
	
	@Override
	public MsgRsp getBlog(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
		page.setSize(1000);
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		List<Document> pipeline = new ArrayList<Document>();
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", new Document("ActiveFlag", true)			
				.append("IsDelete", false)));
		pipeline.add(new Document("$sort",
				new Document("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("Blog").aggregate(pipeline).allowDiskUse(true);
		 iter = cursor.iterator();
		if(iter.hasNext()) {
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
				hItem.put("Title", doc.get("Title", ""));
				hItem.put("SummaryContent", doc.get("SummaryContent", ""));
				hItem.put("User_Asset", doc.get("User_Asset", ""));
				hItem.put("ImageLogo", doc.get("ImageLogo", "img-logo"));
				//hItem.put("_id", doc.get("_id", ""));			
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
	
	
	@Override
	public MsgRsp getSlide(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		JsonNode jsonData = null;
		page.setSize(1000);
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;

		docTmp = null;
		List<Document> pipeline = new ArrayList<Document>();
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", new Document("ActiveFlag", true)			
				.append("IsDelete", false)));
		pipeline.add(new Document("$sort",
				new Document("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("Slide").aggregate(pipeline).allowDiskUse(true);
		 iter = cursor.iterator();
		if(iter.hasNext()) {
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
				hItem.put("Title", doc.get("Title", ""));				
				hItem.put("ImageLogo", doc.get("ImageLogo", "img-logo"));
				//hItem.put("_id", doc.get("_id", ""));			
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
