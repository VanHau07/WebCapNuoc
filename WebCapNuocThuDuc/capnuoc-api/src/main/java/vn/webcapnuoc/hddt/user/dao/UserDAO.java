package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface UserDAO {

	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception;

	public MsgRsp crud(JSONRoot jsonRoot) throws Exception;

}
