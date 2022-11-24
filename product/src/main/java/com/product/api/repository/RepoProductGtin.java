package com.product.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;

import com.product.api.dto.DtoProductGtin;

public interface RepoProductGtin extends JpaRepository<DtoProductGtin, Integer>{
	
	DtoProductGtin findByGtin(@PathVariable("gtin") String gtin);

}
