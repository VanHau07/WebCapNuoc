package vn.webcapnuoc.hddt.user.impl;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import vn.webcapnuoc.hddt.user.dao.AbstractDAO;
import vn.webcapnuoc.hddt.user.dao.LoginDAO;
import vn.webcapnuoc.hddt.user.dto.LoginRes;
import vn.webcapnuoc.hddt.user.dto.UserLoginReq;
import vn.webcapnuoc.hddt.utility.Constants;

@Repository
@Transactional
public class LoginImpl extends AbstractDAO implements LoginDAO{
	private static final Logger log = LogManager.getLogger(LoginImpl.class);
	@Autowired MongoTemplate mongoTemplate;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public LoginRes doAuth(UserLoginReq req) throws Exception {
		LoginRes res = new LoginRes();
		
		Document docFind = new Document("UserName", commons.regexEscapeForMongoQuery(req.getUserName()))
				.append("IsDelete", new Document("$ne", true));
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(new Document("$match", docFind));		
		Document docTmp = null;
		Document docSub = null;
		Iterable<Document> cursor = mongoTemplate.getCollection("Users").aggregate(pipeline).allowDiskUse(true);
		Iterator<Document> iter = cursor.iterator();
		
		if(iter.hasNext())
			docTmp = iter.next();
		
		if(docTmp == null) {
			res.setStatusCode(1);
			res.setStatusText("Người dùng không tồn tại trong hệ thống.");
			return res;
		}
		
		res = new LoginRes();
		res.setUserId(docTmp.getObjectId("_id").toString());
		res.setUserName(docTmp.getString("UserName"));
		res.setPassword(docTmp.get("Password", ""));
		res.setFullName(docTmp.get("FullName", ""));
		res.setPhone(docTmp.get("Phone", ""));
		res.setEmail(docTmp.get("Email", ""));
		res.setRoles(docTmp.get("Roles", ""));
		res.setRoot(docTmp.get("IsRoot", false));
		res.setAdmin(docTmp.get("IsAdmin", false));
		/*CHECK PASSWORD*/
		
		String passwordInput = commons.generateSHA(res.getUserName() + req.getPassword(), false).toUpperCase();
		String passwordInDB = docTmp.get("Password", "");
		
		if(!passwordInput.equals(passwordInDB)) {
			res = new LoginRes();
			res.setStatusCode(999);
			res.setStatusText("Mật khẩu đăng nhập không đúng.");
			return res;
		}
		
		
		/*END - CHECK PASSWORD*/
		
		res.setFullRights(new ArrayList<String>());
		
		/*JWT*/
		Map<String, Object> header = new HashMap<>();
		header.put(Header.TYPE, Constants.TOKEN_TYPE);
		header.put(JwsHeader.ALGORITHM, SignatureAlgorithm.HS512);
		
		Map<String, Object> claims = new HashMap<>();
		claims.put("data01", res.getUserId());
		claims.put("data02", res.getUserId());
		claims.put("data03", res.getUserName());
		claims.put("data04", commons.csRandomAlphaNumbericString(32));
		
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString(Constants.JWT_SECRET.getBytes()));
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		
		LocalDateTime now = LocalDateTime.now();
		now = now.plus(1, ChronoUnit.DAYS);
		
		JwtBuilder builder = Jwts.builder()
				.setHeader(header)
				.setId(null)
				.setClaims(claims)
				.setSubject(res.getUserId())
				.setIssuedAt(new Date())
				.setIssuer(Constants.TOKEN_ISSUER)
				.setAudience(Constants.TOKEN_AUDIENCE)
				.setExpiration(new Date(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
				.signWith(signingKey, signatureAlgorithm);
		
		String token = builder.compact();
		res.setToken(token);
		/*END - JWT*/
		
		res.setStatusCode(0);
		return res;
	}

}
