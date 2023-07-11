package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface ForgotPassDAO {
	
	public MsgRsp crud(JSONRoot jsonRoot) throws Exception;
	public MsgRsp checktoken(JSONRoot jsonRoot) throws Exception;
}
