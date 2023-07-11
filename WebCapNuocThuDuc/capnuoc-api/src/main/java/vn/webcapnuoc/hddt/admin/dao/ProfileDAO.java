package vn.webcapnuoc.hddt.admin.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface ProfileDAO {
	public MsgRsp changepass(JSONRoot jsonRoot) throws Exception;
	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception;
}
