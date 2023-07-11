package vn.webcapnuoc.hddt.user.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import com.api.message.MsgParam;
import com.api.message.MsgParams;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import vn.webcapnuoc.hddt.dto.FileInfo;
import vn.webcapnuoc.hddt.user.dao.AbstractDAO;
import vn.webcapnuoc.hddt.user.dao.CommonDAO;
import vn.webcapnuoc.hddt.user.service.JPUtils;
import vn.webcapnuoc.hddt.utility.Constants;
import vn.webcapnuoc.hddt.utility.Json;
import vn.webcapnuoc.hddt.utility.SystemParams;

@Repository
@Transactional
public class CommonImpl extends AbstractDAO implements CommonDAO {
	private static final Logger log = LogManager.getLogger(CommonImpl.class);
	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	JPUtils jpUtils;

	/*
	 * db.getCollection('ApiLicenseKey').aggregate([ {$limit: 1}, {$project: {_id:
	 * 1}},
	 * 
	 * ]);
	 * 
	 * db.getCollection('ApiLicenseKey').aggregate([ {$limit: 1}, {$project: {_id:
	 * 1}}, {$lookup: { from: 'DMTinhThanh', pipeline: [ {$match: {IsDelete: {$ne:
	 * true}}}, {$project: {_id: 0}}, {$sort: {code: 1}} ], as: 'KeyDMTinhThanh' }
	 * }, {$lookup: { from: 'DMChiCucThue', pipeline: [ {$match: {IsDelete: {$ne:
	 * true},tinhthanh_ma: '101'}}, {$project: {_id: 0}}, {$sort: {code: 1}} ], as:
	 * 'KeyDMChiCucThue' } }, {$lookup: { from: 'DMPaymentType', pipeline: [
	 * {$match: {IsDelete: {$ne: true}}}, {$sort: {order: 1}}, {$project: {_id: 0,
	 * code: 1, name: 1}} ], as: 'DMPaymentType' } }, {$lookup: { from:
	 * 'DMMauSoKyHieu', pipeline: [ {$match: {NamPhatHanh: 2021,IsDelete: {$ne:
	 * true}, 'IssuerId' : '61b851ebb0228bba71fca2ec'}}, {$sort: {_id: 1}},
	 * {$project: {_id: {$toString: '$_id'}, KHMSHDon: 1, KHHDon: 1}} ], as:
	 * 'DMMauSoKyHieu' } }, {$lookup: { from: 'DMCurrencies', let: {}, pipeline: [
	 * {$match: {IsDelete: {$ne: true}}}, {$project: {_id: 0}}, {$sort: {order: 1}}
	 * ], as: 'DMCurrencies' } }, {$lookup: { from: 'DMStock', pipeline: [ {$match:
	 * {IsDelete: {$ne: true}, IssuerId: '61b851ebb0228bba71fca2ec'}}, {$addFields:
	 * {Order: {$ifNull: ['$Order', 999]}}}, {$project: {_id: 0}}, {$sort: {Order:
	 * 1, Code: 1}} ], as: 'DMStock' } } ])
	 */

	@Override
	public MsgRsp getFullParams(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();
		
		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;
		
		MsgParams msgParams = null;
		try {
			msgParams = Json.serializer().fromJson(Json.serializer().toString(msg.getObjData()), new TypeReference<MsgParams>() {
			});
		}catch(Exception e) {
			log.error(" >>>>> An exception occurred!", e);
		}
		
		if(null == msgParams || 0 == msgParams.getParams().size()) {
			responseStatus = new MspResponseStatus(9999, "Không tìm thấy dữ liệu.");
			rsp.setResponseStatus(responseStatus);
			return rsp;
		}
		
		try {
			String code01 = "";
			String code02 = "";
			int currentYear = LocalDate.now().get(ChronoField.YEAR);
			
			HashMap<String, String> hashConds = null;
			Document docMatch = null;
			String IssuerId = msg.getMsgHeader().getIssuerId();
			List<Document> pipeline = new ArrayList<Document>();
			pipeline.add(new Document("$limit", 1));
			pipeline.add(new Document("$project", new Document("_id", 1)));
			for(MsgParam msgParam: msgParams.getParams()) {
				switch (msgParam.getParam()) {
				case "DMPersonnel":
					pipeline.add(
						new Document("$lookup", 
							new Document("from", "DMPersonnel")
							.append("pipeline", 
								Arrays.asList(
									new Document("$match", new Document("IsDelete", new Document("$ne", true))),
									new Document("$project", new Document("_id", 0)),
									new Document("$sort", new Document("Code", 1))
								)
							)
							.append("as", msgParam.getId())
						)
					);
					break;	
					

				case "DMFont":
					pipeline.add(
						new Document("$lookup", 
							new Document("from", "DMFont")
							.append("pipeline", 
								Arrays.asList(
									new Document("$match", new Document("IsDelete", new Document("$ne", true))),
									new Document("$project", new Document("_id", 0)),
									new Document("$sort", new Document("Code", 1))
								)
							)
							.append("as", msgParam.getId())
						)
					);
					break;	
				case "DMEBook":
					pipeline.add(
						new Document("$lookup", 
							new Document("from", "DMEBook")
							.append("pipeline", 
								Arrays.asList(
									new Document("$match", new Document("IsDelete", new Document("$ne", true))),
									new Document("$project", new Document("_id", 0)),
									new Document("$sort", new Document("Code", 1))
								)
							)
							.append("as", msgParam.getId())
						)
					);
					break;		
				default:
					break;
				}
			}
			Document docTmp = null;
			Iterable<Document> cursor = mongoTemplate.getCollection("ApiLicenseKey").aggregate(pipeline).allowDiskUse(true);
			Iterator<Document> iter = cursor.iterator();
			if(iter.hasNext()) {
				docTmp = iter.next();
			}
			
			if(null == docTmp) {
				responseStatus = new MspResponseStatus(9999, "Không tìm thấy dữ liệu.");
				rsp.setResponseStatus(responseStatus);
				return rsp;
			}
			
			responseStatus = new MspResponseStatus(0, "SUCCESS");
			rsp.setResponseStatus(responseStatus);
			rsp.setObjData(docTmp);
			return rsp;
		}catch(Exception e) {
			throw e;
		}finally {
		}
	}

	@Override
	public FileInfo printEInvoice(JSONRoot jsonRoot) throws Exception {
		FileInfo fileInfo = new FileInfo();

		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();

		Object objData = msg.getObjData();

		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} else {
			return new FileInfo();
		}

		String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
		String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
		/*
		 * db.getCollection('EInvoice').find( { IssuerId: '61b851ebb0228bba71fca2ec',
		 * _id: ObjectId("61c7cad6f8b593616ce03cc7"),IsDelete: {$ne: true} } )
		 * 
		 * db.getCollection('EInvoice').aggregate([ {$match: { IssuerId:
		 * '61b851ebb0228bba71fca2ec', _id:
		 * ObjectId("61c7cad6f8b593616ce03cc7"),IsDelete: {$ne: true} } }, {$lookup: {
		 * from: 'DMMauSoKyHieu', let: {vIssuerId: '$IssuerId', vMauSoHD:
		 * '$EInvoiceDetail.TTChung.MauSoHD'}, pipeline: [ {$match: { $expr: { $and: [
		 * {$eq: ['$$vIssuerId', '$IssuerId']}, {$eq: [{$toString: '$_id'},
		 * '$$vMauSoHD']} ] } } } ], as: 'DMMauSoKyHieu' } }, {$unwind: {path:
		 * '$DMMauSoKyHieu', preserveNullAndEmptyArrays: true}} ])
		 * 
		 */

		ObjectId objectId = null;
		try {
			objectId = new ObjectId(_id);
		} catch (Exception e) {
		}

		Document docFind = new Document("IssuerId", header.getIssuerId()).append("_id", objectId).append("IsDelete",
				new Document("$ne", true));
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docFind));
		pipeline.add(new Document("$lookup", new Document("from", "DMMauSoKyHieu")
				.append("let",
						new Document("vIssuerId", "$IssuerId").append("vMauSoHD", "$EInvoiceDetail.TTChung.MauSoHD"))
				.append("pipeline",
						Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
								Arrays.asList(new Document("$eq", Arrays.asList("$$vIssuerId", "$IssuerId")),
										new Document("$eq",
												Arrays.asList(new Document("$toString", "$_id"), "$$vMauSoHD"))))))))
				.append("as", "DMMauSoKyHieu")));
		pipeline.add(new Document("$unwind",
				new Document("path", "$DMMauSoKyHieu").append("preserveNullAndEmptyArrays", true)));
		pipeline.add(new Document("$lookup", new Document("from", "UserConFig")
				.append("pipeline", Arrays.asList(new Document("$match", new Document("viewshd", "Y")
						.append("IssuerId", header.getIssuerId()))))
				.append("as", "UserConFig")));
		pipeline.add(new Document("$unwind",
				new Document("path", "$UserConFig").append("preserveNullAndEmptyArrays", true)));
		Document docTmp = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("EInvoice").aggregate(pipeline).allowDiskUse(true);
		Iterator<Document> iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (null == docTmp || docTmp.get("DMMauSoKyHieu") == null) {
			return new FileInfo();
		}

		boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
		boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

		String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
		String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
		String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
		String ImgQA = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgQA"), "");
		String ImgVien = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgVien"), "");
		String CheckView = docTmp.getEmbedded(Arrays.asList("UserConFig", "viewshd"), "");
		String dir = docTmp.get("Dir", "");
		String signStatusCode = docTmp.get("SignStatusCode", "");
		String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
		String MCCQT = docTmp.get("MCCQT", "");
		String fileName = _id + ".xml";
		if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
			fileName = _id + "_" + MCCQT + ".xml";
		} else {
			if ("SIGNED".equals(signStatusCode)) {
				fileName = _id + "_signed.xml";
			}
		}

		File file = new File(dir, fileName);
		if (!file.exists() || !file.isFile()) {
			return new FileInfo();
		}

		org.w3c.dom.Document doc = commons.fileToDocument(file);
		/* TEST REPORT TO PDF */
		String fileNameJP = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "FileName"), "");
		int numberRowInPage = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowsInPage"), 20);
		int numberRowInPageMultiPage = docTmp
				.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowInPageMultiPage"), 26);
		int numberCharsInRow = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "CharsInRow"), 50);

		File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

		ByteArrayOutputStream baosPDF = null;

		baosPDF = jpUtils.createFinalInvoice(fileJP, doc,CheckView, numberRowInPage, numberRowInPageMultiPage, numberCharsInRow,
				MST, Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgLogo).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgBackground).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgQA ).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgVien ).toString(),

				"Y".equals(isConvert), Constants.INVOICE_STATUS.DELETED.equals(eInvoiceStatus), isThayThe, isDieuChinh);
	
		fileInfo.setFileName("EInvoice.pdf");
		fileInfo.setContentFile(baosPDF.toByteArray());

		return fileInfo;
	}

	
	
	
	
	

	@Override
	public FileInfo printEinvoiceAll(JSONRoot jsonRoot) throws Exception {
		FileInfo fileInfo = new FileInfo();
		List<String> ids = null;
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		ByteArrayOutputStream baosPDF = null;
		List<String> listFileNamePdfFinal = new ArrayList<>();
		Object objData = msg.getObjData();

		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} 
		String _id = "";
		String _token = commons.getTextJsonNode(jsonData.at("/_token")).replaceAll("\\s", "");
		String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
		ids = null;
		try {
			ids = Json.serializer().fromJson(commons.decodeBase64ToString(_token), new TypeReference<List<String>>() {
			});
		}catch(Exception e) {}
		
		
		
		for(int i = 0; i< ids.size(); i++ ) {
			_id = ids.get(i) ;
		
		ObjectId objectId = null;
		try {
			objectId = new ObjectId(_id);
		} catch (Exception e) {
		}

		Document docFind = new Document("IssuerId", header.getIssuerId()).append("_id", objectId).append("IsDelete",
				new Document("$ne", true));
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docFind));
		pipeline.add(new Document("$lookup", new Document("from", "DMMauSoKyHieu")
				.append("let",
						new Document("vIssuerId", "$IssuerId").append("vMauSoHD", "$EInvoiceDetail.TTChung.MauSoHD"))
				.append("pipeline",
						Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
								Arrays.asList(new Document("$eq", Arrays.asList("$$vIssuerId", "$IssuerId")),
										new Document("$eq",
												Arrays.asList(new Document("$toString", "$_id"), "$$vMauSoHD"))))))))
				.append("as", "DMMauSoKyHieu")));
		pipeline.add(new Document("$unwind",
				new Document("path", "$DMMauSoKyHieu").append("preserveNullAndEmptyArrays", true)));
		pipeline.add(new Document("$lookup", new Document("from", "UserConFig")
				.append("pipeline", Arrays.asList(new Document("$match", new Document("viewshd", "Y")
						.append("IssuerId", header.getIssuerId()))))
				.append("as", "UserConFig")));
		pipeline.add(new Document("$unwind",
				new Document("path", "$UserConFig").append("preserveNullAndEmptyArrays", true)));
		Document docTmp = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("EInvoice").aggregate(pipeline).allowDiskUse(true);
		Iterator<Document> iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (null == docTmp || docTmp.get("DMMauSoKyHieu") == null) {
			return new FileInfo();
		}

		boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
		boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
		String CheckView = docTmp.getEmbedded(Arrays.asList("UserConFig", "viewshd"), "");
		String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
		String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
		String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
		String ImgQA = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgQA"), "");
		String ImgVien = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgVien"), "");

		String dir = docTmp.get("Dir", "");
		String signStatusCode = docTmp.get("SignStatusCode", "");
		String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
		String MCCQT = docTmp.get("MCCQT", "");
		String fileName = _id + ".xml";
		if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
			fileName = _id + "_" + MCCQT + ".xml";
		} else {
			if ("SIGNED".equals(signStatusCode)) {
				fileName = _id + "_signed.xml";
			}
		}

		File file = new File(dir, fileName);
		if (!file.exists() || !file.isFile()) {
			return new FileInfo();
		}

		org.w3c.dom.Document doc = commons.fileToDocument(file);
		/* TEST REPORT TO PDF */
		String fileNameJP = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "FileName"), "");
		int numberRowInPage = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowsInPage"), 20);
		int numberRowInPageMultiPage = docTmp
				.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowInPageMultiPage"), 26);
		int numberCharsInRow = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "CharsInRow"), 50);
		File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);
		baosPDF = jpUtils.createFinalInvoice(fileJP, doc, CheckView,numberRowInPage, numberRowInPageMultiPage, numberCharsInRow,
				MST, Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgLogo).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgBackground).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgQA ).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgVien ).toString(),

				"Y".equals(isConvert), Constants.INVOICE_STATUS.DELETED.equals(eInvoiceStatus), isThayThe, isDieuChinh);
	
		if(null != baosPDF) {
			file = new File(dir, docTmp.get("_id") + "_final.pdf");
			try (OutputStream  fileOuputStream = new FileOutputStream(file)) {
				baosPDF.writeTo(fileOuputStream);
				listFileNamePdfFinal.add(file.getAbsolutePath());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}

		}
		if(listFileNamePdfFinal.size() == 0) {
			return fileInfo;
		}else {
			ByteArrayOutputStream out = commons.doMergeMultiPdf(listFileNamePdfFinal);
			fileInfo.setContentFile(out.toByteArray());
			return fileInfo;
		}
		
	
	}

	
	

	@Override
	public FileInfo einvoiceXml(JSONRoot jsonRoot) throws Exception {
		FileInfo fileInfo = new FileInfo();
		ArrayList<HashMap<String, String>> arrayInfoInvoice = new ArrayList<>();
		HashMap<String, String> hItem = null;
		File file123 = null;
		String fileName = "";
		List<String> ids = null;
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		ByteArrayOutputStream baosPDF = null;
		List<String> listFileNamePdfFinal = new ArrayList<>();
		Object objData = msg.getObjData();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} 
		String _id = "";
		String _token = commons.getTextJsonNode(jsonData.at("/_token")).replaceAll("\\s", "");
		ids = null;
		try {
			ids = Json.serializer().fromJson(commons.decodeBase64ToString(_token), new TypeReference<List<String>>() {
			});
		}catch(Exception e) {}
		
		
		
		for(int i = 0; i< ids.size(); i++ ) {
			_id = ids.get(i) ;
		String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
		ObjectId objectId = null;
		try {
			objectId = new ObjectId(_id);
		} catch (Exception e) {
		}

		Document docFind = new Document("IssuerId", header.getIssuerId()).append("_id", objectId).append("IsDelete",
				new Document("$ne", true));
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docFind));
		pipeline.add(new Document("$lookup", new Document("from", "DMMauSoKyHieu")
				.append("let",
						new Document("vIssuerId", "$IssuerId").append("vMauSoHD", "$EInvoiceDetail.TTChung.MauSoHD"))
				.append("pipeline",
						Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
								Arrays.asList(new Document("$eq", Arrays.asList("$$vIssuerId", "$IssuerId")),
										new Document("$eq",
												Arrays.asList(new Document("$toString", "$_id"), "$$vMauSoHD"))))))))
				.append("as", "DMMauSoKyHieu")));
		pipeline.add(new Document("$unwind",
				new Document("path", "$DMMauSoKyHieu").append("preserveNullAndEmptyArrays", true)));

		Document docTmp = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("EInvoice").aggregate(pipeline).allowDiskUse(true);
		Iterator<Document> iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (null == docTmp || docTmp.get("DMMauSoKyHieu") == null) {
			return new FileInfo();
		}
String mskh = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHMSHDon"), "") + docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHHDon"), "");
Integer shd = 	docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","SHDon"), 0);
String namexml = mskh + "_" +shd  + ".xml";
boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
		boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

		String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
		String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
		String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");

		String dir = docTmp.get("Dir", "");
		String signStatusCode = docTmp.get("SignStatusCode", "");
		String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
		String MCCQT = docTmp.get("MCCQT", "");
		 fileName = _id + ".xml";
		if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
			fileName = _id + "_" + MCCQT + ".xml";
		} else {
			if ("SIGNED".equals(signStatusCode)) {
				fileName = _id + "_signed.xml";
			}
		}
		//namexml
		
		File file = new File(dir,fileName);
		File tam = new File(dir,namexml);
		 copyFileUsingStream(file, tam);

		hItem = new HashMap<>();
		hItem.put("UrlFile", tam.getAbsolutePath());
		arrayInfoInvoice.add(hItem);
		}
		if(arrayInfoInvoice.size() == 0) {
			return fileInfo;
		}
		if(arrayInfoInvoice.size() == 1) {
			hItem = arrayInfoInvoice.get(0);
			file123 = new File(hItem.get("UrlFile"));
			fileInfo.setContentFile(Files.readAllBytes(file123.toPath()));
			fileInfo.setFileName(fileName);
			return fileInfo;
		}else {
			/*NEN DANH SACH FILE XML*/
			FileInputStream fis = null;
			int length;
			byte[] buffer = new byte[1024];
			 bos = new ByteArrayOutputStream();
		    ZipOutputStream zout = new ZipOutputStream(bos);
		    for(int i=0;i<arrayInfoInvoice.size();i++){
		    	hItem = arrayInfoInvoice.get(i);
		    	file123 = new File(hItem.get("UrlFile"));
		    	fis = new FileInputStream(file123);
		    	zout.putNextEntry(new ZipEntry(file123.getName()));
				while((length = fis.read(buffer)) > 0)
	                   zout.write(buffer, 0, length);
	                
				zout.closeEntry();
				fis.close();
	            
			}
			zout.close();
			fileInfo.setFileName("EINVOICE.zip");
			fileInfo.setContentFile(bos.toByteArray());
		}
			return fileInfo;
	}
	
	private void copyFileUsingStream(File file, File tam)throws IOException {
		 InputStream is = null;
		    OutputStream os = null;
		    try {
		        is = new FileInputStream(file);
		        os = new FileOutputStream(tam);
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = is.read(buffer)) > 0) {
		            os.write(buffer, 0, length);
		        }
		    } finally {
		        is.close();
		        os.close();
		    }
	}

	/*
	 * db.getCollection('DMProduct').find({ IssuerId: '61b851ebb0228bba71fca2ec',
	 * IsDelete: {$ne: true}, $or: [ {Name: {$regex: '', $options: 'i'}}, {Code:
	 * {$regex: '', $options: 'i'}} ] }).sort({Name: 1, Code: 1, _id: 1}) .limit(20)
	 */

	@Override
	public MsgRsp getAutoCompleteProducts(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();

		JsonNode jsonData = null;
		String dataInput = "";
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
			dataInput = commons.getTextJsonNode(jsonData.at("/DataInput")).trim().replaceAll("\\s+", " ");
		}

		dataInput = commons.regexEscapeForMongoQuery(dataInput);
		Document docFind = new Document("IssuerId", header.getIssuerId()).append("IsDelete", new Document("$ne", true))
				.append("$or",
						Arrays.asList(new Document("Name", new Document("$regex", dataInput).append("$options", "i")),
								new Document("Code", new Document("$regex", dataInput).append("$options", "i"))));

//		 docFind = new Document("IsDelete", new Document("$ne", true))
//				.append("$or", 
//					Arrays.asList(
//						new Document("Name", new Document("$regex", dataInput).append("$options", "i")),
//						new Document("Code", new Document("$regex", dataInput).append("$options", "i"))
//					)
//				);

		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ArrayList<HashMap<String, Object>> datasReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;

		Document docTmp = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("DMProduct").find(docFind)
				.sort(new Document("Name", 1).append("Code", 1).append("_id", 1)).limit(20);
		Iterator<Document> iter = cursor.iterator();
		while (iter.hasNext()) {
			docTmp = iter.next();

			hItem = new HashMap<String, Object>();
			hItem.put("Code", docTmp.get("Code"));
			hItem.put("Name", docTmp.get("Name"));
			hItem.put("Price", docTmp.get("Price"));
			hItem.put("Unit", docTmp.get("Unit"));
			hItem.put("VatRate", docTmp.get("VatRate"));
			hItem.put("Stock", docTmp.get("Stock"));

			datasReturn.add(hItem);
		}
		responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);
		rsp.setObjData(datasReturn);
		return rsp;
	}

	@Override
	public MsgRsp listSearchCustomer(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();

		String taxCode = "";
		String companyName = "";
		String customerName = "";

		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(objData);
			taxCode = commons.getTextJsonNode(jsonData.at("/TaxCode")).trim().replaceAll("\\s+", " ");
			companyName = commons.getTextJsonNode(jsonData.at("/CompanyName")).trim().replaceAll("\\s+", " ");
			customerName = commons.getTextJsonNode(jsonData.at("/CustomerName")).trim().replaceAll("\\s+", " ");
		}

		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Iterable<Document> cursor = null;
		Iterator<Document> iter = null;
		List<Document> pipeline = new ArrayList<Document>();

		Document docMatch = new Document("IssuerId", header.getIssuerId()).append("IsDelete",
				new Document("$ne", true));
		if (!"".equals(taxCode))
			docMatch.append("TaxCode",
					new Document("$regex", commons.regexEscapeForMongoQuery(taxCode)).append("$options", "i"));
		if (!"".equals(companyName))
			docMatch.append("CompanyName",
					new Document("$regex", commons.regexEscapeForMongoQuery(companyName)).append("$options", "i"));
		if (!"".equals(customerName))
			docMatch.append("CustomerName",
					new Document("$regex", commons.regexEscapeForMongoQuery(customerName)).append("$options", "i"));

		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docMatch));
		pipeline.add(new Document("$sort", new Document("Stock", 1).append("Code", 1).append("_id", 1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("DMCustomer").aggregate(pipeline).allowDiskUse(true);
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
				hItem.put("TaxCode", doc.get("TaxCode"));
				hItem.put("CustomerCode", doc.get("CustomerCode"));
				hItem.put("CompanyName", doc.get("CompanyName"));
				hItem.put("CustomerName", doc.get("CustomerName"));
				hItem.put("Address", doc.get("Address"));
				hItem.put("Email", doc.get("Email"));
				hItem.put("EmailCC", doc.get("EmailCC"));
				hItem.put("Phone", doc.get("Phone"));
				hItem.put("AccountNumber", doc.get("AccountNumber"));
				hItem.put("AccountBankName", doc.get("AccountBankName"));

				hItem.put("Province", doc.get("Province"));
				hItem.put("CustomerGroup1", doc.get("CustomerGroup1"));
				hItem.put("CustomerGroup2", doc.get("CustomerGroup2"));
				hItem.put("CustomerGroup3", doc.get("CustomerGroup3"));

				hItem.put("InfoCreated", doc.get("InfoCreated"));
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

	/*
	 * db.getCollection('EInvoice').aggregate([ {$match: { 'IssuerId' :
	 * '61b851ebb0228bba71fca2ec', IsDelete: {$ne: true}, SignStatusCode: 'SIGNED',
	 * EInvoiceStatus: 'COMPLETE', MCCQT: {$exists: true, $ne: null},
	 * 'EInvoiceDetail.TTChung.MauSoHD': '61bf1c62b0228bba7101b8c0',
	 * 'EInvoiceDetail.TTChung.SHDon': 1, 'EInvoiceDetail.TTChung.NLap': {$gte:
	 * ISODate("2021-12-26T00:00:00.000Z"), $lt:
	 * ISODate("2021-12-30T00:00:00.000Z")}, 'EInvoiceDetail.NDHDon.NMua.MST':
	 * '11111111', $or: [ {'EInvoiceDetail.NDHDon.NMua.Ten': {$regex: '',
	 * '$options': 'i'}}, {'EInvoiceDetail.NDHDon.NMua.HVTNMHang': {$regex: '',
	 * '$options': 'i'}} ] } } , {$sort: {'EInvoiceDetail.TTChung.MauSoHD':-1,
	 * 'SHDon': -1, _id: -1}} , {$facet: { meta: [{$count: 'total'}], data: [{$skip:
	 * 0}, {$limit: 10}] } } , {$unwind: '$meta'} ,
	 * {$project:{'total':'$meta.total',data:1}} ]);
	 */
	@Override
	public MsgRsp listEInvoiceSigned(JSONRoot jsonRoot) throws Exception {
		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();
		Object objData = msg.getObjData();

		String mauSoHdon = "";
		String soHoaDon = "";
		String fromDate = "";
		String toDate = "";
		String nbanMst = "";
		String nbanTen = "";

		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(objData);

			mauSoHdon = commons.getTextJsonNode(jsonData.at("/MauSoHdon")).replaceAll("\\s", "");
			soHoaDon = commons.getTextJsonNode(jsonData.at("/SoHoaDon")).replaceAll("\\s", "");
			fromDate = commons.getTextJsonNode(jsonData.at("/FromDate")).replaceAll("\\s", "");
			toDate = commons.getTextJsonNode(jsonData.at("/ToDate")).replaceAll("\\s", "");
			nbanMst = commons.getTextJsonNode(jsonData.at("/NbanMst")).trim().replaceAll("\\s+", " ");
			nbanTen = commons.getTextJsonNode(jsonData.at("/NbanTen")).trim().replaceAll("\\s+", " ");
		}

		MsgRsp rsp = new MsgRsp(header);
		MspResponseStatus responseStatus = null;

		ObjectId objectId = null;
		Document docTmp = null;
		Document docTmp1 = null;
		Document docTmp2 = null;
		Document docTmp3 = null;
		Iterable<Document> cursor = null;
		Iterable<Document> cursor1 = null;
		Iterator<Document> iter = null;
		Iterator<Document> iter1 = null;
		Iterable<Document> cursor2 = null;
		Iterable<Document> cursor3 = null;
		Iterator<Document> iter2 = null;
		Iterator<Document> iter3 = null;
		List<Document> pipeline = new ArrayList<Document>();

		LocalDate dateFrom = null;
		LocalDate dateTo = null;
		Document docMatchDate = null;

		dateFrom = "".equals(fromDate) || !commons.checkLocalDate(fromDate, Constants.FORMAT_DATE.FORMAT_DATE_WEB)
				? null
				: commons.convertStringToLocalDate(fromDate, Constants.FORMAT_DATE.FORMAT_DATE_WEB);
		dateTo = "".equals(toDate) || !commons.checkLocalDate(toDate, Constants.FORMAT_DATE.FORMAT_DATE_WEB) ? null
				: commons.convertStringToLocalDate(toDate, Constants.FORMAT_DATE.FORMAT_DATE_WEB);
		if (null != dateTo)
			dateTo = dateTo.plus(1, ChronoUnit.DAYS);
		if (null != dateFrom || null != dateTo) {
			docMatchDate = new Document();
			if (null != dateFrom)
				docMatchDate.append("$gte", dateFrom);
			if (null != dateTo)
				docMatchDate.append("$lt", dateTo);
		}

		Document docMatch = new Document("IssuerId", header.getIssuerId()).append("IsDelete", new Document("$ne", true))
				.append("SignStatusCode", "SIGNED")
				.append("EInvoiceStatus", 
                      new Document("$in", Arrays.asList(Constants.INVOICE_STATUS.COMPLETE, Constants.INVOICE_STATUS.ADJUSTED))
                    )

				.append("MCCQT", new Document("$exists", true).append("$ne", null));
		if (!"".equals(mauSoHdon))
			docMatch.append("EInvoiceDetail.TTChung.MauSoHD", commons.regexEscapeForMongoQuery(mauSoHdon));
		if (!"".equals(soHoaDon))
			docMatch.append("EInvoiceDetail.TTChung.SHDon", commons.stringToInteger(soHoaDon));
		if (null != docMatchDate)
			docMatch.append("EInvoiceDetail.TTChung.NLap", docMatchDate);
		if (!"".equals(nbanMst))
			docMatch.append("EInvoiceDetail.NDHDon.NMua.MST",
					new Document("$regex", commons.regexEscapeForMongoQuery(nbanMst)).append("$options", "i"));
		if (!"".equals(nbanTen)) {
			docMatch.append("$or",
					Arrays.asList(
							new Document("EInvoiceDetail.NDHDon.NMua.Ten",
									new Document("$regex", commons.regexEscapeForMongoQuery(nbanTen)).append("$options",
											"i")),
							new Document("EInvoiceDetail.NDHDon.NMua.HVTNMHang",
									new Document("$regex", commons.regexEscapeForMongoQuery(nbanTen)).append("$options",
											"i"))));
		}
		pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docMatch));

		pipeline.add(new Document("$sort",
				new Document("EInvoiceDetail.TTChung.MauSoHD", -1).append("SHDon", -1).append("_id", -1)));
		pipeline.addAll(createFacetForSearchNotSort(page));

		cursor = mongoTemplate.getCollection("EInvoice").aggregate(pipeline).allowDiskUse(true);
		cursor1 = mongoTemplate.getCollection("EInvoicePXK").aggregate(pipeline).allowDiskUse(true);
		cursor2 = mongoTemplate.getCollection("EInvoiceBH").aggregate(pipeline).allowDiskUse(true);
		cursor3 = mongoTemplate.getCollection("EInvoicePXKDL").aggregate(pipeline).allowDiskUse(true);
		iter = cursor.iterator();
		iter1 = cursor1.iterator();
		iter2 = cursor2.iterator();
		iter3 = cursor3.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (iter1.hasNext()) {
			docTmp1 = iter1.next();

		}
		if (iter2.hasNext()) {
			docTmp2 = iter2.next();
		}
		if (iter3.hasNext()) {
			docTmp3 = iter3.next();

		}
		rsp = new MsgRsp(header);
		responseStatus = null;
		ArrayList<HashMap<String, Object>> rowsReturn = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hItem = null;
		if (null != docTmp) {
			page.setTotalRows(docTmp.getInteger("total", 0));
			rsp.setMsgPage(page);
			List<Document> rows = null;
			if (docTmp.get("data") != null && docTmp.get("data") instanceof List) {
				rows = docTmp.getList("data", Document.class);
			}
			if (null != rows) {
				for (Document doc : rows) {
					objectId = (ObjectId) doc.get("_id");

					hItem = new HashMap<String, Object>();
					hItem.put("_id", objectId.toString());
					hItem.put("EInvoiceStatus", doc.get("EInvoiceStatus"));
					hItem.put("SignStatusCode", doc.get("SignStatusCode"));
					hItem.put("MCCQT", doc.get("MCCQT"));

					hItem.put("EInvoiceDetail", doc.get("EInvoiceDetail"));
					hItem.put("InfoCreated", doc.get("InfoCreated"));
					hItem.put("LDo", doc.get("LDo"));

					rowsReturn.add(hItem);
				}
			}
		}
		if (null != docTmp1) {
			page.setTotalRows(docTmp1.getInteger("total", 0));
			rsp.setMsgPage(page);
			List<Document> rows1 = null;
			if (docTmp1.get("data") != null && docTmp1.get("data") instanceof List) {
				rows1 = docTmp1.getList("data", Document.class);
			}

			if (null != rows1) {
				for (Document doc : rows1) {
					objectId = (ObjectId) doc.get("_id");

					hItem = new HashMap<String, Object>();
					hItem.put("_id", objectId.toString());
					hItem.put("EInvoiceStatus", doc.get("EInvoiceStatus"));
					hItem.put("SignStatusCode", doc.get("SignStatusCode"));
					hItem.put("MCCQT", doc.get("MCCQT"));

					hItem.put("EInvoiceDetail", doc.get("EInvoiceDetail"));
					hItem.put("InfoCreated", doc.get("InfoCreated"));
					hItem.put("LDo", doc.get("LDo"));

					rowsReturn.add(hItem);
				}
			}
		}
		if (null != docTmp2) {
			page.setTotalRows(docTmp2.getInteger("total", 0));
			rsp.setMsgPage(page);
			List<Document> rows1 = null;
			if (docTmp2.get("data") != null && docTmp2.get("data") instanceof List) {
				rows1 = docTmp2.getList("data", Document.class);
			}

			if (null != rows1) {
				for (Document doc : rows1) {
					objectId = (ObjectId) doc.get("_id");

					hItem = new HashMap<String, Object>();
					hItem.put("_id", objectId.toString());
					hItem.put("EInvoiceStatus", doc.get("EInvoiceStatus"));
					hItem.put("SignStatusCode", doc.get("SignStatusCode"));
					hItem.put("MCCQT", doc.get("MCCQT"));

					hItem.put("EInvoiceDetail", doc.get("EInvoiceDetail"));
					hItem.put("InfoCreated", doc.get("InfoCreated"));
					hItem.put("LDo", doc.get("LDo"));

					rowsReturn.add(hItem);
				}
			}
		}
		if (null != docTmp3) {
			page.setTotalRows(docTmp3.getInteger("total", 0));
			rsp.setMsgPage(page);
			List<Document> rows1 = null;
			if (docTmp3.get("data") != null && docTmp3.get("data") instanceof List) {
				rows1 = docTmp3.getList("data", Document.class);
			}

			if (null != rows1) {
				for (Document doc : rows1) {
					objectId = (ObjectId) doc.get("_id");

					hItem = new HashMap<String, Object>();
					hItem.put("_id", objectId.toString());
					hItem.put("EInvoiceStatus", doc.get("EInvoiceStatus"));
					hItem.put("SignStatusCode", doc.get("SignStatusCode"));
					hItem.put("MCCQT", doc.get("MCCQT"));

					hItem.put("EInvoiceDetail", doc.get("EInvoiceDetail"));
					hItem.put("InfoCreated", doc.get("InfoCreated"));
					hItem.put("LDo", doc.get("LDo"));

					rowsReturn.add(hItem);
				}
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
	public FileInfo printExport(JSONRoot jsonRoot) throws Exception {
		FileInfo fileInfo = new FileInfo();

		Msg msg = jsonRoot.getMsg();
		MsgHeader header = msg.getMsgHeader();
		MsgPage page = msg.getMsgPage();

		Object objData = msg.getObjData();

		JsonNode jsonData = null;
		if (objData != null) {
			jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		} else {
			return new FileInfo();
		}
		ObjectId objectIdIssu = null;
	    try {
	      objectIdIssu = new ObjectId(header.getIssuerId());
	    } catch (Exception e) {
	    }
		String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
		String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
		ObjectId objectId = null;
		try {
			objectId = new ObjectId(_id);
		} catch (Exception e) {
		}

		Document docFind = new Document("IssuerId", header.getIssuerId()).append("_id", objectId).append("IsDelete",
				new Document("$ne", true));
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docFind));
		pipeline.add(new Document("$lookup", new Document("from", "DMMauSoKyHieu")
				.append("let",
						new Document("vIssuerId", "$IssuerId").append("vMauSoHD", "$EInvoiceDetail.TTChung.MauSoHD"))
				.append("pipeline",
						Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
								Arrays.asList(new Document("$eq", Arrays.asList("$$vIssuerId", "$IssuerId")),
										new Document("$eq",
												Arrays.asList(new Document("$toString", "$_id"), "$$vMauSoHD"))))))))
				.append("as", "DMMauSoKyHieu")));
		pipeline.add(new Document("$unwind",
				new Document("path", "$DMMauSoKyHieu").append("preserveNullAndEmptyArrays", true)));
		pipeline.add(
		        new Document("$lookup", 
		          new Document("from", "Issuer")
		          .append("pipeline", 
		            Arrays.asList(
		              new Document("$match", 
		                new Document("_id", objectIdIssu).append("IsDelete", new Document("$ne", true))
		              )
		            )
		          )
		          .append("as", "Issuer")
		        )
		      );
		      pipeline.add(new Document("$unwind", new Document("path", "$Issuer").append("preserveNullAndEmptyArrays", true)));
		  	pipeline.add(new Document("$lookup", new Document("from", "UserConFig")
					.append("pipeline", Arrays.asList(new Document("$match", new Document("viewshd", "Y")
							.append("IssuerId", header.getIssuerId()))))
					.append("as", "UserConFig")));
			pipeline.add(new Document("$unwind",
					new Document("path", "$UserConFig").append("preserveNullAndEmptyArrays", true)));

		Document docTmp = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("EInvoicePXK").aggregate(pipeline).allowDiskUse(true);
		Iterator<Document> iter = cursor.iterator();
		if (iter.hasNext()) {
			docTmp = iter.next();
		}
		if (null == docTmp || docTmp.get("DMMauSoKyHieu") == null) {
			return new FileInfo();
		}

		boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
		boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

		String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
		String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
		String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
		String ImgQA = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgQA"), "");
		String ImgVien = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgVien"), "");

		String dir = docTmp.get("Dir", "");
		String signStatusCode = docTmp.get("SignStatusCode", "");
		String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
		String MCCQT = docTmp.get("MCCQT", "");
		String fileName = _id + ".xml";
		if ("SIGNED".equals(signStatusCode) && "COMPLETE".equals(eInvoiceStatus) && !"".equals(MCCQT)) {
			fileName = _id + "_" + MCCQT + ".xml";
		} else {
			if ("SIGNED".equals(signStatusCode)) {
				fileName = _id + "_signed.xml";
			}
		}
		File file = new File(dir, fileName);
		if (!file.exists() || !file.isFile()) {
			return new FileInfo();
		}

//				file = new File("D:\\WORKING\\WEB\\HDDT-NEW\\_TTP\\_temp", "61d058d11b9b8d400da33da7_00276F25B531ED4541B4D1C69B06E60AE2.xml");

		org.w3c.dom.Document doc = commons.fileToDocument(file);
		/* TEST REPORT TO PDF */
		String fileNameJP = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "FileName"), "");
		int numberRowInPage = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowsInPage"), 20);
		int numberRowInPageMultiPage = docTmp
				.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowInPageMultiPage"), 26);
		int numberCharsInRow = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "CharsInRow"), 50);
		String CheckView = docTmp.getEmbedded(Arrays.asList("UserConFig", "viewshd"), "");
		File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

		ByteArrayOutputStream baosPDF = null;

		baosPDF = jpUtils.createFinalInvoice2(fileJP, doc,docTmp, CheckView,numberRowInPage, numberRowInPageMultiPage, numberCharsInRow,
		        MST, Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgLogo).toString(),
		        Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgBackground).toString(),
		        Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgQA ).toString(),
				Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgVien ).toString(),
		        "Y".equals(isConvert), Constants.INVOICE_STATUS.DELETED.equals(eInvoiceStatus), isThayThe, isDieuChinh);
		    fileInfo.setFileName("Export.pdf");
		    fileInfo.setContentFile(baosPDF.toByteArray());

		return fileInfo;
	}


		@Override
		public FileInfo viewpdf(JSONRoot jsonRoot) throws Exception {
			FileInfo fileInfo = new FileInfo();

			Msg msg = jsonRoot.getMsg();
			MsgHeader header = msg.getMsgHeader();
			MsgPage page = msg.getMsgPage();

			Object objData = msg.getObjData();

			JsonNode jsonData = null;
			if (objData != null) {
				jsonData = Json.serializer().nodeFromObject(msg.getObjData());
			} else {
				return new FileInfo();
			}

			String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
			String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
			ObjectId objectId = null;
			try {
				objectId = new ObjectId(_id);
			} catch (Exception e) {
			}
			ObjectId objectIdIssu = null;
			try {
				objectIdIssu = new ObjectId(header.getIssuerId());
			} catch (Exception e) {
			}

			List<Document> pipeline = new ArrayList<Document>();
			pipeline.add(
					new Document("$match", 
						new Document("_id", objectId).append("IsDelete", new Document("$ne", true))
					)
				);
			pipeline.add(
					new Document("$lookup", 
						new Document("from", "Issuer")
						.append("pipeline", 
							Arrays.asList(
								new Document("$match", 
									new Document("_id", objectIdIssu ).append("IsDelete", new Document("$ne", true))
								)
							)
						)
						.append("as", "Issuer")
					)
				);
				pipeline.add(new Document("$unwind", new Document("path", "$Issuer").append("preserveNullAndEmptyArrays", true)));
				
		
			Document docTmp = null;
			Iterable<Document> cursor = mongoTemplate.getCollection("DMMauSoKyHieu").aggregate(pipeline).allowDiskUse(true);
			Iterator<Document> iter = cursor.iterator();
			if (iter.hasNext()) {
				docTmp = iter.next();
			}
			
			String ImgLogo = docTmp.getEmbedded(Arrays.asList("Templates", "ImgLogo"), "");
			String ImgBackground = docTmp.getEmbedded(Arrays.asList( "Templates", "ImgBackground"), "");
			String ImgQA = docTmp.getEmbedded(Arrays.asList( "Templates", "ImgQA"), "");
			String ImgVien = docTmp.getEmbedded(Arrays.asList( "Templates", "ImgVien"), "");

	
			//org.w3c.dom.Document doc = (org.w3c.dom.Document) isu;
			/* TEST REPORT TO PDF */
			String fileNameJP = docTmp.getEmbedded(Arrays.asList( "Templates", "FileName"), "");
			int numberRowInPage = docTmp.getEmbedded(Arrays.asList( "Templates", "RowsInPage"), 20);
			int numberRowInPageMultiPage = docTmp
					.getEmbedded(Arrays.asList( "Templates", "RowInPageMultiPage"), 26);
			int numberCharsInRow = docTmp.getEmbedded(Arrays.asList( "Templates", "CharsInRow"), 50);

			File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

			ByteArrayOutputStream baosPDF = null;

			baosPDF = jpUtils.viewpdf(fileJP, docTmp, numberRowInPage, numberRowInPageMultiPage, numberCharsInRow,
					 Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", docTmp.getEmbedded(Arrays.asList( "Issuer", "TaxCode"), ""), ImgLogo).toString(),
					Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images",docTmp.getEmbedded(Arrays.asList( "Issuer", "TaxCode"), ""),  ImgBackground).toString(),
					Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images",docTmp.getEmbedded(Arrays.asList( "Issuer", "TaxCode"), ""), ImgQA).toString(),
					Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images",docTmp.getEmbedded(Arrays.asList( "Issuer", "TaxCode"), ""), ImgVien).toString(),

					"Y".equals(isConvert));
			fileInfo.setFileName("Viewpdf.pdf");
			fileInfo.setContentFile(baosPDF.toByteArray());

			return fileInfo;
		}

		
		

		@Override
		public FileInfo viewpdftncn(JSONRoot jsonRoot) throws Exception {
			FileInfo fileInfo = new FileInfo();

			Msg msg = jsonRoot.getMsg();
			MsgHeader header = msg.getMsgHeader();
			MsgPage page = msg.getMsgPage();

			Object objData = msg.getObjData();

			JsonNode jsonData = null;
			if (objData != null) {
				jsonData = Json.serializer().nodeFromObject(msg.getObjData());
			} else {
				return new FileInfo();
			}

			String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
			String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
			ObjectId objectId = null;
			try {
				objectId = new ObjectId(_id);
			} catch (Exception e) {
			}
			ObjectId objectIdIssu = null;
			try {
				objectIdIssu = new ObjectId(header.getIssuerId());
			} catch (Exception e) {
			}

			List<Document> pipeline = new ArrayList<Document>();
			pipeline.add(
					new Document("$match", 
						new Document("_id", objectId).append("IsDelete", new Document("$ne", true))
					)
				);
			pipeline.add(
					new Document("$lookup", 
						new Document("from", "Issuer")
						.append("pipeline", 
							Arrays.asList(
								new Document("$match", 
									new Document("_id", objectIdIssu ).append("IsDelete", new Document("$ne", true))
								)
							)
						)
						.append("as", "Issuer")
					)
				);
				pipeline.add(new Document("$unwind", new Document("path", "$Issuer").append("preserveNullAndEmptyArrays", true)));
				
		
			Document docTmp = null;
			Iterable<Document> cursor = mongoTemplate.getCollection("DMMSTNCN").aggregate(pipeline).allowDiskUse(true);
			Iterator<Document> iter = cursor.iterator();
			if (iter.hasNext()) {
				docTmp = iter.next();
			}
			
			String ImgLogo = docTmp.getEmbedded(Arrays.asList("LoGo"), "");
			
			String KYHieu = docTmp.getEmbedded(Arrays.asList("KyHieu"), "");
			String MauSo = docTmp.getEmbedded(Arrays.asList("MauSo"), "");
			String Nam = docTmp.getEmbedded(Arrays.asList("Nam"), "");
			String ChungTu = docTmp.getEmbedded(Arrays.asList("ChungTu"), "");
			
			String KH = KYHieu + MauSo + Nam + ChungTu;
			//org.w3c.dom.Document doc = (org.w3c.dom.Document) isu;
			/* TEST REPORT TO PDF */
		String fileNameJP = docTmp.getEmbedded(Arrays.asList("FileName"), "");
			
			File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

			ByteArrayOutputStream baosPDF = null;

			baosPDF = jpUtils.viewpdftncn(fileJP, docTmp,   
					 Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "MauSoTNCN",  docTmp.getEmbedded(Arrays.asList( "Issuer", "TaxCode"), ""), ImgLogo).toString(),
					 KH,	"Y".equals(isConvert));
			fileInfo.setFileName("viewpdftncn.pdf");
			fileInfo.setContentFile(baosPDF.toByteArray());

			return fileInfo;
		}

		
		
		
		
		
		
		
		
		
		
		
		 public FileInfo print04(JSONRoot jsonRoot) throws Exception {
		      FileInfo fileInfo = new FileInfo();
		      Msg msg = jsonRoot.getMsg();
		      MsgHeader header = msg.getMsgHeader();
		      MsgPage page = msg.getMsgPage();
		      Object objData = msg.getObjData();
		      JsonNode jsonData = null;
		      if (objData != null) {
		         jsonData = Json.serializer().nodeFromObject(msg.getObjData());
		         String _id = this.commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
		         String isConvert = this.commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
		         ObjectId objectId = null;

		         try {
		            objectId = new ObjectId(_id);
		         } catch (Exception var29) {
		         }

		         ObjectId objectIdIssu = null;

		         try {
		            objectIdIssu = new ObjectId(header.getIssuerId());
		         } catch (Exception var28) {
		         }

		         List<Document> pipeline = new ArrayList();
		         pipeline.add(new Document("$match", (new Document("_id", objectId)).append("IsDelete", new Document("$ne", true))));
		         pipeline.add(new Document("$lookup", (new Document("from", "Issuer")).append("pipeline", Arrays.asList(new Document("$match", (new Document("_id", objectIdIssu)).append("IsDelete", new Document("$ne", true))))).append("as", "Issuer")));
		         pipeline.add(new Document("$unwind", (new Document("path", "$Issuer")).append("preserveNullAndEmptyArrays", true)));
		         Document docTmp = null;
		         Iterable<Document> cursor = this.mongoTemplate.getCollection("EInvoiceHDSS").aggregate(pipeline).allowDiskUse(true);
		         Iterator<Document> iter = cursor.iterator();
		         if (iter.hasNext()) {
		            docTmp = (Document)iter.next();
		         }

		         String ImgLogo = "";
		         String ImgBackground = "";
		         String fileName = _id +"_signed.xml";
		         String dir = (String)docTmp.get("Dir", "");
		         File file = new File(dir, fileName);
		         if (file.exists() && file.isFile()) {
		            org.w3c.dom.Document doc = this.commons.fileToDocument(file);
		            String fileNameJP = "04SS.jrxml";
		            int numberRowInPage = 5;
		            int numberRowInPageMultiPage = 15;
		            int numberCharsInRow = 50;
		            File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);
		            ByteArrayOutputStream baosPDF = null;
		            baosPDF = this.jpUtils.print04(fileJP, doc, docTmp, numberRowInPage, numberRowInPageMultiPage, numberCharsInRow, Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", (String)docTmp.getEmbedded(Arrays.asList("Issuer", "TaxCode"), ""), ImgLogo).toString(), Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", (String)docTmp.getEmbedded(Arrays.asList("Issuer", "TaxCode"), ""), ImgBackground).toString(), "Y".equals(isConvert));
		            fileInfo.setFileName("print04.pdf");
		            fileInfo.setContentFile(baosPDF.toByteArray());
		            return fileInfo;
		         } else {
		            return new FileInfo();
		         }
		      } else {
		         return new FileInfo();
		      }
		   }

		
		
		
		
		
		
		//Print Agent
		@Override
		public FileInfo printAgent(JSONRoot jsonRoot) throws Exception {
			FileInfo fileInfo = new FileInfo();

			Msg msg = jsonRoot.getMsg();
			MsgHeader header = msg.getMsgHeader();
			MsgPage page = msg.getMsgPage();

			Object objData = msg.getObjData();

			JsonNode jsonData = null;
			if (objData != null) {
				jsonData = Json.serializer().nodeFromObject(msg.getObjData());
			} else {
				return new FileInfo();
			}

			String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
			String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
			ObjectId objectId = null;
			try {
				objectId = new ObjectId(_id);
			} catch (Exception e) {
			}

			Document docFind = new Document("IssuerId", header.getIssuerId()).append("_id", objectId).append("IsDelete",
					new Document("$ne", true));
			List<Document> pipeline = new ArrayList<Document>();
			pipeline.add(new Document("$match", docFind));
			pipeline.add(new Document("$lookup", new Document("from", "DMMauSoKyHieu")
					.append("let",
							new Document("vIssuerId", "$IssuerId").append("vMauSoHD", "$EInvoiceDetail.TTChung.MauSoHD"))
					.append("pipeline",
							Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
									Arrays.asList(new Document("$eq", Arrays.asList("$$vIssuerId", "$IssuerId")),
											new Document("$eq",
													Arrays.asList(new Document("$toString", "$_id"), "$$vMauSoHD"))))))))
					.append("as", "DMMauSoKyHieu")));
			pipeline.add(new Document("$unwind",
					new Document("path", "$DMMauSoKyHieu").append("preserveNullAndEmptyArrays", true)));
			pipeline.add(new Document("$lookup", new Document("from", "UserConFig")
					.append("pipeline", Arrays.asList(new Document("$match", new Document("viewshd", "Y")
							.append("IssuerId", header.getIssuerId()))))
					.append("as", "UserConFig")));
			pipeline.add(new Document("$unwind",
					new Document("path", "$UserConFig").append("preserveNullAndEmptyArrays", true)));
			Document docTmp = null;
			Iterable<Document> cursor = mongoTemplate.getCollection("EInvoicePXKDL").aggregate(pipeline).allowDiskUse(true);
			Iterator<Document> iter = cursor.iterator();
			if (iter.hasNext()) {
				docTmp = iter.next();
			}
			if (null == docTmp || docTmp.get("DMMauSoKyHieu") == null) {
				return new FileInfo();
			}

			boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
			boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

			String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
			String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
			String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
			String ImgQA = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgQA"), "");
			String ImgVien = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgVien"), "");
			String CheckView = docTmp.getEmbedded(Arrays.asList("UserConFig", "viewshd"), "");
			String dir = docTmp.get("Dir", "");
			String signStatusCode = docTmp.get("SignStatusCode", "");
			String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
			String MCCQT = docTmp.get("MCCQT", "");
			String fileName = _id + ".xml";
			if ("SIGNED".equals(signStatusCode) && "COMPLETE".equals(eInvoiceStatus) && !"".equals(MCCQT)) {
				fileName = _id + "_" + MCCQT + ".xml";
			} else {
				if ("SIGNED".equals(signStatusCode)) {
					fileName = _id + "_signed.xml";
				}
			}
			File file = new File(dir, fileName);
			if (!file.exists() || !file.isFile()) {
				return new FileInfo();
			}

//					file = new File("D:\\WORKING\\WEB\\HDDT-NEW\\_TTP\\_temp", "61d058d11b9b8d400da33da7_00276F25B531ED4541B4D1C69B06E60AE2.xml");

			org.w3c.dom.Document doc = commons.fileToDocument(file);
			/* TEST REPORT TO PDF */
			String fileNameJP = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "FileName"), "");
			int numberRowInPage = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowsInPage"), 20);
			int numberRowInPageMultiPage = docTmp
					.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowInPageMultiPage"), 26);
			int numberCharsInRow = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "CharsInRow"), 50);

			File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

			ByteArrayOutputStream baosPDF = null;

			baosPDF = jpUtils.createFinalInvoiceDL(fileJP, doc,CheckView, numberRowInPage, numberRowInPageMultiPage, numberCharsInRow,
					MST, Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgLogo).toString(),
					Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgBackground).toString(),
					Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgQA ).toString(),
					Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgVien ).toString(),

					"Y".equals(isConvert), Constants.INVOICE_STATUS.DELETED.equals(eInvoiceStatus), isThayThe, isDieuChinh);
			fileInfo.setFileName("Agent.pdf");
			fileInfo.setContentFile(baosPDF.toByteArray());

			return fileInfo;
		}
		
			//Print EInvoice BH  
			@Override
			public FileInfo printEInvoiceBH(JSONRoot jsonRoot) throws Exception {
				FileInfo fileInfo = new FileInfo();

				Msg msg = jsonRoot.getMsg();
				MsgHeader header = msg.getMsgHeader();
				MsgPage page = msg.getMsgPage();

				Object objData = msg.getObjData();

				JsonNode jsonData = null;
				if (objData != null) {
					jsonData = Json.serializer().nodeFromObject(msg.getObjData());
				} else {
					return new FileInfo();
				}

				String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
				String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
				/*
				 * db.getCollection('EInvoice').find( { IssuerId: '61b851ebb0228bba71fca2ec',
				 * _id: ObjectId("61c7cad6f8b593616ce03cc7"),IsDelete: {$ne: true} } )
				 * 
				 * db.getCollection('EInvoice').aggregate([ {$match: { IssuerId:
				 * '61b851ebb0228bba71fca2ec', _id:
				 * ObjectId("61c7cad6f8b593616ce03cc7"),IsDelete: {$ne: true} } }, {$lookup: {
				 * from: 'DMMauSoKyHieu', let: {vIssuerId: '$IssuerId', vMauSoHD:
				 * '$EInvoiceDetail.TTChung.MauSoHD'}, pipeline: [ {$match: { $expr: { $and: [
				 * {$eq: ['$$vIssuerId', '$IssuerId']}, {$eq: [{$toString: '$_id'},
				 * '$$vMauSoHD']} ] } } } ], as: 'DMMauSoKyHieu' } }, {$unwind: {path:
				 * '$DMMauSoKyHieu', preserveNullAndEmptyArrays: true}} ])
				 * 
				 */

				ObjectId objectId = null;
				try {
					objectId = new ObjectId(_id);
				} catch (Exception e) {
				}

				Document docFind = new Document("IssuerId", header.getIssuerId()).append("_id", objectId).append("IsDelete",
						new Document("$ne", true));
				List<Document> pipeline = new ArrayList<Document>();
				pipeline.add(new Document("$match", docFind));
				pipeline.add(new Document("$lookup", new Document("from", "DMMauSoKyHieu")
						.append("let",
								new Document("vIssuerId", "$IssuerId").append("vMauSoHD", "$EInvoiceDetail.TTChung.MauSoHD"))
						.append("pipeline",
								Arrays.asList(new Document("$match", new Document("$expr", new Document("$and",
										Arrays.asList(new Document("$eq", Arrays.asList("$$vIssuerId", "$IssuerId")),
												new Document("$eq",
														Arrays.asList(new Document("$toString", "$_id"), "$$vMauSoHD"))))))))
						.append("as", "DMMauSoKyHieu")));
				pipeline.add(new Document("$unwind",
						new Document("path", "$DMMauSoKyHieu").append("preserveNullAndEmptyArrays", true)));
				pipeline.add(new Document("$lookup", new Document("from", "UserConFig")
						.append("pipeline", Arrays.asList(new Document("$match", new Document("viewshd", "Y")
								.append("IssuerId", header.getIssuerId()))))
						.append("as", "UserConFig")));
				pipeline.add(new Document("$unwind",
						new Document("path", "$UserConFig").append("preserveNullAndEmptyArrays", true)));
				Document docTmp = null;
				Iterable<Document> cursor = mongoTemplate.getCollection("EInvoiceBH").aggregate(pipeline).allowDiskUse(true);
				Iterator<Document> iter = cursor.iterator();
				if (iter.hasNext()) {
					docTmp = iter.next();
				}
				if (null == docTmp || docTmp.get("DMMauSoKyHieu") == null) {
					return new FileInfo();
				}

				boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
				boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

				String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
				String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
				String ImgQA = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgQA"), "");
				String ImgVien = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgVien"), "");

				String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
				String CheckView = docTmp.getEmbedded(Arrays.asList("UserConFig", "viewshd"), "");
				String dir = docTmp.get("Dir", "");
				String signStatusCode = docTmp.get("SignStatusCode", "");
				String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
				String MCCQT = docTmp.get("MCCQT", "");
				String fileName = _id + ".xml";
				if ("SIGNED".equals(signStatusCode) && "COMPLETE".equals(eInvoiceStatus) && !"".equals(MCCQT)) {
					fileName = _id + "_" + MCCQT + ".xml";
				} else {
					if ("SIGNED".equals(signStatusCode)) {
						fileName = _id + "_signed.xml";
					}
				}
//				else if("PENDING".equals(eInvoiceStatus))
//					fileName = _id + "_signed.xml";

				File file = new File(dir, fileName);
				if (!file.exists() || !file.isFile()) {
					return new FileInfo();
				}

//				file = new File("D:\\WORKING\\WEB\\HDDT-NEW\\_TTP\\_temp", "61d058d11b9b8d400da33da7_00276F25B531ED4541B4D1C69B06E60AE2.xml");

				org.w3c.dom.Document doc = commons.fileToDocument(file);
				/* TEST REPORT TO PDF */
				String fileNameJP = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "FileName"), "");
				int numberRowInPage = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowsInPage"), 20);
				int numberRowInPageMultiPage = docTmp
						.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "RowInPageMultiPage"), 26);
				int numberCharsInRow = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "CharsInRow"), 50);

				File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

				ByteArrayOutputStream baosPDF = null;

				baosPDF = jpUtils.createFinalInvoice(fileJP, doc,CheckView, numberRowInPage, numberRowInPageMultiPage, numberCharsInRow,
						MST, Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgLogo).toString(),
						Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgBackground).toString(),
						Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgQA ).toString(),
						Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "images", MST, ImgVien ).toString(),

						"Y".equals(isConvert), Constants.INVOICE_STATUS.DELETED.equals(eInvoiceStatus), isThayThe, isDieuChinh);

//				Map<String, Object> reportParams = new HashMap<String, Object>();
//				
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				JasperReport jr = JasperCompileManager.compileReport(new FileInputStream(fileJP));
//				JasperPrint jp = JasperFillManager.fillReport(jr, reportParams);
//				
//				Exporter exporter = null;
//				
//				exporter = new JRPdfExporter();
//				exporter.setExporterInput(new SimpleExporterInput(jp));
//				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
//		        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
//		        configuration.setCreatingBatchModeBookmarks(true);
//		        exporter.setConfiguration(configuration);
//		        exporter.exportReport();
//				
				fileInfo.setFileName("EInvoiceBH.pdf");
				fileInfo.setContentFile(baosPDF.toByteArray());

				return fileInfo;
			}

			
			@Override
			public FileInfo viewpdfcttncn(JSONRoot jsonRoot) throws Exception {
				FileInfo fileInfo = new FileInfo();

				Msg msg = jsonRoot.getMsg();
				MsgHeader header = msg.getMsgHeader();
				MsgPage page = msg.getMsgPage();

				Object objData = msg.getObjData();

				JsonNode jsonData = null;
				if (objData != null) {
					jsonData = Json.serializer().nodeFromObject(msg.getObjData());
				} else {
					return new FileInfo();
				}

				String _id = commons.getTextJsonNode(jsonData.at("/_id")).replaceAll("\\s", "");
				String isConvert = commons.getTextJsonNode(jsonData.at("/IsConvert")).replaceAll("\\s", "");
				ObjectId objectId = null;
				try {
					objectId = new ObjectId(_id);
				} catch (Exception e) {
				}
				ObjectId objectIdIssu = null;
				try {
					objectIdIssu = new ObjectId(header.getIssuerId());
				} catch (Exception e) {
				}

				List<Document> pipeline = new ArrayList<Document>();
				pipeline.add(
						new Document("$match", 
							new Document("_id", objectId).append("IsDelete", new Document("$ne", true))
						)
					);
				pipeline.add(
						new Document("$lookup", 
							new Document("from", "Issuer")
							.append("pipeline", 
								Arrays.asList(
									new Document("$match", 
										new Document("_id", objectIdIssu ).append("IsDelete", new Document("$ne", true))
									)
								)
							)
							.append("as", "Issuer")
						)
					);
					pipeline.add(new Document("$unwind", new Document("path", "$Issuer").append("preserveNullAndEmptyArrays", true)));
				
					pipeline.add(
							new Document("$lookup", 
								new Document("from", "DMMSTNCN")
								.append("pipeline", 
									Arrays.asList(
										new Document("$match", 
											new Document("IssuerId", objectIdIssu.toString() ).append("IsActive", true)
										)
									)
								)
								.append("as", "DMMSTNCN")
							)
						);
						pipeline.add(new Document("$unwind", new Document("path", "$DMMSTNCN").append("preserveNullAndEmptyArrays", true)));
			
				Document docTmp = null;
				Iterable<Document> cursor = mongoTemplate.getCollection("ChungTuTNCN").aggregate(pipeline).allowDiskUse(true);
				Iterator<Document> iter = cursor.iterator();
				if (iter.hasNext()) {
					docTmp = iter.next();
				}
				
				String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMSTNCN","LoGo"), "");
				
				
				String MauSo = docTmp.getEmbedded(Arrays.asList("DMMSTNCN","MauSo"), "");
			
				
				String KH = docTmp.getEmbedded(Arrays.asList("DMMSTNCN","KyHieu"), "")+"/"+docTmp.getEmbedded(Arrays.asList("DMMSTNCN","Nam"), "")+"/"+docTmp.getEmbedded(Arrays.asList("DMMSTNCN","ChungTu"), "");
				String MS = MauSo;
				//org.w3c.dom.Document doc = (org.w3c.dom.Document) isu;
				/* TEST REPORT TO PDF */
				
				
				String dir = docTmp.get("Dir", "");
				String SignStatus = docTmp.get("SignStatus", "");
				String fileName = _id + ".xml";
				if ("SIGNED".equals(SignStatus)) {
					fileName = _id + "_signed.xml";
				} 
				

				File file = new File(dir, fileName);
				if (!file.exists() || !file.isFile()) {
					return new FileInfo();
				}

				org.w3c.dom.Document doc = commons.fileToDocument(file);
				
				
				
				String fileNameJP = docTmp.getEmbedded(Arrays.asList("DMMSTNCN","FileName"), "");
			
				File fileJP = new File(SystemParams.DIR_E_INVOICE_TEMPLATE, fileNameJP);

				ByteArrayOutputStream baosPDF = null;

				baosPDF = jpUtils.viewpdfcttncn(fileJP, doc,docTmp,   
						 Paths.get(SystemParams.DIR_E_INVOICE_TEMPLATE, "MauSoTNCN",  docTmp.getEmbedded(Arrays.asList( "Issuer", "TaxCode"), ""), ImgLogo).toString(),
						 KH,MS,	"Y".equals(isConvert));
				fileInfo.setFileName("viewpdftncn.pdf");
				fileInfo.setContentFile(baosPDF.toByteArray());

				return fileInfo;
			}
			
			
			public FileInfo getXml(JSONRoot jsonRoot) throws Exception {
				FileInfo fileInfo = new FileInfo();
				ArrayList<HashMap<String, String>> arrayInfoInvoice = new ArrayList<>();
				HashMap<String, String> hItem = null;
				File file123 = null;
				String fileName = "";
				List<String> data = null;
				Msg msg = jsonRoot.getMsg();
				MsgHeader header = msg.getMsgHeader();
				MsgPage page = msg.getMsgPage();
				ByteArrayOutputStream baosPDF = null;
				List<String> listFileNamePdfFinal = new ArrayList<>();
				Object objData = msg.getObjData();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				JsonNode jsonData = null;
				if (objData != null) {
					jsonData = Json.serializer().nodeFromObject(msg.getObjData());
				} 
				String mst = "";
				String khhd = "";
				String shoad = "";
				String _token = commons.getTextJsonNode(jsonData.at("/_token")).replaceAll("\\s", "");
				data = null;
				try {
							data = Json.serializer().fromJson(commons.decodeBase64ToString(_token), new TypeReference<List<String>>() {
							});
						}catch(Exception e) {}
				
				mst = data.get(0);
				khhd = data.get(1);
				shoad = data.get(2);
				int shdon = Integer.parseInt(shoad);
				Document docFind = new Document("EInvoiceDetail.TTChung.KHHDon", khhd).append("EInvoiceDetail.TTChung.SHDon", shdon).append("EInvoiceDetail.NDHDon.NBan.MST", mst).append("IsDelete",
						new Document("$ne", true));
				List<Document> pipeline = new ArrayList<Document>();
				pipeline.add(new Document("$match", docFind));
				
				Document docTmp = null;
				Document docTmp1 = null;
				Document docTmp2 = null;
				Document docTmp3 = null;
				Iterable<Document> cursor = mongoTemplate.getCollection("EInvoice").aggregate(pipeline).allowDiskUse(true);
				Iterable<Document> cursor1 = mongoTemplate.getCollection("EInvoiceBH").aggregate(pipeline).allowDiskUse(true);
				Iterable<Document> cursor2 = mongoTemplate.getCollection("EInvoicePXK").aggregate(pipeline).allowDiskUse(true);
				Iterable<Document> cursor3 = mongoTemplate.getCollection("EInvoicePXKDL").aggregate(pipeline).allowDiskUse(true);
				Iterator<Document> iter = cursor.iterator();
				Iterator<Document> iter1 = cursor1.iterator();
				Iterator<Document> iter2 = cursor2.iterator();
				Iterator<Document> iter3 = cursor3.iterator();
				if (iter.hasNext()) {
					docTmp = iter.next();
				}
				if (iter1.hasNext()) {
					docTmp1 = iter1.next();
				}
				if (iter2.hasNext()) {
					docTmp2 = iter2.next();
				}
				if (iter3.hasNext()) {
					docTmp3 = iter3.next();
				}
				if (null != docTmp) {
					String mskh = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHMSHDon"), "") + docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHHDon"), "");
					Integer shd = 	docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","SHDon"), 0);
					String namexml = mskh + "_" +shd  + ".xml";
					boolean isDieuChinh = "2".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
					boolean isThayThe = "3".equals(docTmp.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

					String MST = docTmp.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
					String ImgLogo = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
					String ImgBackground = docTmp.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
					
					String _id = docTmp.get("_id").toString();	
					String dir = docTmp.get("Dir", "");
					String signStatusCode = docTmp.get("SignStatusCode", "");
					String eInvoiceStatus = docTmp.get("EInvoiceStatus", "");
					String MCCQT = docTmp.get("MCCQT", "");
					 fileName = _id + ".xml";
					if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
						fileName = _id + "_" + MCCQT + ".xml";
					} else {
						if ("SIGNED".equals(signStatusCode)) {
							fileName = _id + "_signed.xml";
						}
					}		
					File file = new File(dir,fileName);
					File tam = new File(dir,namexml);
					 copyFileUsingStream(file, tam);

					hItem = new HashMap<>();
					hItem.put("UrlFile", tam.getAbsolutePath());
					arrayInfoInvoice.add(hItem);
					
					if(arrayInfoInvoice.size() == 0) {
						return fileInfo;
					}
					if(arrayInfoInvoice.size() == 1) {
						hItem = arrayInfoInvoice.get(0);
						file123 = new File(hItem.get("UrlFile"));
						fileInfo.setContentFile(Files.readAllBytes(file123.toPath()));
						fileInfo.setFileName(fileName);
						return fileInfo;
					}
				}else if (null != docTmp1) {
					String mskh = docTmp1.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHMSHDon"), "") + docTmp1.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHHDon"), "");
					Integer shd = 	docTmp1.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","SHDon"), 0);
					String namexml = mskh + "_" +shd  + ".xml";
					boolean isDieuChinh = "2".equals(docTmp1.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
					boolean isThayThe = "3".equals(docTmp1.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

					String MST = docTmp1.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
					String ImgLogo = docTmp1.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
					String ImgBackground = docTmp1.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
					
					String _id = docTmp1.get("_id").toString();		
					String dir = docTmp1.get("Dir", "");
					String signStatusCode = docTmp1.get("SignStatusCode", "");
					String eInvoiceStatus = docTmp1.get("EInvoiceStatus", "");
					String MCCQT = docTmp1.get("MCCQT", "");
					 fileName = _id + ".xml";
					if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
						fileName = _id + "_" + MCCQT + ".xml";
					} else {
						if ("SIGNED".equals(signStatusCode)) {
							fileName = _id + "_signed.xml";
						}
					}		
					File file = new File(dir,fileName);
					File tam = new File(dir,namexml);
					 copyFileUsingStream(file, tam);

					hItem = new HashMap<>();
					hItem.put("UrlFile", tam.getAbsolutePath());
					arrayInfoInvoice.add(hItem);
					
					if(arrayInfoInvoice.size() == 0) {
						return fileInfo;
					}
					if(arrayInfoInvoice.size() == 1) {
						hItem = arrayInfoInvoice.get(0);
						file123 = new File(hItem.get("UrlFile"));
						fileInfo.setContentFile(Files.readAllBytes(file123.toPath()));
						fileInfo.setFileName(fileName);
						return fileInfo;
					}
				}else if (null != docTmp2) {
					String mskh = docTmp2.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHMSHDon"), "") + docTmp2.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHHDon"), "");
					Integer shd = 	docTmp2.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","SHDon"), 0);
					String namexml = mskh + "_" +shd  + ".xml";
					boolean isDieuChinh = "2".equals(docTmp2.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
					boolean isThayThe = "3".equals(docTmp2.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

					String MST = docTmp2.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
					String ImgLogo = docTmp2.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
					String ImgBackground = docTmp2.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
					
					String _id = docTmp2.get("_id").toString();		
					String dir = docTmp2.get("Dir", "");
					String signStatusCode = docTmp2.get("SignStatusCode", "");
					String eInvoiceStatus = docTmp2.get("EInvoiceStatus", "");
					String MCCQT = docTmp2.get("MCCQT", "");
					 fileName = _id + ".xml";
					if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
						fileName = _id + "_" + MCCQT + ".xml";
					} else {
						if ("SIGNED".equals(signStatusCode)) {
							fileName = _id + "_signed.xml";
						}
					}		
					File file = new File(dir,fileName);
					File tam = new File(dir,namexml);
					 copyFileUsingStream(file, tam);

					hItem = new HashMap<>();
					hItem.put("UrlFile", tam.getAbsolutePath());
					arrayInfoInvoice.add(hItem);
					
					if(arrayInfoInvoice.size() == 0) {
						return fileInfo;
					}
					if(arrayInfoInvoice.size() == 1) {
						hItem = arrayInfoInvoice.get(0);
						file123 = new File(hItem.get("UrlFile"));
						fileInfo.setContentFile(Files.readAllBytes(file123.toPath()));
						fileInfo.setFileName(fileName);
						return fileInfo;
					}
				}else if (null != docTmp3) {
					String mskh = docTmp3.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHMSHDon"), "") + docTmp3.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","KHHDon"), "");
					Integer shd = 	docTmp3.getEmbedded(Arrays.asList("EInvoiceDetail", "TTChung","SHDon"), 0);
					String namexml = mskh + "_" +shd  + ".xml";
					boolean isDieuChinh = "2".equals(docTmp3.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));
					boolean isThayThe = "3".equals(docTmp3.getEmbedded(Arrays.asList("HDSS", "TCTBao"), ""));

					String MST = docTmp3.getEmbedded(Arrays.asList("EInvoiceDetail", "NDHDon", "NBan", "MST"), "");
					String ImgLogo = docTmp3.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgLogo"), "");
					String ImgBackground = docTmp3.getEmbedded(Arrays.asList("DMMauSoKyHieu", "Templates", "ImgBackground"), "");
					
					String _id = docTmp3.get("_id").toString();		
					String dir = docTmp3.get("Dir", "");
					String signStatusCode = docTmp3.get("SignStatusCode", "");
					String eInvoiceStatus = docTmp3.get("EInvoiceStatus", "");
					String MCCQT = docTmp3.get("MCCQT", "");
					 fileName = _id + ".xml";
					if ("SIGNED".equals(signStatusCode) && !"".equals(MCCQT)) {
						fileName = _id + "_" + MCCQT + ".xml";
					} else {
						if ("SIGNED".equals(signStatusCode)) {
							fileName = _id + "_signed.xml";
						}
					}		
					File file = new File(dir,fileName);
					File tam = new File(dir,namexml);
					 copyFileUsingStream(file, tam);

					hItem = new HashMap<>();
					hItem.put("UrlFile", tam.getAbsolutePath());
					arrayInfoInvoice.add(hItem);
					
					if(arrayInfoInvoice.size() == 0) {
						return fileInfo;
					}
					if(arrayInfoInvoice.size() == 1) {
						hItem = arrayInfoInvoice.get(0);
						file123 = new File(hItem.get("UrlFile"));
						fileInfo.setContentFile(Files.readAllBytes(file123.toPath()));
						fileInfo.setFileName(fileName);
						return fileInfo;
					}
				}else {
					return new FileInfo();
				}
				
					return fileInfo;
			}
				
}
