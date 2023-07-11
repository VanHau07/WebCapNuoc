package vn.webcapnuoc.hddt.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import vn.webcapnuoc.hddt.controller.AbstractController;
import vn.webcapnuoc.hddt.resources.RestAPIUtility;

@Controller
@RequestMapping(value = "/main")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class MainUserController extends AbstractController {
	@Autowired RestAPIUtility restAPI;
	@RequestMapping(value = { "/"}, method = { RequestMethod.GET, RequestMethod.POST })
	public String main(@PathVariable(name = "transaction", required = false, value = "") String transaction) throws Exception {
		return "redirect:/";
	}
}
