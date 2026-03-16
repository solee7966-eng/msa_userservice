package com.spring.app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.app.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> { // String 이라고 쓴 이유는 RefreshToken 엔티티 클래스에 @ID 로 사용되는 key 필드의 타입이 String 이기 때문이다. 

 /*
	RefreshTokenRepository 를 이용해 엔티티클래스명의 테이블(refreshtoken)에 SQL문장 없이 CRUD 작업이 되어진다. 
	JpaRepository 는 아래와 같은 내장된 쿼리메소드를 사용하여 CRUD 작업이 되어진다.
	
	- insert 작업 : save(엔티티 객체) 
	- select 작업 : findAll(), findById(키 타입)
	- update 작업 : save(엔티티 객체) 
	- delete 작업 : deleteById(키 타입) 
	
	특이하게도 insert와 update 작업에는 동일한 메서드인 save()를 이용하는데 
	그 이유는 JPA의 구현체가 메모리상(엔티티들을 관리해주는 Entity Manager 가 메모리에 올라감)에서 
	엔티티 객체(행)가 없다면 insert, 엔티티 객체(행)가 존재한다면 update 를 동작시키는 방식으로 실행되기 때문이다.
 */		
	
}
