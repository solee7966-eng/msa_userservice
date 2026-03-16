package com.spring.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_product_image")
@Data                   // lombok 에서 사용하는 @Data 어노테이션은 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 를 모두 합쳐놓은 종합선물세트인 것이다.
@AllArgsConstructor     // 모든 필드 값을 파라미터로 받는 생성자를 만들어주는 것
@NoArgsConstructor      // 파라미터가 없는 기본생성자를 만들어주는 것
@Builder                // 생성자 대신, 필요한 값만 선택해서 체이닝 방식으로 객체를 만들 수 있게 해주는 것.
public class ProductImage { 
  
	@Id
	@Column(name="imgno", nullable=false)
	private int imgno;  // Primary Key 로 사용되어지는 @Id 컬럼에는 name 속성을 꼭 주도록 하자.      
	
	@Column(nullable=false, length=40)
	private String productname;
	
	@Column(nullable=false, length=100)
	private String imgfilename;   
	
}
