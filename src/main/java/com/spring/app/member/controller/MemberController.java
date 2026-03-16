package com.spring.app.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.spring.app.member.domain.MemberDTO;
import com.spring.app.entity.Member;
import com.spring.app.member.service.MemberService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/member/")
public class MemberController {

	// 의존객체 주입하기(DI: Dependency Injection) - 생성자 주입 (Constructor Injection) 사용
	private final MemberService service;
	private final PasswordEncoder passwordEncoder; // com.spring.app.config.SecurityConfig 클래스에서 빈으로 만들어 두었음.
	
	
	// 회원가입
	@GetMapping("register")
	public String register() {
		return "member/register";
	}
	
	
	// 아이디중복검사
	@PostMapping("memberidCheck")
	@ResponseBody
	public Map<String, Boolean> memberidCheck(@RequestParam String memberid) {
		
		Map<String, Boolean> map = new HashMap<>();
		
		try {
			int n = service.memberidCheck(memberid);
			
			if(n==1) {
				map.put("isExists", true);	
			}
			else {
				map.put("isExists", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		 //	map.put("isExists", true);
		}
		
		return map;
	}
	
	
	// 회원가입.  DB에 insert 하기 
	@PostMapping("register")
	public ModelAndView register(MemberDTO memberDto, ModelAndView mav){
			
		// >>>> 사용자 패스워드 암호화 하기
		/*
		    비밀번호 암호화를 위해 BCryptPasswordEncodr의 해시 함수를 이용하여 비밀번호를 암호화하여 DB에 저장하도록 한다.
		    이를 위해 BCryptPasswordEncoder 를 빈으로 등록하여 사용한다.
		    passwordEncoder.encode(암호화할 비밀번호) 를 사용하면 암호화 된 값이 나오고,
	        비교할 때는 if문을 사용하여 passwordEncoder.matches(입력받은 비밀번호, 암호화된 비밀번호)로 비교해주면 된다!
		 */
		String hashedUserPwd = passwordEncoder.encode(memberDto.getPasswd());
	 // System.out.println(">>>> 암호화 hashedUserPwd : " + hashedUserPwd);
		// >>>> 암호화 hashedUserPwd : $2a$10$R2rV0/IedVXZ65J7ZGv2KODnFhlHqeTh5i.2LADueIDIo6tGyCdve  동일한 qwer1234$ 이지만 암호화시 salt(암호화키)가 내부적으로 추가되기 때문에 암호화가 된 값은 매번 바뀐다. 암호화가 강력하다.
		// >>>> 암호화 hashedUserPwd : $2a$10$C9ZujzlAdaxIzYVqGu5GaOZSruM3dR2VUqx13MhHHGWfkHEACmQIy  동일한 qwer1234$ 이지만 암호화시 salt(암호화키)가 내부적으로 추가되기 때문에 암호화가 된 값은 매번 바뀐다. 암호화가 강력하다. 
			
		memberDto.setPasswd(hashedUserPwd);
		
		Member member = Member.builder()
				.memberid(memberDto.getMemberid())
				.passwd(memberDto.getPasswd())
				.name(memberDto.getName())
				.build();

		StringBuilder sb = new StringBuilder();
		
		try {
			  int result = service.insertMember(member);
			  
			  if(result == 1) {
				  mav.addObject("success", "1");
			  		
				  sb.append("<span style='font-weight: bold;'>"+ memberDto.getName() + "</span>님의 회원 가입이 정상적으로 처리되었습니다.<br/>");
				  sb.append("메인메뉴에서 \"로그인\" 을 클릭하여 로그인 하시기 바랍니다.<br/>");
			  }
			  else {
				  mav.addObject("success", "0");
			  		
				  sb.append("<span style='font-weight: bold;'>"+ memberDto.getName() + "</span>님의 회원 가입이 실패 되었습니다.<br/>");
			  }
			  
			  mav.addObject("message", sb.toString());
			  
		} catch(Exception e) {
			mav.addObject("success", "0");
			sb.append("<span style='font-weight: bold;'>"+ memberDto.getName() + "</span>님의 회원 가입이 실패 되었습니다.<br/>");
			mav.addObject("message", sb.toString());
			
			e.printStackTrace();
		}
		
		mav.setViewName("member/register_result");
		
		return mav; 
	}
	
	
	// 로그인
	@GetMapping("login")
	public String login() {
		return "member/login";
	}
	
	
	// 내정보
	@GetMapping("mypage")
	@ResponseBody
	public MemberDTO mypage(@AuthenticationPrincipal UserDetails userDetails) { // Spring Security 에서 현재 로그인한 사용자의 정보를 반환하는 것이다.
	 /*	
		@AuthenticationPrincipal UserDetails userDetails 에서 userDetails 이 null 이 되지 않기 위해서는 
		반드시 클라이언트에서 /member/mypage 를 호출할 때 http 헤더중 Authorization 헤더(인증 토큰 JWT 가 들어가는것임)를 보내야만 한다.
		즉,  ajax 로 headers: {"Authorization": `Bearer ${token}`} 을 보내주어야만 userDetails 이 null 이 되지 않는다.  
		
		@AuthenticationPrincipal UserDetails userDetails 은 
		현재 인증된 사용자의 정보를 가져오는 데 사용된다.
		
		@AuthenticationPrincipal 은 Spring Security 에서 제공하는 어노테이션으로, 
		SecurityContextHolder 에 저장된 인증 정보를 UserDetails 객체로 자동 주입한다.
		
		UserDetails 인터페이스는 Spring Security의 기본적인 사용자 정보(아이디, 비밀번호, 권한 등)를 포함한다.

		SecurityConfig 클래스의 SecurityFilterChain filterChain(HttpSecurity http) 메소드에 정의한 URI 허가시
		JWT 인증을 위하여 직접 구현한 필터인 JwtAuthenticationFilter 를 UsernamePasswordAuthenticationFilter 전에 실행하도록 했으므로
		JwtAuthenticationFilter 클래스의 doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 메소드가
		먼저 실행되어진다. 그래서 클라이언트가 보내온 Http Request Header에서 JWT 토큰 추출하여 Authentication 객체를 생성한후 
		생성된 Authentication 객체를 SecurityContext 에 저장하도록 되도록 만들어 두었다. 
		그래서 SecurityContext 저장된 Authentication 객체가 @AuthenticationPrincipal 어노테이션에 의하여 UserDetails userDetails 에 자동 주입된다.
	*/	
		
		System.out.println("안녕하세요 /member/mypage 입니다 , " + userDetails.getUsername() + " 님!");
		// 안녕하세요, seoyh 님!
		// userDetails 는 com.spring.app.auth.domain.CustomUserDetails 이다.  
		// userDetails.getUsername() 이 아이디(memberDto.getMemberid()) 이다.
		
		try {
			Member member = service.getMember(userDetails.getUsername());
			
			return member.toDTO();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
}
