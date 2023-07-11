package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface BlogDAO {
	public MsgRsp getBlog(JSONRoot jsonRoot) throws Exception;
	
	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception;
	
	public MsgRsp getBlogNew(JSONRoot jsonRoot) throws Exception;
}
