package vn.webcapnuoc.hddt.user.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import vn.webcapnuoc.hddt.dto.BaseDTO;

public class LoginRes extends BaseDTO{
	private static final long serialVersionUID = 1437427362761509867L;
	
	private String issuerId;
	private String userId;
	private String userName;
	@JsonIgnore
	private transient String password;
	private String fullName;
	private String phone;
	private String email;
	private boolean AdIssu;
	
	
	public boolean isAdIssu() {
		return AdIssu;
	}
	public void setAdIssu(boolean adIssu) {
		AdIssu = adIssu;
	}
	private String roles;
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	private boolean isRoot;
	private boolean isAdmin;
	private IssuerInfo issuerInfo;
	private String token;
	private List<String> fullRights;
	
	public String getIssuerId() {
		return issuerId;
	}
	public void setIssuerId(String issuerId) {
		this.issuerId = issuerId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isRoot() {
		return isRoot;
	}
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public IssuerInfo getIssuerInfo() {
		return issuerInfo;
	}
	public void setIssuerInfo(IssuerInfo issuerInfo) {
		this.issuerInfo = issuerInfo;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public List<String> getFullRights() {
		return fullRights;
	}
	public void setFullRights(List<String> fullRights) {
		this.fullRights = fullRights;
	}
	
}
