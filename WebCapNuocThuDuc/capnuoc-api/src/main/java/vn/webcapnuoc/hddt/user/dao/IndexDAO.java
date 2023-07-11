package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

public interface IndexDAO {
	
	public MsgRsp getLogo(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getPath(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getHeader(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getFooter(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getIntroduce(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getTitle(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getFont(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getEBookHot(JSONRoot jsonRoot) throws Exception;
	public MsgRsp contact(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getBlog(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getBlogMain(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getSlide(JSONRoot jsonRoot) throws Exception;
//	public MsgRsp list(JSONRoot jsonRoot) throws Exception;
//	public MsgRsp detail(JSONRoot jsonRoot, String _id) throws Exception;
}
