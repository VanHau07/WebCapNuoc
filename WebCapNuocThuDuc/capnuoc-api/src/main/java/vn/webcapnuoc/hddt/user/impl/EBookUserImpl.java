package vn.webcapnuoc.hddt.user.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.mongodb.client.model.FindOneAndUpdateOptions;

import vn.webcapnuoc.hddt.user.dao.AbstractDAO;
import vn.webcapnuoc.hddt.user.dao.EBookDAO;
import vn.webcapnuoc.hddt.user.service.JPUtils;
import vn.webcapnuoc.hddt.user.service.TCTNService;
import vn.webcapnuoc.hddt.utility.Constants;

@Repository
@Transactional
public class EBookUserImpl extends AbstractDAO implements EBookDAO {
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
	public MsgRsp getEBook(JSONRoot jsonRoot) throws Exception {
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
	
	public MsgRsp getEBookReadOnline(JSONRoot jsonRoot) throws Exception {
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
				.append("ReadOnline", true)
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
		Iterable<Document> cursor = mongoTemplate.getCollection("EBook").find(docFind);
		Iterator<Document> iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}

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
				hItem.put("Price", docTmp.get("Price", ""));			
				hItem.put("ImageLogo", docTmp.get("ImageLogo", "img-logo"));
				hItem.put("ImageLogo1", docTmp.get("ImageLogo1", "img-logo"));
				hItem.put("SoTrang", docTmp.get("SoTrang", ""));
				hItem.put("Year", docTmp.get("Year", ""));
				hItem.put("TacGia", docTmp.get("TacGia", ""));
				hItem.put("SummaryContent", docTmp.get("SummaryContent", ""));
				hItem.put("ReadOnline", docTmp.get("ReadOnline", ""));
				hItem.put("LoaiSachText", docTmp.get("LoaiSachText", ""));							
				hItem.put("Content", docTmp.get("Content", ""));
				hItem.put("ViewCount", docTmp.get("ViewCount", 0).toString());
				hItem.put("InfoCreated", docTmp.get("InfoCreated", ""));
				rowsReturn.add(hItem);			
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		
		
		/*TANG ViewCount +1*/
		if(null != docTmp) {
			mongoTemplate.getCollection("EBook").findOneAndUpdate(
				docFind
				, new Document("$inc", 
					new Document("ViewCount", 1)
				)
				, new FindOneAndUpdateOptions().upsert(false)
			);
		}
		return rsp;
	}

	
	public MsgRsp check(JSONRoot jsonRoot, String name) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();

		MsgRsp rsp = new MsgRsp(header);
		rsp.setMsgPage(page);
		MspResponseStatus responseStatus = null;
		
		page.setSize(1000);

		Document docFind = null;
		
		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		List<Document> pipeline = new ArrayList<Document>();

		Document docMatch = new Document("IsDelete",
				new Document("$ne", true));
		if(!"".equals(name))
		docMatch.append("Title", new Document("$regex", commons.regexEscapeForMongoQuery(name)).append("$options", "i"));
	
		pipeline = new ArrayList<Document>();
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docMatch));
		pipeline.add(new Document("$sort",
				new Document("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("EBook").aggregate(pipeline).allowDiskUse(true);
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

		int dem = docTmp.getInteger("total", 0);
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
				hItem.put("Price", doc.get("Price", ""));
				hItem.put("LoaiSachText", doc.get("LoaiSachText", ""));
				hItem.put("ImageLogo", doc.get("ImageLogo", "img-logo"));
				rowsReturn.add(hItem);
			}
		}
		
		String dem_ = String.valueOf(dem);
		
		responseStatus = new MspResponseStatus(0, dem_);
		rsp.setResponseStatus(responseStatus);

		HashMap<String, Object> mapDataR = new HashMap<String, Object>();
		mapDataR.put("rows", rowsReturn);
		rsp.setObjData(mapDataR);
		return rsp;

	}

}
