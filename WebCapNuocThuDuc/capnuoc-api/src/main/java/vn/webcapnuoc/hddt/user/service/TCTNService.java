package vn.webcapnuoc.hddt.user.service;

import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import vn.webcapnuoc.hddt.utility.Commons;

@Service
public class TCTNService {
	private static final Logger log = LogManager.getLogger(TCTNService.class);
	Commons commons = new Commons();
	TrustManager[] trustAllCerts = new TrustManager[] { 
		new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} 
	};
	

	

}
