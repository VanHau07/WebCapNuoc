package vn.webcapnuoc.hddt.user.dao;

import vn.webcapnuoc.hddt.user.dto.LoginRes;
import vn.webcapnuoc.hddt.user.dto.UserLoginReq;

public interface LoginDAO {
	public LoginRes doAuth(UserLoginReq req) throws Exception;
}
