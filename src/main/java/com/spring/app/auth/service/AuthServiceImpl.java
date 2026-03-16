package com.spring.app.auth.service;

import static com.spring.app.entity.QAuthorities.authorities;
import static com.spring.app.entity.QMember.member;

import java.time.LocalDateTime;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.app.auth.domain.TokenRequestDTO;
import com.spring.app.auth.repository.RefreshTokenRepository;
import com.spring.app.entity.Member;
import com.spring.app.entity.RefreshToken;
import com.spring.app.security.JwtToken;
import com.spring.app.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// @Slf4j
public class AuthServiceImpl implements AuthService {

	// 의존객체 DI 생성자 주입
  	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	/*
	   AuthenticationManagerBuilder 는 Spring Security에서 인증(Authentication)을 담당하는 핵심 객체이다.
       또한 Spring Boot + Spring Security가 내부적으로 Bean으로 등록해 주는 객체이다.
       직접 @Bean으로 등록하지 않아도 DI가 가능하다.
       왜냐하면 Spring Security Auto Configuration 이 이미 Bean 등록을 해두었기 때문이다.
 	*/
 	private final JwtTokenProvider jwtTokenProvider;
 	private final RefreshTokenRepository refreshTokenRepository;
 	private final JPAQueryFactory jPAQueryFactory; // Query DSL 사용하기
 		
    // === 로그인시 입력한 아이디와 비밀번호가 DB에 저장된 것과 일치할 경우 
 	//     JwtToken(토큰인증방식+액세스토큰+액세스토큰 만료기간+리프레쉬토큰) 발급 받아 오기 ===
    // 만약에 @Transactional 을 사용하지 않으면 SQL명령을 수행해도 DB에는 반영이 안되어짐.
 	@Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = {Throwable.class}) 
	@Override
	public JwtToken login(String memberid, String passwd) {
		
		/*
		   UsernamePasswordAuthenticationToken 클래스는 Spring Security에서 사용자의 인증 정보(username, password)를 담는 객체이다.
           이 객체는 인증(authentication) 과 인가(authorization) 과정에서 사용된다.
           
           Spring Security에서 인증(Authentication)은 사용자의 신원을 확인하는 과정이다.
           이때, 인증 요청을 처리할 때 사용자의 아이디(username)와 비밀번호(password)를 담는 객체가 필요하다.
           바로 UsernamePasswordAuthenticationToken 클래스가 사용자의 아이디(username)와 비밀번호(password)를 담아주는 클래스이다. 

           UsernamePasswordAuthenticationToken 의 주요 생성자는 다음과 같다.
           -------------------------------------------------------------------------------------------------------------------------------------------------- 
            생성자	                                                                                 설명
           --------------------------------------------------------------------------------------------------------------------------------------------------  
            UsernamePasswordAuthenticationToken(Object principal, Object credentials)	             인증 전: username 과 password 를 담아 인증 요청을 보낼 때 사용
            
            UsernamePasswordAuthenticationToken(Object principal, Object credentials, 
                                                Collection<? extends GrantedAuthority> authorities)	 인증 후: 인증이 완료된 사용자의 정보와 권한 목록을 담을 때 사용
            ------------------------------------------------------------------------------------------------------------------------------------------------- 
		 */
		
 		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberid, passwd); 
 		// 1. 사용자가 입력한 아이디와 비밀번호를 담을 인증정보 객체인 UsernamePasswordAuthenticationToken 객체를 생성한다.
		//    생성되어진 authenticationToken 객체 에서 인증(로그인성공) 여부를 알려주는 Authenticated 값은 false 로 되어져 나온다.  
		
			System.out.println("~~~~~~~~~~~ 2단계");
			System.out.println("~~~~ 확인용 authenticationToken : " + authenticationToken);
		    // ~~~~ 확인용 authenticationToken : UsernamePasswordAuthenticationToken [Principal=seoyh, Credentials=[PROTECTED], Authenticated=false, Details=null, Granted Authorities=[]]  
		    // 현단계에서 인증(로그인성공) 여부를 알려주는 Authenticated 값은 false 로 되어 있다. 즉 인증이 안되어 있는 상태이다. 
	
			
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken); 	
		// 2. 위에서 생성한 사용자가 입력한 아이디와 비밀번호를 담은 UsernamePasswordAuthenticationToken authenticationToken 객체를 가지고 실제로 인증수행 하기
		//    즉, 생성된 UsernamePasswordAuthenticationToken authenticationToken 객체는 AuthenticationManager 의 authenticate() 메서드의 파라미터값으로 전달되어 실제 검증(인증)을 수행 한다.
		
		// authenticationManagerBuilder.getObject() 가 AuthenticationManager 클래스인 것이다. 
        
		// ----------------------------------------------------------------------------------------------------------------------------------------------------------- //
		//  !!!! >>>>>> 중요 <<<<<< !!!!
		//  1. 위에서 기술한 AuthenticationManager 클래스( authenticationManagerBuilder.getObject() )의 authenticate(authenticationToken) 메서드가 실행될 때  
		//     Spring Security 는 내부적으로 UserDetailsService 를 구현한 클래스인 com.spring.app.auth.service.CustomUserDetailsService 클래스의 
		//	   loadUserByUsername(String username) 메서드가 (데이터베이스 또는 다른 저장소에서 사용자 정보를 로드함) 자동 실행된다.!!!!!!!
		//     이때, 내부적으로 자동 실행 되어지는 loadUserByUsername(String username) 메서드의 파라미터인 String username 의 값은 authenticationToken 속에 들어있는 username 이 들어간다.  
		//
		//  2. 자동실행 되어진 loadUserByUsername(String username) 메서드의 리턴값인 UserDetails 를 가져와서,
		//	   UserDetails 속에 들어가 있는 사용자의 비밀번호(DB에서 조회한것)와 authenticationToken 속에 들어있는 password 가 같은지 검증을 한다.
		//
		//	3. 검증을 하여 비밀번호가 동일할 경우에만, Authentication authentication 객체를 생성하게 된다.  
		//	   이때, 생성된 Authentication authentication 객체의 Authenticated 값은 true 가 되어진다.
		//     이어서 생성된 Authentication authentication 객체는 자동적으로 SecurityContext 에 저장되어진다.
		//	   
		//     Authentication authentication 객체를 비유를 들어 설명하자면, 어떤 빌딩(어플리케이션)에 들어갈 수 있는 출입증에 해당 된다.
		//     이 출입증에는 username(authentication.getName()) 과 빌딩내에서 출입이 가능한 호실이 적힌 정보인 권한들(authentication.getAuthorities())이 적혀있다.
			
		/*
			   ※ SecurityContext 란?
	             - Spring Security 에서 현재 인증된 사용자(Authentication)를 저장하는 컨테이너(그릇) 역할을 하는 객체이다.
	               즉, 로그인한 사용자의 보안 컨텍스트(Security Context)를 유지하는 공간으로서, 현재 로그인한 사용자 정보를 가져오는 데 사용된다.
	          
	            SecurityContext 는 Authentication 객체를 저장한다.
	        
		        SecurityContext                         --> Authentication 객체를 저장하는 컨테이너
				└── Authentication  (인증 정보)           --> 현재 사용자의 인증 정보 객체 (UsernamePasswordAuthenticationToken 등)
		    		├── Principal   (사용자 정보)          --> 로그인한 사용자 정보 (보통 UserDetails 구현체)
		    		├── Credentials (자격 증명, 예: 비밀번호)
		    		├── Authorities (사용자 권한 목록)      --> 사용자의 권한 목록 (예: ROLE_USER, ROLE_ADMIN)
		*/	
		// ----------------------------------------------------------------------------------------------------------------------------------------------------------- // 
		  
		    // 3단계 까지 진행하고서 로그인시 아이디는 맞지만 암호가 틀린 경우라면 
		    // Spring Security 는 내부적으로 예외절이 발생시켜, 이후 5단계로는 넘어가지 않는다.
			System.out.println("~~~~~~~~~~~ 4단계"); 
			System.out.println("~~~~ 확인용 authentication : " + authentication);
			// ~~~~ 확인용 authentication : UsernamePasswordAuthenticationToken [Principal=org.springframework.security.core.userdetails.User [Username=seoyh, Password=[PROTECTED], Enabled=true, AccountNonExpired=true, CredentialsNonExpired=true, AccountNonLocked=true, Granted Authorities=[ROLE_ADMIN, ROLE_USER]], Credentials=[PROTECTED], Authenticated=true, Details=null, Granted Authorities=[ROLE_ADMIN, ROLE_USER]]
			// 인증 여부를 확인하는 Authenticated 값이 true 로 되어있다.	
			// Granted Authorities=[ROLE_ADMIN, ROLE_USER]] 로 되어있다.
			
			System.out.println("~~~~ 확인용 authentication.getName() : " + authentication.getName());
	        // ~~~~ 확인용 authentication.getName() : seoyh
			
			System.out.println("~~~~ 확인용 authentication.getAuthorities() : " + authentication.getAuthorities());
	        // ~~~~ 확인용 authentication.getAuthorities() : [ROLE_ADMIN, ROLE_USER]
			
			for (GrantedAuthority authority : authentication.getAuthorities()) {
	            System.out.println("~~~~ 확인용 Authority : " + authority.getAuthority());
	        }
			// ~~~~ 확인용 Authority : ROLE_ADMIN
			// ~~~~ 확인용 Authority : ROLE_USER
						
				
		
        // 3. 인증된 사용자정보와 권한을 기반으로 JWT 토큰 생성하기 
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
        
	        System.out.println("#### 확인용 jwtToken.getGrantType() : " + jwtToken.getGrantType());
	        // #### 확인용 jwtToken.getGrantType() : Bearer
	        
	        System.out.println("#### 확인용 jwtToken.getAccessToken() : " + jwtToken.getAccessToken()); // Access Token (액세스 토큰) 
	        // #### 확인용 jwtToken.getAccessToken() : eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzZW95aCIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTc0MjM0NDAzMH0.iKWjFCyPOBcisjWs9Pyn3RxKQvJnUhkBNmiNC7W9xXNvrNKn7xWQ4erJd8vnT3EE 
	        
	        System.out.println("#### 확인용 jwtToken.getAccessTokenExpiresIn() : " + jwtToken.getAccessTokenExpiresIn());
	        // #### 확인용 jwtToken.getAccessTokenExpiresIn() : 1742344030733
	        
	        System.out.println("#### 확인용 jwtToken.getRefreshToken() : " + jwtToken.getRefreshToken()); // Refresh Token (리프레시 토큰)
	        // #### 확인용 jwtToken.getRefreshToken() : eyJhbGciOiJIUzM4NCJ9.eyJleHAiOjE3NDI5NDcwMzB9.-_1OP8JGYUnplYl9k2t003RB6thaXAxH7T8KHp9u_kZ09OdJ8AhBnWxO8faErr-C 
	        
	        /*
	            Access Token (액세스 토큰) 
	            - 주요 목적: 사용자의 인증 및 권한 확인
	            - 특징:
	                   서버에서 클라이언트에게 발급하여 사용자가 API 요청을 보낼 때 인증 수단으로 사용
	                   주로 짧은 유효기간을 가짐 (예: 몇 분 ~ 몇 시간)
	                   유효한 동안 클라이언트가 API 요청을 보낼 때 헤더(예: Authorization: Bearer <access_token>)에 포함하여 서버에서 인증을 수행
	                   일반적으로 사용자 정보 및 권한을 포함
	                   저장장소는 메모리 또는 로컬스토리지
	                   
	            Refresh Token (리프레시 토큰)
	            - 주요 목적: 새로운 액세스 토큰을 발급받기 위한 용도
	            - 특징:
	                  길고 안전한 유효기간을 가짐 (예: 며칠 ~ 몇 주)
	                  클라이언트가 액세스 토큰이 만료되었을 때 새로운 액세스 토큰을 요청하는 데 사용
	                  일반적으로 액세스 토큰보다 보안이 강화되어야 하며, 별도의 저장소(예: DB 또는 HttpOnly 쿠키 등)에 저장
	                  단독으로는 API 접근 권한이 없음 (즉, 인증이 아닌 새 액세스 토큰 발급에만 사용됨) 
	                  저장장소는 DataBase 또는 HttpOnly 쿠기 
	           
	           [절차]       
	            1. 사용자가 로그인하면 서버는 "Access Token + Refresh Token" 을 발급해줌.
	            2. 클라이언트는 API 요청 시 Access Token 을 사용하여 인증
	            3. Access Token 이 만료되면, 클라이언트는 Refresh Token 을 서버로 보내 새로운 Access Token 을 요청
	            4. 서버는 Refresh Token 을 검증한 후 새로운 Access Token 을 발급
	            5. 사용자가 로그아웃하거나 Refresh Token이 만료되면 다시 로그인해야 함                        
	        */
        
        
        // 4. 리프레시토큰 이 저장될 RefreshToken 객체 생성하기
        RefreshToken refreshToken = RefreshToken.builder() 
            .memberid(authentication.getName())
            .rtValue(jwtToken.getRefreshToken())
            .build();
        
       // authentication.getName() 은 내부적으로 principal의 username을 반환하는 축약 메서드이다.
       // 하지만 보다 정확하게 사용자 정보를 다루려면 principal 객체를 직접 사용하는 것이 맞습니다.
        
        // DB 의 "tbl_refreshtoken" 테이블에 저장하기
        refreshTokenRepository.save(refreshToken);  // 없으면 insert, 있으면 update
      
        
        // 5. SecurityContext 에 인증정보인 Authentication authentication 객체를 저장하기
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        
        // 6. JWT 토큰 발급
        return jwtToken;  // API 요청에서는 JWT 인증 가능
	}

	
	// 만약에 @Transactional 을 사용하지 않으면 SQL명령을 수행해도 DB에는 반영이 안되어짐.
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = {Throwable.class}) 
    @Override
    public JwtToken reissue(TokenRequestDTO tokenRequestDto) {
        
    	// 1. Refresh Token 검증
        if (! jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 현재 로그인된 사용자 정보 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. username(아이디 또는 이메일)를 기반으로 DB에서 Refresh Token 값을 가져온다. 
        //    authentication.getName() 이 username(아이디) 값이다. 
        //    RefreshToken 은 엔티티 이다.
        RefreshToken refreshToken = refreshTokenRepository.findById(authentication.getName())
        		.orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));
              // orElseThrow() 을 사용하려면 refreshTokenRepository.findById(authentication.getName()) 메서드의 리턴타입이 Optional<RefreshToken> 이어야 한다. 
              // orElseThrow() 은 Optional 객체에 값이 없는 경우 예외를 발생시킨다. 
              
        
        // 4. 사용자가 보내온 Refresh Token 과 DB에 있던 Refresh Token 이 일치하는지 검사
        if (!refreshToken.getRtValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        JwtToken tokenDto = jwtTokenProvider.generateToken(authentication);

        // 6. 새로이 재발급받은 토큰에서 Refresh Token (리프레시 토큰) 값을 읽어서 새 RefreshToken 으로 만들고 이것을 DB에 저장하기
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 토큰 발급
        return tokenDto;
    }	

	
	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = {Throwable.class}) 
	public Member findByIdWithAuthorities(String memberid) {
	
		// 먼저, 마지막으로 로그인한 날짜를 현재 날짜로 갱신한 후 회원정보(Member 엔티티)를 리턴시키도록 한다.
		jPAQueryFactory
        .update(member)
        .set(member.lastLoginDate, LocalDateTime.now())  // 애플리케이션 서버 시간과 DB 서버 시간이 다를 경우 시간 불일치 가능성이 있음
        .where(member.memberid.eq(memberid))
        .execute(); 
		
		
		// >>> fetch join 으로 권한까지 함께 조회함.<<<
		// 권한이 없는 회원도 조회를 해야 안정적 이므로 
		// .join(member.authorities, authorities) 이 아니라 
		// .leftJoin(member.authorities, authorities) 으로 해야한다.
		// .join(member.authorities, authorities) 은 권한이 있는 회원만 조회를 해주는 것이다.
		// 그리고 .fetchJoin() 이 붙은 것은 JPA 에서 사용되어지는 문법으로서 연관관계에 있는 컬렉션 객체까지 읽어서 넣어주는 것을 말하는 것이다.
		// 여기서는 member 엔티티의 연관관계에 있는 컬렉션 객체는 List<Authorities> authorities 이다. 
		// 만약에 .fetchJoin() 을 뺀 .leftJoin(member.authorities, authorities) 만 해주면 LAZY 상태에 머물러 있어서 
		// 나중에 연관관계에 있는 컬렉션 객체가 필요할 경우 추가적인 select 가 발생하거나 트랜잭션 밖에서 호출하면 LazyInitializationException 이 발생할 수 있다. 
		// 그래서 일반적으로 로그인시 필요한 정보는 로그인한 사용자의 정보와 함께 권한까지 모두 읽어와야 하므로 .fetchJoin() 을 꼭 붙여준다. 
	    Member result = jPAQueryFactory
	        .selectFrom(member)
	        .leftJoin(member.authorities, authorities).fetchJoin()
	        .where(member.memberid.eq(memberid))
	        .fetchOne();  
	     // 행이 1개만 나오므로 .fetchOne();  
	     // 행이 1개이상 복수개가 나오는 경우라면 .fetch(); 
	    /*
			위의 것은
			
			select M.*, A.authority
			from tbl_member M left join tbl_authorities A
			on M.memberid = A.memberid
			where M.memberid = ?
			
			와 같은 SQL 을 생성한다. 
	     */
	    /*
	     Member result = jPAQueryFactory
	        .select(member)
	        .from(member)
	        .leftJoin(member.authorities, authorities).fetchJoin()
	        .where(member.memberid.eq(memberid))
	        .fetchOne();
	        
	      .selectFrom(member) 
	        와
	      .select(member)
	      .from(member)
	        은 같은 것이다.    
	    */
	    

	    if (result == null) {
	        throw new RuntimeException("등록된 회원이 아닙니다.");
	    }

	    return result;
		// 값이 있을 경우 Member 를 리턴시켜주고 끝내지만
		// 값이 없을 경우라면 "등록된 회원이 아닙니다." 라는 메시지를 담은 RuntimeException 을 발생시킨다. 
	}


}
