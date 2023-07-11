package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface PhotoDAO {
	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception;
	public MsgRsp getPhoto(JSONRoot jsonRoot) throws Exception;
}
