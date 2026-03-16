package com.spring.app.auth.service;

import com.spring.app.auth.domain.TokenRequestDTO;
import com.spring.app.entity.Member;
import com.spring.app.security.JwtToken;

public interface AuthService {
	
	// 로그인시 입력한 아이디와 비밀번호가 DB에 저장된 것과 일치할 경우 
	// JwtToken(토큰인증방식+액세스토큰+액세스토큰 만료기간+리프레쉬토큰) 발급 해주기
	public JwtToken login(String username, String password);
	
	// Access Token 이 만료된 경우 Refresh Token 을 가지고 JwtToken 재발급 해주기
	public JwtToken reissue(TokenRequestDTO tokenRequestDto);
	
	// memberid(아이디)를 가지고 권한까지 포함한 회원정보(entity) 가져오기
	public Member findByIdWithAuthorities(String memberid);
	
}
