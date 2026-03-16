package com.spring.app.member.service;

import java.util.List;

import com.spring.app.member.domain.MemberDTO;
import com.spring.app.entity.Member;

public interface MemberService {

	// 아이디중복검사
	int memberidCheck(String memberid) throws Exception;

	// 회원가입 
	int insertMember(Member memberEntity) throws Exception;

	// 내정보
	Member getMember(String username) throws Exception;

	// authority 가 "ROLE_ADMIN" 을 제외한 모든 사용자 읽어오기
	List<MemberDTO> allMember() throws Exception;

}
