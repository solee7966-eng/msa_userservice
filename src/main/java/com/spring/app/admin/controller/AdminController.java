package com.spring.app.admin.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.app.member.domain.MemberDTO;
import com.spring.app.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/")
public class AdminController {

	private final MemberService service;
		
	// 관리자 전용 메인 페이지
	@GetMapping("main")
	public List<MemberDTO> mypage(@AuthenticationPrincipal UserDetails userDetails) { 
		                       // Spring Security 에서 현재 로그인한 사용자의 정보를 반환하는 것이다.
	 /*	
		@AuthenticationPrincipal UserDetails userDetails 에서 userDetails 가 null 이 되지 않기 위해서는 
		반드시 클라이언트에서 /admin/main 를 호출할 때 http 헤더중 Authorization 헤더(인증 토큰 JWT 가 들어가는것임)를 보내야만 한다.
		즉,  ajax 로 headers: {"Authorization": `Bearer ${token}`} 을 보내주어야만 userDetails 가 null 이 되지 않는다.  
		
		@AuthenticationPrincipal UserDetails userDetails 은 	현재 인증된 사용자의 정보를 가져오는 데 사용된다.
		
		@AuthenticationPrincipal 은 Spring Security 에서 제공하는 어노테이션으로, 
		SecurityContextHolder 에 저장된 인증 정보를 UserDetails 객체로 자동 주입해준다.
		
		UserDetails 인터페이스는 Spring Security의 기본적인 사용자 정보(username(아이디 또는 이메일) + 비밀번호 + 권한 등)를 포함한다.

		SecurityConfig 클래스의 SecurityFilterChain filterChain(HttpSecurity http) 메소드에 정의한 URI 허가시
		JWT 인증을 위하여 직접 구현한 필터인 JwtAuthenticationFilter 를 UsernamePasswordAuthenticationFilter 전에 실행하도록 했으므로
		JwtAuthenticationFilter 클래스의 doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 메소드가
		먼저 실행되어진다. 그래서 클라이언트가 보내온 Http Request Header에서 JWT 토큰 추출하여 Authentication 객체를 생성한후 
		생성된 Authentication 객체를 SecurityContext 에 저장하도록 되도록 만들어 두었다. 
		그래서 SecurityContext 저장된 Authentication 객체가 @AuthenticationPrincipal 어노테이션에 의하여 UserDetails userDetails 에 자동 주입된다.
	*/	
		
		System.out.println("안녕하세요 /admin/main 페이지 입니다 , " + userDetails.getUsername() + " 님!");
		// 안녕하세요, admin 님!
		
		try {
			// authority 가 "ADMIN" 을 제외한 모든 사용자 읽어오기
			List<MemberDTO> allMember = service.allMember(); 
			
			return allMember;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
}
