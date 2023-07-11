package vn.webcapnuoc.hddt.admin.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface MainDAO {
	public MsgRsp getLogo(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getHeader(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getTitle(JSONRoot jsonRoot) throws Exception;
	public MsgRsp list(JSONRoot jsonRoot) throws Exception;
}
