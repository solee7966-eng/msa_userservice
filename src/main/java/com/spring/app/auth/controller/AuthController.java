package com.spring.app.auth.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.app.auth.domain.LoginUserDTO;
import com.spring.app.auth.domain.MemberIdPasswdDTO;
import com.spring.app.auth.domain.TokenRequestDTO;
import com.spring.app.auth.service.AuthService;
import com.spring.app.entity.Member;
import com.spring.app.member.domain.MemberDTO;
import com.spring.app.security.JwtToken;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
  - HttpEntity 클래스
    : HttpEntity 클래스는 Spring framework 에서 HTTP 요청(Request) 또는 응답(Response)을 나타내는 객체이다. 
      주로 REST API를 호출하거나 응답을 반환할 때 사용된다.
      이 클래스는 HTTP 헤더와 바디를 함께 포함할 수 있으며, 제네릭 타입을 이용해 유연하게 데이터를 다룰 수 있다.
      HttpEntity 클래스를 상속받아 만들어진 자식 클래스에는 RequestEntity, ResponseEntity가 있다.
  
  - RequestEntity<T>  (HTTP 요청)
    : HTTP 요청(Request) 정보를 감싸는 클래스이다. 요청 메소드(GET, POST, PUT, DELETE 등), URL 정보, 헤더(Header), 요청 본문(Body)바디를 포함한다.
    : RestTemplate을 사용하여 API 호출 시 이용된다.
    
    예제: RequestEntity를 사용하여 API 호출하기  
	import org.springframework.http.*;
	import org.springframework.web.client.RestTemplate;
	import java.net.URI;
	
	public class RequestEntityExample {
	    
	    public static void main(String[] args) {
	        
	        RestTemplate restTemplate = new RestTemplate();
	
	        // 헤더 설정
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer my-token");
	        headers.setContentType(MediaType.APPLICATION_JSON);
	
	        // 요청 바디
	        String requestBody = "{ \"name\": \"Alice\" }";
	
	        // RequestEntity 생성 (POST 요청) - RequestEntity를 사용하여 POST 요청을 생성하고 헤더와 바디를 추가함.
	        RequestEntity<String> requestEntity = RequestEntity
	            .post(URI.create("https://example.com/api"))
	            .headers(headers)
	            .body(requestBody);
	
	        // RestTemplate.exchange()를 사용하여 API를 호출.
	        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
	        System.out.println("Response: " + response.getBody());
	    }
	}

    
    
  - ResponseEntity<T> (HTTP 응답)
    : 사용자의 HttpRequest에 대한 HTTP 응답 정보를 감싸는 클래스
    : HttpStatus, HttpHeaders, HttpBody를 포함한다.
    : HTTP 상태 코드(Status Code), 응답 헤더(Header), 응답 본문(Body) 를 포함한다.
    : REST API 컨트롤러에서 응답을 반환할 때 사용한다.
    : ResponseEntity.ok(), ResponseEntity.status() 등의 static 메소드를 제공한다.
    
  예제 1: ResponseEntity를 사용하여 응답 반환 (Spring REST 컨트롤러)
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.*;
	
	@RestController
	@RequestMapping("/api")
	public class UserController {
	    
	    @GetMapping("/user")
	    public ResponseEntity<String> getUser() {
	        String responseBody = "{ \"name\": \"Alice\", \"age\": 25 }";
	        return ResponseEntity.ok(responseBody); // ResponseEntity.ok()를 사용하여 JSON 형태의 응답 반환.
	                                                // HTTP 상태 코드 200 OK와 함께 데이터를 반환.
	    }
	}
	
  예제 2: 커스텀 HTTP 상태 코드와 헤더 추가
	import org.springframework.http.*;
	import org.springframework.web.bind.annotation.*;
	
	@RestController
	@RequestMapping("/api")
	public class UserController {
	
	    @PostMapping("/user")
	    public ResponseEntity<String> createUser(@RequestBody String user) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Custom-Header", "UserCreated");
	
	        return ResponseEntity
	            .status(HttpStatus.CREATED) // 201 Created // ResponseEntity.status(HttpStatus.CREATED)를 사용하여 201 Created 상태 반환.
	            .headers(headers)  // HttpHeaders를 이용해 커스텀 헤더 추가.
	            .body("User Created Successfully");
	    }
	}
	
	
 >> RequestEntity vs ResponseEntity 비교 <<
 -----------------------------------------------------------------------
  기능	    RequestEntity<T> (요청)	         ResponseEntity<T> (응답)
 -----------------------------------------------------------------------
  용도	    HTTP 요청을 생성할 때 사용	         HTTP 응답을 반환할 때 사용
  포함 내용	HTTP 메서드, URL, 헤더, 바디	     HTTP 상태 코드, 헤더, 바디
  주요 메소드	.post(), .get(), .put(),         .ok(), .status(), 
            .headers(), .body()	             .headers(), .body()
  사용 위치	RestTemplate에서 API 호출 시	     REST 컨트롤러에서 응답 반환 시	
 ------------------------------------------------------------------------ 
 
 
 예제: 클라이언트에서 RequestEntity를 사용해 API 요청 & 서버에서 ResponseEntity로 응답
 
  [클라이언트 코드] (API 호출)
	import org.springframework.http.*;
	import org.springframework.web.client.RestTemplate;
	import java.net.URI;
	
	public class ClientApp {
	    public static void main(String[] args) {
	        RestTemplate restTemplate = new RestTemplate();
	
	        // 요청 헤더 설정
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	
	        // 요청 바디 설정
	        String requestBody = "{ \"username\": \"Alice\" }";
	
	        // RequestEntity 생성 (POST 요청)
	        RequestEntity<String> requestEntity = RequestEntity
	            .post(URI.create("http://localhost:8080/api/user"))
	            .headers(headers)
	            .body(requestBody);
	
	        // API 호출
	        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
	
	        // 응답 출력
	        System.out.println("Response: " + response.getBody());
	        System.out.println("Status Code: " + response.getStatusCode());
	    }
	}
	
	
  [서버 코드] (API 응답)	
	import org.springframework.http.*;
	import org.springframework.web.bind.annotation.*;
	
	@RestController
	@RequestMapping("/api")
	public class UserController {
	
	    @PostMapping("/user")
	    public ResponseEntity<String> createUser(@RequestBody String user) {
	        System.out.println("Received request: " + user);
	
	        return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body("User Created Successfully");
	    }
	}

  >> 실행 과정 <<:
	1. 클라이언트가 RequestEntity를 이용해 POST 요청을 보냄.
	2. 서버가 ResponseEntity를 사용해 201 Created 응답을 반환.
	3. 클라이언트는 응답을 받고 ResponseEntity.getBody()로 내용 확인.

*/

@RestController
@RequestMapping(value = "/auth/")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	 
	// 의존객체 DI 생성자 주입
	private final AuthService authService; 
	
	// 로그인 처리하기
    @PostMapping("login")
    public ResponseEntity<LoginUserDTO> login(@RequestBody MemberIdPasswdDTO dto, 
    		                                  HttpServletRequest request,
    		                                  HttpServletResponse response) {
    	// 파라미터에 어노테이션 @RequestBody 는 입력받는 데이터 값이 JSON 형태의 문자열 이므로, 
    	// 이것(JSON 형태의 문자열)을 자동적으로 자바의 객체로 변형시켜주는 것이 @RequestBody 이다.
    	// src/main/resources/templates/member/login.html 파일의 
    	// 로그인을 요청한 함수 func_Login() 에서 전송한 문자열 "{"memberid":memberid,"passwd":passwd}" 을 받아와 
    	// MemberIdPasswdDTO 객체로 변형한 후 dto 에 저장한 것이다. 
    	
    	/* 리턴타입 ResponseEntity<T> 은 사용자의 HttpRequest에 대한 HTTP 응답 정보를 "감싸는" 클래스 로서 
    	   REST 컨트롤러에서 응답 반환 시에 사용하는 것이다.
    	   ResponseEntity 에 들어가는 내용은 
           HttpStatus(상태코드), HttpHeaders(헤더), HttpBody(응답본문)을 포함한다.
           ResponseEntity.ok(), ResponseEntity.status() 등의 static 메소드를 제공한다.
            
           @RestController 클래스의 메서드는 리턴타입이 String 아니면 JSON 이어야 하는데
           ResponseEntity.ok("객체"); 는 HTTP 상태 코드 200 OK와 함께 "객체"를 JSON 형태로 바꾸어서 반환해주는 메서드 이라서
           @RestController 클래스 메서드의 리턴타입으로 많이 사용된다.     
    	*/
    	
    	    System.out.println("~~~~~~~~~~~ 1단계");
    	
    	String memberid = dto.getMemberid();  // 사용자ID
        String passwd = dto.getPasswd();      // 비밀번호
        
        	log.info("### request memberid = {}, passwd = {}", memberid, passwd);
        	// 2026-03-01T10:34:53.881+09:00 INFO 22116 --- [nio-9090-exec-6] c.spring.app.controller.AuthController  : request username = seoyh, password = qwer1234$  
        
        // === 로그인 처리하여 우리가 만든 JwtToken 값 받아오기 === // 
        JwtToken jwtToken = authService.login(memberid, passwd); 
        // authService.login(memberid, passwd); 을 잘 봐야 한다.!!! 
        
        	log.info("### jwtToken accessToken = {}, refreshToken = {}", jwtToken.getAccessToken(), jwtToken.getRefreshToken());
        	// 2026-03-01T10:34:54.265+09:00 INFO 22116 --- [nio-9090-exec-6] c.spring.app.controller.AuthController  : jwtToken accessToken = eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzZW95aCIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTc0MjM0OTg5NH0.4Av9_3dfeRq84mgx1c8Bjn4ZffSxS9K64GVBKmdnga6z4y_QMnSycRqXNpou88mO, refreshToken = eyJhbGciOiJIUzM4NCJ9.eyJleHAiOjE3NDI5NTI4OTR9.wdecRHUtXcwXFLQ-ClbUR1XThvRSKQveq6rDaQjIBXhvrL0iKQYrG7YFwDJ4DFjc 
        
            System.out.println("######## jwtToken : " + jwtToken);
            // ######## jwtToken : JwtToken(grantType=Bearer, accessToken=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzZW95aCIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTc0MjM1MTI0Nn0.lM6aO7Rq03ljcPEe6raRKaVs-DDWGAyg17DvNzNvLhMO3GcD0k488bvrEbsXkKTe, accessTokenExpiresIn=1742351246366, refreshToken=eyJhbGciOiJIUzM4NCJ9.eyJleHAiOjE3NDI5NTQyNDZ9.8qOe9fonjZg16zJZtVgAIAZR1Ntm12Ze8CbbwRC5Xo-Hmkpckjp_Scm7aEGbMvmy) 
        
            System.out.println("====== 인증결과 확인용 시작 =====");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 인증(Authentication) 정보와 권한(Authorities)을 담는 객체인 SecurityContext 에서 올라가 있는 사용자의 출입증을 꺼내온다.  
        
            System.out.println(auth);
        // UsernamePasswordAuthenticationToken [Principal=org.springframework.security.core.userdetails.User [Username=seoyh, Password=[PROTECTED], Enabled=true, AccountNonExpired=true, CredentialsNonExpired=true, AccountNonLocked=true, Granted Authorities=[ROLE_USER]], Credentials=[PROTECTED], Authenticated=true, Details=null, Granted Authorities=[ROLE_USER]] 
        
         // !!! 세션에 SecurityContext 를 저장하기 => SSR 렌더링에서는 세션 기반 인증 가능 → sec:authorize 동작 OK !!!
            HttpSession session = request.getSession();
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                 SecurityContextHolder.getContext());
            // !!! HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY 은 상수임. !!!
            // SecurityContextHolder.getContext() 은 SecurityContext를 반환하는 정적 메서드 임.
            // - SecurityContext => 인증(Authentication) 정보와 권한(Authorities)을 담는 객체
            // - SecurityContextHolder => SecurityContext 객체를 저장하고 접근하는 전역 저장소(Holder)
            // - Authentication 에는 Principal(사용자(UserDetails) 정보), Credentials(비밀번호), Authorities(권한)가 포함되어 있음
            // !!! Thymeleaf SSR에서 sec:authorize를 쓰려면, 세션을 일부 허용하는 하이브리드 방식이 사실상 유일한 방법임. !!!
            /*
              << 이 하이브리드 방식의 동작 흐름 >>
    		  1. 로그인 성공 → JWT 발급 + SecurityContext 에 인증정보인 Authentication authentication 객체를 저장하기
              2. 세션에 SecurityContext 를 저장하기
              3. 이후 HTML 요청 시 자동적으로 SecurityFilterChain 이 세션에 저장된 인증 정보인 SecurityContext 를 꺼내서 SecurityContextHolder 에 저장함.
              4. sec:authorize 는 정상 동작하게 됨
            */
            
            System.out.println("====== 인증결과 확인용 끝 =====");
        
        
     // memberid(아이디)를 가지고 권한까지 포함한 회원정보(entity) 가져오기
        Member member = authService.findByIdWithAuthorities(memberid);
                
        MemberDTO memberDto = member.toDTO();
        
     // JWT 으로 되어질 LoginUserDTO 생성하기   
     // LoginUserDTO 는 로그인 되어진 이후에 사용되어질 사용자 DTO 이다.
        LoginUserDTO loginUserDto = LoginUserDTO.builder()
                .memberid(memberDto.getMemberid())
                .name(memberDto.getName())
                .grantType(jwtToken.getGrantType())
                .accessToken(jwtToken.getAccessToken())
                .accessTokenExpiresIn(jwtToken.getAccessTokenExpiresIn())
             // .refreshToken(jwtToken.getRefreshToken())  // refreshToken 을 localStorage 에 저장하면 XSS(Cross-Site Scripting)공격에 매우 취약하다. 그러므로 refreshToken 은 아래처럼 HttpOnly Cookie (읽기전용 쿠기)로 보낸다. 
                .build();
        
     // JWT 구조에서 refreshToken 은 반드시 HttpOnly Cookie 에 저장해야만 보안적으로 훨씬 안전하다. 
     /* 
        refreshToken 을 반드시 HttpOnly Cookie 에 저장하는 이유는 아래와 같다.
        1. localStorage 에 저장하면 
           - JS 로 접근 가능하고,
           - XSS(Cross-Site Scripting) 발생 시 탈취가 가능하기 때문이다.

        2. 반면에 HttpOnly Cookie 에 저장하면
           - JavaScript 로 접근이 불가하며
           - 브라우저가 자동으로 요청에 포함시켜 주며
           - XSS(Cross-Site Scripting) 으로 읽을 수 없기 때문이다.
     */
        
        // 스프링 버전 5 이상에서의 쿠키 생성
        // 쿠키 밸류값은 refreshToken 임
        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                "refreshToken", // 쿠키명
                jwtToken.getRefreshToken() // 쿠키밸류값은 refreshToken 임 
        )
                .httpOnly(true) // HttpOnly Cookie 에 저장함으로써 JavaScript 로 접근이 불가하도록 만든다.
                .secure(false)  // localhost HTTP 이므로 false임.   https 이라면 .secure(true)
                .path("/")      // 쿠키가 사용되어질 범위 
                .maxAge(60 * 60 * 24 * 7)  // 쿠키수명 7일 
                .sameSite("Strict")   // 단일 도메인인 경우 .sameSite("Strict") , React 사용한 프론트(localhost:3000) / Spring 울 사용한 백엔드(localhost:9082) 로 분리된 경우라면 .sameSite("None")으로 해야함  .sameSite() 를 생략하면 기본적으로 sameSite("Lax") 되어짐.  
                .build();

        // 생성된 쿠키 전송하기 
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        
        /*
           ★★ 쿠키 생성시 이 쿠키가 사용되어지는 범위 지정하기 ★★
           sameSite("Strict") = "우리 집 사람들만 출입 가능함 (보안 높음)"  
                              ==> 단일 도메인(localhost:9082) 웹앱 환경에서 사용 권장.
                              ==> 단일 도메인(localhost:9082) 사이트에서 보내온 쿠키만 허락함.
                              ==> 사용자가 악성 사이트에 접속하여 보내온 쿠키라면 무조건 쿠키 차단 시킴. 
                                
           sameSite("Lax")    = "문을 열어두어 외부에서 들어올수 있으나, 외부접근은 소포(GET 방식만)는 받는다" 
                              ==> 일반 웹사이트 환경에서 사용 권장
                              ==> 단일 도메인(localhost:9082) 사이트에서 보내온 쿠키는 허락함.
                              ==> 사용자가 다른 사이트에 접속하여 보내온 쿠키라면 GET 방식만 허락함. POST 방식은 허락하지 않음.  
                              ==> 기본값임  .sameSite() 를 생략하면 기본적으로 sameSite("Lax") 가 되어짐.
                              
           sameSite("None")   = "아무나 다 들어와도 된다 (보안 낮음)"      
                              ==> 서로다른 도메인 환경(프론트 React(localhost:3000) 와 백엔드 Spring(localhost:9082)가 분리된 환경에서 사용해야 함.
                              ==> 보안을 확보하기 위해 http 가 아닌 https 로 사이트를 구축해야 하며 .secure(true) 라고 해주어야 함. 
        
           .sameSite("Strict") 와 .sameSite("Lax") 은 거의 차이가 없다.
           단지, 보안강화를 위해서 단일 도메인(localhost:9082)으로 사용하는 경우라면 보안강화를 위해서 .sameSite("Strict") 으로 사용한다.
        
         */
        
        System.out.println("~~~~ 확인용 ResponseEntity.ok(loginDto) : " + ResponseEntity.ok(loginUserDto));
        // ~~~~ 확인용 ResponseEntity.ok(loginUserDto) : <200 OK OK,com.spring.app.domain.LoginDto@74658bc7,[]>
        
        return ResponseEntity.ok(loginUserDto); 
        // ResponseEntity.ok()를 사용하여 응답을 JSON 형태로 반환함.
        // HTTP 상태 코드 200 OK와 함께 데이터를 JSON 형태로 반환함.
    }
   
    
    // 유효기간이 지난 토큰이라면 토큰을 재발급 하기  
    @PostMapping("reissue") 
    public ResponseEntity<JwtToken> reissue(@RequestBody TokenRequestDTO tokenRequestDto,
    		                                HttpServletRequest request,
    		                                HttpServletResponse response) {
    	
    	System.out.println("########## 토큰 재발급 확인용 : /auth/reissue 호출됨!!!");
    	
    	// 쿠키에서 refreshToken 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    tokenRequestDto.setRefreshToken(cookie.getValue());
                    break;
                }
            }
        }
    	
        // 토큰 재발급 받아오기 
        JwtToken jwtToken = authService.reissue(tokenRequestDto);
        
        // 새로운 refreshToken 쿠키로 다시 세팅
        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                "refreshToken", // 쿠키명
                jwtToken.getRefreshToken() // 쿠키밸류값은 재발급 받아온 refreshToken 임 
        )
                .httpOnly(true) // HttpOnly Cookie 에 저장함으로써 JavaScript 로 접근이 불가하도록 만든다.
                .secure(false)  // localhost HTTP 이므로 false   https 이라면 .secure(true)
                .path("/")      // 쿠키가 사용되어질 범위 
                .maxAge(60 * 60 * 24 * 7)  // 쿠키수명 7일 
                .sameSite("None")   // 단일 도메인인 경우 .sameSite("Strict") , React 를 사용하여 프론트(localhost:3000)/백엔드(localhost:9082) 로 분리된 경우라면 .sameSite("None")  
                .build();

        // 생성된 쿠키 전송하기 
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        
    	// 토큰 재발급 해주기
        return ResponseEntity.ok(jwtToken); 
        // ResponseEntity.ok()를 사용하여 JSON 형태의 응답 반환.
        // HTTP 상태 코드 200 OK와 함께 데이터를 반환.
    }	    
    
}
