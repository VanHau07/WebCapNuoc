package vn.webcapnuoc.hddt.user.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;

import vn.webcapnuoc.hddt.dto.FileInfo;
import vn.webcapnuoc.hddt.user.dao.CommonDAO;
import vn.webcapnuoc.hddt.utility.Commons;
import vn.webcapnuoc.hddt.utility.SystemParams;

@RestController
@RequestMapping(value = "/commons")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class CommonsController {
	@Autowired private CommonDAO dao;
	private Commons commons = new Commons();
	
	@RequestMapping(
		value = "/get-full-params", method = RequestMethod.POST
		, consumes = {MediaType.APPLICATION_JSON_VALUE } // MediaType.TEXT_PLAIN_VALUE,
		, produces = { MediaType.APPLICATION_JSON_VALUE }
	)
	public ResponseEntity<?> getFullParams(@RequestBody JSONRoot jsonRoot) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok().headers(headers).cacheControl(CacheControl.noCache()).body(dao.getFullParams(jsonRoot));
	}
	
	@RequestMapping(value = "/print-einvoice", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> printEinvoice(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.printEInvoice(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "einvoice.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	@RequestMapping(value = "/print-einvoiceAll", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> printEinvoiceAll(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.printEinvoiceAll(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "einvoice.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
		
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	@RequestMapping(value = "/einvoiceXml", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> einvoiceXml(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.einvoiceXml(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "bill.data");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	//Print PXK
	@RequestMapping(value = "/print-export", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> printExport(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.printExport(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "export.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	
	@RequestMapping(
		value = "/auto-complete-products", method = RequestMethod.POST
		, consumes = {MediaType.APPLICATION_JSON_VALUE } // MediaType.TEXT_PLAIN_VALUE,
		, produces = { MediaType.APPLICATION_JSON_VALUE }
	)
	public ResponseEntity<?> getAutoCompleteProducts(@RequestBody JSONRoot jsonRoot) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok().headers(headers).cacheControl(CacheControl.noCache()).body(dao.getAutoCompleteProducts(jsonRoot));
	}
	
	@RequestMapping(value = "/processUploadTmpFile", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> processUploadTmpFile(HttpServletRequest req,
			MultipartHttpServletRequest multipartHttpServletRequest,
			@RequestParam(name = "IssuerId", defaultValue = "") String issuerId) throws Exception {
		MsgRsp rsp = new MsgRsp();

		/* TAO THU MUC NEU CHUA TON TAI */
		File file = new File(SystemParams.DIR_TEMPORARY, issuerId);
		if (!file.exists())
			file.mkdirs();

		ArrayList<HashMap<String, String>> files = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> hFile = null;

		String originalFilename = "";
		String fileNameStore = "";
		String fileNameExt = "";
		List<String> requestKeys = new ArrayList<String>();
		multipartHttpServletRequest.getFileNames().forEachRemaining(requestKeys::add);

		for (String multiPartFile : requestKeys) {
			originalFilename = multipartHttpServletRequest.getFile(multiPartFile).getOriginalFilename();
			fileNameExt = FilenameUtils.getExtension(originalFilename);
			fileNameExt = "".equals(fileNameExt) ? "" : "." + fileNameExt;
			fileNameStore = commons.convertLocalDateTimeToString(LocalDateTime.now(), "yyyyMMddHHmmss") + "-" + commons.csRandomAlphaNumbericString(5) + fileNameExt;

			FileCopyUtils.copy(multipartHttpServletRequest.getFile(multiPartFile).getBytes(),
					new FileOutputStream(new File(file.getAbsolutePath(), fileNameStore)));

			hFile = new HashMap<String, String>();
			hFile.put("OriginalFilename", originalFilename);
			hFile.put("SystemFilename", fileNameStore);
			files.add(hFile);
		}

		MspResponseStatus responseStatus = new MspResponseStatus(0, "SUCCESS");
		rsp.setResponseStatus(responseStatus);
		rsp.setObjData(files);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok().headers(headers).cacheControl(CacheControl.noCache()).body(rsp);
	}
	
	@RequestMapping(value = "/list-search-customer", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> listSearchCustomer(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.listSearchCustomer(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	
	@RequestMapping(value = "/list-einvoice-signed", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> listEInvoiceSigned(@RequestBody JSONRoot jsonRoot) throws Exception{
		MsgRsp rsp = dao.listEInvoiceSigned(jsonRoot);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(rsp);
	}
	@RequestMapping(value = "/viewpdf", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> viewpdf(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.viewpdf(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "viewpdf.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	
	@RequestMapping(value = "/viewpdftncn", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> viewpdftncn(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.viewpdftncn(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "viewpdf.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	@RequestMapping(value = "/viewpdfcttncn", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> viewpdfcttncn(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.viewpdfcttncn(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "viewpdf.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	@RequestMapping(value = "/print04", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> print04(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.print04(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "print04.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	//Print PXKDL
	@RequestMapping(value = "/print-agent", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> printAgent(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.printAgent(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "agent.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	//Print Hóa đơn bán hàng
	@RequestMapping(value = "/print-einvoice1", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> printEInvoiceBH(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.printEInvoiceBH(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "einvoiceBH.pdf");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
	
	@RequestMapping(value = "/getXml", method = RequestMethod.POST,
			consumes = {MediaType.APPLICATION_JSON_VALUE},		//MediaType.TEXT_PLAIN_VALUE, 
			produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<?> getXml(@RequestBody JSONRoot jsonRoot) throws Exception{
		FileInfo fileInfo = dao.getXml(jsonRoot);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-disposition", "attachment; filename=" + "bill.data");
		headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
		return ResponseEntity.ok()
				.headers(headers)
				.cacheControl(CacheControl.noCache())
				.body(SerializationUtils.serialize(fileInfo));
	}
}
