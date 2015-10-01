package net.rzt.kat.simplewebApp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	@RequestMapping(value="/")
	public ModelAndView test(HttpServletResponse response) throws IOException{
		return new ModelAndView("home");
	}
	@RequestMapping(value="/notification")
	public ModelAndView notification(HttpServletResponse request , HttpServletResponse response) throws IOException{
		System.out.println("request came");
		
		
		ModelAndView mv =new ModelAndView("notified");
		mv.addObject("details", "ok ");
		return mv;
	}
}
