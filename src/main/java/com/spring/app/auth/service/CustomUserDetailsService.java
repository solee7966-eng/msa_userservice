package com.spring.app.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.app.entity.Member;
import com.spring.app.auth.domain.CustomUserDetails;
import com.spring.app.member.domain.MemberDTO;
import com.spring.app.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	/* 
		UserDetailsService 는 Spring Security 에서 사용자 인증을 담당하는 인터페이스 이다.
	    그래서 Spring Security 는 기본적으로 UserDetailsService 를 구현한 클래스(지금은 CustomUserDetailsService)를 만들어서 사용자 인증을 처리해준다. 
	    이 클래스의 역할은 DB에서 username(아이디)으로 사용자를 조회하여 
	    사용자가 있으면 "username(아이디) + 비밀번호 + 권한" 을 담은 UserDetails 객체를 만들어서 반환하는 역할을 한다.
	    사용자 없으면 UsernameNotFoundException 발생시킨다.
	    
	!!! UserDetailsService 는 오로지 loadUserByUsername(String username) 이라는 메소드 1개만 가지고 있다. !!!

      ※ loadUserByUsername(String username) 메소드 ※
        -> 데이터베이스 또는 다른 저장소에서 사용자 정보를 로드하는 역할을 한다.
        -> 리턴타입은 UserDetails 객체를 반환하며, 존재하지 않는 사용자라면 UsernameNotFoundException을 발생시킨다.
        -> 리턴타입인 UserDetails 객체는 DB에서 읽어온 인증된 사용자(로그인 성공한 사용자)의 정보인 아이디, 비밀번호, 권한(Role), 계정 상태, 인증 토큰(JWT) 등을 담는 객체이다.
 	*/	
 	
	private final MemberRepository memberRepository;
  	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	
		// Spring Security 에서 인증된 사용자의 정보는 UserDetails 인터페이스를 구현하여 관리한다.
	    // UserDetails 인터페이스를 구현한 클래스는 로그인한 사용자의 정보 및 사용자의 권한(Role), 인증 토큰(JWT) 등을 포함할 수 있는 객체가 되어진다.
		// 이것을 위해 우리는 UserDetails 인터페이스를 구현한 com.spring.app.member.domain.CustomUserDetails 클래스를 만들어 두었다. 
		
		   System.out.println("~~~~~~~~~~~ 3단계");
			
	   Member member = memberRepository.findById(username)
	            .orElseThrow(() -> 
	                new UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다.")
	            );
	   /* 
	      findById() 는 Optional<Member> 를 리턴시켜주는데, 값이 있을 수도 있고 없을 수도 있다. 
	      리턴타입이 Optional<T> 이라서
	      .orElseThrow(() -> 
               익셉션객체생성
          ); 을 사용할 수 있다.
          
          그래서 // 값이 없을 경우라면 orElseThrow() 가 작동하는데 "해당하는 회원을 찾을 수 없습니다." 라는 
          메시지를 담은 UsernameNotFoundException 을 발생시킨다. 
       */

	    MemberDTO memberDto = member.toDTO();

	    return new CustomUserDetails(memberDto);
	    // !!! 중요 !!! //
	 	/* com.spring.app.auth.domain.CustomUserDetails 클래스에 스프링시큐리티로 사용되어질 로그인 되어진 사용자의 정보를 얻기 위한 메서드를 정의해둠!!
	 	   권한종류, 아이디, 비밀번호 등등등
	 	   권한종류는 getAuthorities() 이고, 아이디는 getUsername() 이며, 비밀번호는 getPassword() 이다. 
	    */
	    
	    /*
	       >> 동작 흐름 <<
         1. findById(username) → Optional<Member> 반환
         2. 값이 없으면 → UsernameNotFoundException 발생
         3. 값이 있으면 → Member 반환
         4. member.toDTO() 호출 
         5. new CustomUserDetails(memberDTO) 생성 후 리턴 
	    */	   	
	}

}
