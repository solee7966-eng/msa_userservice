package com.spring.app.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter             
@Setter             
@ToString           
@NoArgsConstructor  
@AllArgsConstructor 
@Builder  

// 유효기간이 지난 accessToken 일 경우 refreshToken 을 가지고 
// JwtToken 을 재발급 받아오기 위한 DTO 
public class TokenRequestDTO {
	private String accessToken;
	private String refreshToken;
}
