package vn.webcapnuoc.hddt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//https://stackjava.com/spring-boot/spring-boot-tuy-chinh-trang-whitelabel-error-page.html
//https://stackjava.com/spring/spring-mvc-exception-handling-xu-ly-exception-trong-spring-mvc.html
//@EnableAutoConfiguration(exclude = {
//	ErrorMvcAutoConfiguration.class
//})


@SpringBootApplication(scanBasePackages = "vn.webcapnuoc.hddt")
public class WebCapNuocApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebCapNuocApplication.class, args);
	}

}
