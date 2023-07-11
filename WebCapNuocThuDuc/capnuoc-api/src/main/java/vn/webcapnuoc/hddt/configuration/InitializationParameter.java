package vn.webcapnuoc.hddt.configuration;

import java.util.Iterator;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.context.ServletContextAware;

import vn.webcapnuoc.hddt.utility.SystemParams;

@Configuration
public class InitializationParameter implements InitializingBean, DisposableBean, ServletContextAware{
	private static final Logger log = LogManager.getLogger(InitializationParameter.class);
	private ServletContext context;
	@Autowired private Environment env;
	@Autowired MongoTemplate mongoTemplate;
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("hddt-api shutting down...");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("API: Starting Initialization Parameters...");
		SystemParams.VERSION = env.getProperty("api.version", "1.0");
		SystemParams.VERSION_XML = env.getProperty("version.xml", "2.0.0");
		SystemParams.DIR_TEMPORARY = env.getProperty("dir.temporary", "");
		try {
			StringBuilder sb = new StringBuilder();
			
			Document docTmp = null;
			Iterable<Document> cursor = mongoTemplate.getCollection("ApiLicenseKey").find(new Document("ActiveFlag", true));
			Iterator<Document> iter = cursor.iterator();
			while(iter.hasNext()) {
				docTmp = iter.next();
				SystemParams.LIST_APILICENSEKEY.add(docTmp.get("LicenseKey", ""));
			}	
			
			
			Document docTmp1 = null;
			Iterable<Document> cursor1 = mongoTemplate.getCollection("Path").find(new Document("ActiveFlag", true));
			Iterator<Document> iter1 = cursor1.iterator();
			while(iter1.hasNext()) {
				docTmp1 = iter1.next();
			}	
			if(docTmp1==null) {
				SystemParams.DIR_IMAGES = env.getProperty("dir.images", "");
				SystemParams.DIR_TMP = env.getProperty("dir.tmp", "");
			}else {
				SystemParams.DIR_IMAGES = docTmp1.get("dir_images", "");
				SystemParams.DIR_TMP = docTmp1.get("dir_tmp", "");
			}
		}catch(Exception e) {
			log.error(" >>>>> An exception occurred!", e);
		}finally {
			
		}
		
	}

}
