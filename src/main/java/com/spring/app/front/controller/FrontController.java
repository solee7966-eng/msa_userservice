package com.spring.app.front.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.spring.app.front.service.FrontService;
import com.spring.app.product.domain.ProductImageDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class FrontController {
	
	private final FrontService frontService;
	
	@GetMapping("") 
	public String start() { 
		return "redirect:/index"; 
	}
	
	
//	@GetMapping(value={"","index"})
	@GetMapping("index")
	public ModelAndView index(ModelAndView mav) { 
		
		List <ProductImageDTO> productImageList = frontService.getProductImage();
		
		mav.addObject("productImageDtoList", productImageList);
		
		mav.setViewName("index");
	    //   /src/main/resources/templates/index.html 파일을 생성한다.
		
		return mav;
	}
	
}
