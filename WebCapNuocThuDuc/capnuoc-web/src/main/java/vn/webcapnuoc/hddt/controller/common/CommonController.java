package vn.webcapnuoc.hddt.controller.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.dto.CurrentUserProfile;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Constants;
import vn.webcapnuoc.hddt.utils.SystemParams;

@Controller
@RequestMapping(value = {"/"})
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class CommonController extends AbstractController{
	private static final Logger log = LogManager.getLogger(CommonController.class);
	@Autowired RestAPIUtility restAPI;
	private String _token;
	private List<String> ids = null;
	@RequestMapping(value = "/common/generatingCaptcha", method = {RequestMethod.POST})
	public ResponseEntity<?> genCaptcha(HttpServletRequest req) throws Exception{
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);

		String captcha = commons.csRandomNumbericString(6);
		req.getSession(true).setAttribute(Constants.SESSION_CAPTCHA, captcha);

		BufferedImage bufferedImage = new BufferedImage(150, 45, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphics = bufferedImage.createGraphics();

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 150, 45);
		graphics.setColor(Color.CYAN);
		graphics.drawRoundRect(0, 0, 149, 44, 10, 10);

		graphics.setColor(Color.GREEN);
		graphics.drawRoundRect(-10, 20, 140, 25, 40, 10);
		graphics.setColor(Color.MAGENTA);
		graphics.drawRoundRect(30, 0, 130, 15, 10, 90);
		
		Color color = null;
		for (int i = 1; i <= captcha.length(); i++) {
			color = new Color(12, 14, 63);
			graphics.setFont(new Font("Courier New", Font.BOLD, 30));		//Arial	Font.PLAIN
			graphics.setColor(color);
			graphics.drawString(String.valueOf(captcha.charAt(i - 1)), (i - 1) * 25, 35);
		}
		graphics.dispose();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", baos);
		baos.flush();
		byte[] imageInByteArray = baos.toByteArray();
		baos.close();
		String b64 = javax.xml.bind.DatatypeConverter.printBase64Binary(imageInByteArray);

		return new ResponseEntity<byte[]>(b64.getBytes("utf-8"), httpHeaders, HttpStatus.OK);
	}
	
	
	
	
	@RequestMapping(value = "/e-images/{fileName:.+}", method = RequestMethod.GET)
	@ResponseBody
	public byte[] viewEImage(Locale locale, HttpServletRequest req, HttpSession session
			, HttpServletResponse response
			, @PathVariable(name = "fileName", required = true) String fileName) throws Exception{
		

		File image = new File(SystemParams.DIR_IMAGES);
		if (!image.exists()) {
			image.mkdir();
		}
		File file = new File(SystemParams.DIR_IMAGES, fileName);
		if(!file.exists() || !file.isFile()) {
			file = new File(SystemParams.DIR_IMAGES, SystemParams.FILENAME_LOGO_COMPANY);
			return null;
		}else {
			InputStream in = new FileInputStream(file);
		    return IOUtils.toByteArray(in);
		}
	}
	
	@RequestMapping(value = "/view-pdf/{fileName:.+}", method = RequestMethod.GET)
	@ResponseBody
	public void viewPDF(Locale locale, HttpServletRequest req, HttpServletResponse resp, HttpSession session
			, @PathVariable(name = "fileName", required = true) String fileName
		) throws Exception {
		PrintWriter writer = null;
		
		String uri = req.getRequestURI();
		File image = new File(SystemParams.DIR_IMAGES);
		if (!image.exists()) {
			image.mkdir();
		}
		File file = new File(SystemParams.DIR_IMAGES, fileName);
		if(!file.exists() || !file.isFile()) {
			file = new File(SystemParams.DIR_IMAGES, SystemParams.FILENAME_LOGO_COMPANY);
			return;
		}else {
		InputStream in = new FileInputStream(file);			
		String type = "application/pdf";
		InputStream inputStream = new ByteArrayInputStream(IOUtils.toByteArray(in));
		resp.setHeader("Content-Type", type);
		
		resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); 
		resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		resp.setHeader("Expires", "0"); // Proxies.
		
		int bufferSize = 1024;
		resp.setContentType(type);
        final byte[] buffer = new byte[bufferSize];
        int bytesRead = 0;
        OutputStream out = null;
        try {
            out = resp.getOutputStream();
            long totalWritten = 0;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
                totalWritten += bytesRead;
                if (totalWritten >= buffer.length) {
                    out.flush();
                }
            }
        } finally {
            tryToCloseStream(out);
            tryToCloseStream(inputStream);
        }
	}
        
	}
	
	@RequestMapping(value = "/admin/common/processUploadFile", produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public BaseDTO processUploadFile(HttpServletRequest req, 
			HttpSession session, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception{
		BaseDTO dtoRes = new BaseDTO();
//		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		
		byte[] data = null;
		String originalFilename = "";
		
		Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
		for (MultipartFile multipartFile : fileMap.values()) {
			originalFilename = multipartFile.getOriginalFilename();
			data = multipartFile.getBytes();
			break;
		}
		
		if(data == null) {
			dtoRes = new BaseDTO(1, "Dữ liệu upload không tồn tại.");
			return dtoRes;
		}
		
		String extensionFile = FilenameUtils.getExtension(originalFilename);
		String fileNameSystem = UUID.randomUUID() + "-" + commons.csRandomAlphaNumbericString(5) + "." + extensionFile;
		
		FileCopyUtils.copy(data, new FileOutputStream(new File(SystemParams.DIR_IMAGES, fileNameSystem)));
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SystemFilename", fileNameSystem);
		map.put("OriginalFilename", originalFilename);
		dtoRes.setResponseData(map);
		return dtoRes;
	}
	
	@RequestMapping(value = "/admin/common/processUploadFileTmp", produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public BaseDTO processUploadFileTmp(HttpServletRequest req, 
			HttpSession session, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception{
		BaseDTO dtoRes = new BaseDTO();
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		
		byte[] data = null;
		String originalFilename = "";
		
		Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
		for (MultipartFile multipartFile : fileMap.values()) {
			originalFilename = multipartFile.getOriginalFilename();
			data = multipartFile.getBytes();
			break;
		}
		
		if(data == null) {
			dtoRes = new BaseDTO(1, "Dữ liệu upload không tồn tại.");
			return dtoRes;
		}
		
		/*KIEM TRA THU MUC TMP; NEU CHUA CO THI TAO*/
		File file = new File(SystemParams.DIR_TMP);
		if(!file.exists()) file.mkdirs();
		
		String extensionFile = FilenameUtils.getExtension(originalFilename);
		String fileNameSystem = UUID.randomUUID() + "-" + commons.csRandomAlphaNumbericString(5) + "." + extensionFile;
		
		FileCopyUtils.copy(data, new FileOutputStream(new File(SystemParams.DIR_TMP, fileNameSystem)));
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SystemFilename", fileNameSystem);
		map.put("OriginalFilename", originalFilename);
		dtoRes.setResponseData(map);
		return dtoRes;
	}
		
	
	
	@RequestMapping(value = "/admin/common/processUploadFileTmp1", produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public BaseDTO processUploadFileTmp1(HttpServletRequest req, 
			HttpSession session, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception{
		BaseDTO dtoRes = new BaseDTO();
		CurrentUserProfile cup = getCurrentlyAuthenticatedPrincipal();
		
		byte[] data = null;
		String originalFilename1 = "";
		
		Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
		for (MultipartFile multipartFile : fileMap.values()) {
			originalFilename1 = multipartFile.getOriginalFilename();
			data = multipartFile.getBytes();
			break;
		}
		
		if(data == null) {
			dtoRes = new BaseDTO(1, "Dữ liệu upload không tồn tại.");
			return dtoRes;
		}
		
		/*KIEM TRA THU MUC TMP; NEU CHUA CO THI TAO*/
		File file = new File(SystemParams.DIR_TMP);
		if(!file.exists()) file.mkdirs();
		
		String extensionFile1 = FilenameUtils.getExtension(originalFilename1);
		String fileNameSystem1 = UUID.randomUUID() + "-" + commons.csRandomAlphaNumbericString(5) + "." + extensionFile1;
		
		FileCopyUtils.copy(data, new FileOutputStream(new File(SystemParams.DIR_TMP, fileNameSystem1)));
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SystemFilename1", fileNameSystem1);
		map.put("OriginalFilename1", originalFilename1);
		dtoRes.setResponseData(map);
		return dtoRes;
	}
		
		
}