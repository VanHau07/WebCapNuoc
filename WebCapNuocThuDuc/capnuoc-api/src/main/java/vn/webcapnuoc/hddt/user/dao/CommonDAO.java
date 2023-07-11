package vn.webcapnuoc.hddt.user.dao;

import com.api.message.JSONRoot;
import com.api.message.MsgRsp;

import vn.webcapnuoc.hddt.dto.FileInfo;

public interface CommonDAO {
	public MsgRsp getFullParams(JSONRoot jsonRoot) throws Exception;
	public FileInfo printEInvoice(JSONRoot jsonRoot) throws Exception;
	public MsgRsp getAutoCompleteProducts(JSONRoot jsonRoot) throws Exception;
	
	public MsgRsp listSearchCustomer(JSONRoot jsonRoot) throws Exception;
	public MsgRsp listEInvoiceSigned(JSONRoot jsonRoot) throws Exception;
	FileInfo printExport(JSONRoot jsonRoot) throws Exception;
	public FileInfo viewpdf(JSONRoot jsonRoot)throws Exception;
	
	
	FileInfo printAgent(JSONRoot jsonRoot) throws Exception;
	FileInfo printEInvoiceBH(JSONRoot jsonRoot) throws Exception;
	public FileInfo printEinvoiceAll(JSONRoot jsonRoot)throws Exception;
	public FileInfo einvoiceXml(JSONRoot jsonRoot)throws Exception;
	public FileInfo print04(JSONRoot jsonRoot)throws Exception;
	public FileInfo viewpdftncn(JSONRoot jsonRoot)throws Exception;
	public FileInfo viewpdfcttncn(JSONRoot jsonRoot)throws Exception;
	public FileInfo getXml(JSONRoot jsonRoot)throws Exception;
}
