package com.spring.app.front.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.front.repository.ProductImageRepository;
import com.spring.app.product.domain.ProductImageDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FrontService_imple implements FrontService {

	private final ProductImageRepository productImageRepository;
	
	
	@Override
	@Transactional(readOnly = true)
	public List<ProductImageDTO> getProductImage() {
		
	/*	
		List<ProductImageDTO> dtoList = new ArrayList<>();
		List<ProductImage> entityList = productImageRepository.findAll();
		
		for(ProductImage entity : entityList) {
			dtoList.add(ProductImageDTO.builder()
					.imgno(entity.getImgno())
					.productname(entity.getProductname())
					.imgfilename(entity.getImgfilename())
				    .build());
		}
		
		return dtoList;
	*/	
		// 또는
		
		// Spring Data JPA의 findAll() 은 절대로 null 반환하지 않고, 단지 빈 리스트 반환함. 
		// 그러므로 null 걱정은 할 필요가 없음.
		return productImageRepository.findAll()
	            .stream()
	            .map(entity -> ProductImageDTO.builder()
	                    .imgno(entity.getImgno())
	                    .productname(entity.getProductname())
	                    .imgfilename(entity.getImgfilename())
	                    .build())
	            .toList();  // Java 16 이상인 경우임. 만약에 Java 8 ~ 15라면 .toList(); 가 아니라 .collect(Collectors.toList()); 임. 
	}

}
