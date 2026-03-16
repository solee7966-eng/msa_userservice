package com.spring.app.member.service;

import static com.spring.app.entity.QAuthorities.authorities;
import static com.spring.app.entity.QMember.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.app.entity.Authorities;
import com.spring.app.entity.Member;
import com.spring.app.member.domain.MemberDTO;
import com.spring.app.member.repository.AuthoritiesRepository;
import com.spring.app.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService_imple implements MemberService {

	// === Query DSL(Domain Specific Language) 를 사용하여 구하기 === //
	
	/*
	   QueryDSL은 Java 기반의 오픈 소스 프레임워크로, JPQL (Java Persistence Query Language)을 Java 코드로 작성할 수 있도록 해준다. 
	   즉, SQL이나 JPQL과 같은 데이터베이스 쿼리를 자바 코드로 작성하여, 타입 안정성과 코드 가독성을 높여준다.  
	   
		1. 특정 조건으로 조회하기 (WHERE)
		
		같다:       .eq()
		크거나 같다: .goe()
		작거나 같다: .loe()
		크다:      .gt()
		작다:      .lt()
		포함한다 (LIKE %값%): .contains()
		시작한다 (LIKE 값%):  .startsWith()
		끝난다 (LIKE %값):   .endsWith()
		널(null)이다: .isNull()
		널(null)이 아니다: .isNotNull()
		목록 안에 있다 (IN): .in()
		
		
		2. 정렬하기 (ORDER BY)
		
		오름차순: .asc()
		내림차순: .desc()
		
		
		3. **일부만 가져오기 (LIMIT/OFFSET 또는 페이징)
		
		시작점 지정:     .offset()
		가져올 개수 지정: .limit()
		
		
		4. **그룹화 및 집계 함수 (GROUP BY, COUNT, SUM, AVG 등)
		
		그룹화:    .groupBy()
		개수 세기: .count()
		합계:     .sum()
		평균:     .avg()
		최대값:    .max()
		최소값:    .min()
		
		
		5. **조건이 여러 개일 때 (AND, OR)
		
		AND 조건: .and()
		OR 조건:  .or()
	*/	
	
	private final MemberRepository memberRepository;
	private final AuthoritiesRepository authoritiesRepository;
	
	private final JPAQueryFactory jPAQueryFactory;
	
	
	// === 아이디중복검사 === //
	@Override
	public int memberidCheck(String memberid) {
		long n =  jPAQueryFactory
				.select(member.count())  // count 컬럼
                .from(member)
                .where(member.memberid.eq(memberid))
                .fetchOne();
		
		return (int)n;
	}

	
	// === 회원가입 === //
	@Override
 // @Transactional // JPA 에서는 DML 작업시 필수임. 성공시 commit 해주고, 실패시 roolback 해줌. 
	               // JPA 에서는 오로지 1개의 DML 작업이 있는 경우에도 반드시 @Transactional 을 적어주어아 한다.!!                
	               // 만약에 @Transactional 을 사용하지 않으면 SQL명령을 수행해도 DB에는 반영이 안되어진다. 
                   // 그리고 jakarta.persistence.TransactionRequiredException: Executing an update/delete query 오류가 발생함.
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = {Throwable.class}) 
	public int insertMember(Member memberEntity) throws Exception {
		
		int n = 0;
		
		try {
			// 회원정보 저장하기
			Member savedMember = memberRepository.save(memberEntity); // save(엔티티 객체)
		
			// 권한정보 저장을 위해 Authorities 엔티티 객체 생성하기
 			Authorities authorities = Authorities.builder()
			        .member(savedMember)    // 위에서 save()의 반환값을 넣어주어야만 Foreign Key 관계가 맺어진다.
			        .authority("ROLE_USER") // ROLE_ 를 꼭 붙여 주어야 한다.!!!
			        .build();

 		    // 권한정보 저장하기
			authoritiesRepository.save(authorities); // save(엔티티 객체)
			
			n = 1;
			
		} catch(Exception e) {
			throw e;
		}
		
		return n;
	}


	// === 내정보 === //
	@Override
	public Member getMember(String memberid) throws Exception {
		
		Member result = jPAQueryFactory
		        .selectFrom(member)
		        .leftJoin(member.authorities, authorities).fetchJoin()
		        .where(member.memberid.eq(memberid))
		        .fetchOne();
		/*
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
	}


	// === authority 가 "ROLE_ADMIN" 을 제외한 모든 사용자 읽어오기 ===
	@Override
	public List<MemberDTO> allMember() throws Exception {
		
		List<MemberDTO> result = jPAQueryFactory
			    .select(Projections.constructor(MemberDTO.class, // MemberDTO 로 변환. 반드시 com.spring.app.domain.MemberDTO 에서 생성자 public MemberDTO(String username, String memberName, LocalDateTime registerday, LocalDateTime lastlogindate) 을 만들어야 함.
			        member.memberid,
			        member.name,
			        member.registerday,    // QueryDSL은 DB의 to_char(registerday, 'yyyy-mm-dd hh24:mi:ss') 같은 함수의 직접 호출은 지원하지 않는다. 
			        member.lastLoginDate)  // 그래서 일반적으로 DTO 변환 후 Java에서 포맷팅 한다.
			    )
			    .from(member)
			    .where(
			        member.memberid.notIn(  // SQL의 NOT IN (SELECT ...) 구문은 member.memberid.notIn(JPAExpressions.select(...)) 형태로 구현함. 
			            JPAExpressions
			                .select(authorities.member.memberid)
			                .from(authorities)
			                .where(authorities.authority.eq("ROLE_ADMIN"))
			        )
			    )
			    .orderBy(member.registerday.desc())
			    .fetch();
		
		return result;
		/*
		  [{"memberid":"eomjh","passwd":null,"enabled":null,"name":"엄정화","registerday":"2026-03-02","lastLoginDate":null,"authorities":null},
		   {"memberid":"seoyh","passwd":null,"enabled":null,"name":"서영학","registerday":"2026-03-01","lastLoginDate":"2026-03-02T20:23:28","authorities":null}]
		*/
		
	}

}
