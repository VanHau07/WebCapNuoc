package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface EBookDAO {
	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception;
	public MsgRsp getEBook(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getEBookReadOnline(JSONRoot jsonRoot) throws Exception;
	public MsgRsp check(JSONRoot jsonRoot, String name) throws Exception;
}
