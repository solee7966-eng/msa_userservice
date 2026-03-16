package com.spring.app.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 로그인 되어진 이후에 사용되어질 사용자 DTO
// 즉, JWT 으로 사용되어지는 객체이다.
public class LoginUserDTO { 
	private String memberid;             // 회원아이디
	private String name;                 // 회원명
	
	private String grantType;            // 인증방식(예: Bearer)
	private String accessToken;          // 액세스토큰
	private Long   accessTokenExpiresIn; // 액세스토큰 만료기간
	private String refreshToken;	     // 리프레쉬토큰
}

