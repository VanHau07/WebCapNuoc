package vn.webcapnuoc.hddt.configuration;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.ServletContextAware;

import com.api.message.JSONRoot;
import com.api.message.Msg;
import com.api.message.MsgRsp;
import com.api.message.MspResponseStatus;
import com.fasterxml.jackson.databind.JsonNode;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.dto.BaseDTO;
import vn.webcapnuoc.hddt.resources.APIParams;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;
import vn.webcapnuoc.hddt.utils.Json;
import vn.webcapnuoc.hddt.utils.SystemParams;

@Configuration

public class InitializationParameter extends AbstractController implements InitializingBean, DisposableBean, ServletContextAware{
	private ServletContext context;
	private static final Logger log = LogManager.getLogger(InitializationParameter.class);
	@Autowired private Environment env;
	@Autowired MongoTemplate mongoTemplate;
	@Autowired RestAPIUtility restAPI;
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}
	@Override
	public void destroy() throws Exception {
		System.out.println("hddt web admin shutting down...");
	}
	@Override
	public void afterPropertiesSet()  throws Exception {
		
		
		System.out.println("Web Starting Initialization Parameters...");
		
		APIParams.HTTP_URI = env.getProperty("api.hddt.uri", "");
		APIParams.HTTP_LICENSEKEY = env.getProperty("api.hddt.license.key", "");
		
		SystemParams.DIR_TMP_SAVE_FILES = env.getProperty("dir.tmp.save.files", "");
		SystemParams.DIR_TEMPLATE_FILES = env.getProperty("dir.template.files", "");

		try {
			StringBuilder sb = new StringBuilder();
			
			BaseDTO dtoRes = new BaseDTO();
			Msg msg = dtoRes.createMsgPass();
			HashMap<String, String> hInput = new HashMap<>();
			msg.setObjData(hInput);
			JSONRoot root = new JSONRoot(msg);
			
			MsgRsp rsp = restAPI.callAPIPass("/index/getPath", HttpMethod.POST, root);
			MspResponseStatus rspStatus = rsp.getResponseStatus();
			JsonNode rows = null;
			if(rspStatus.getErrorCode() == 0) {
				JsonNode jsonData = Json.serializer().nodeFromObject(rsp.getObjData());
				if(!jsonData.at("/rows").isMissingNode()) {
					rows = jsonData.at("/rows");
				for(JsonNode row: rows) {
					SystemParams.DIR_IMAGES = commons.getTextJsonNode(row.at("/dir_images"));
					SystemParams.DIR_TMP = commons.getTextJsonNode(row.at("/dir_tmp"));
				}
				}
			}else {
				SystemParams.DIR_IMAGES = env.getProperty("dir.images", "");
				SystemParams.DIR_TMP = env.getProperty("dir.tmp", "");
			}

		}catch(Exception e) {
			log.error(" >>>>> An exception occurred!", e);
		}finally {
			
		}
	}

}
