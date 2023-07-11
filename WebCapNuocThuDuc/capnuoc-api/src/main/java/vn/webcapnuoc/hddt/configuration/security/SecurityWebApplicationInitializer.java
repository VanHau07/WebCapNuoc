package vn.webcapnuoc.hddt.configuration.security;

import javax.servlet.ServletContext;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import vn.webcapnuoc.hddt.filter.CheckApiLicenseKeyFilter;

public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
	
	@Override
	protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		insertFilters(servletContext, new CheckApiLicenseKeyFilter());	//, new CheckLicenseEInvoiceFilter(): NOT @Autowired ==> NOT USED, USE Interceptor
	}
}
