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

// 로그인 폼태그에서 입력해준 아이디, 비밀번호를 가지는 DTO
public class MemberIdPasswdDTO { 
	private String memberid;  // 회원아이디 
	private String passwd;    // 비밀번호
}
