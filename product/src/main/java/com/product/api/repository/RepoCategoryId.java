package com.product.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.product.api.dto.DtoCategoryId;

public interface RepoCategoryId extends JpaRepository<DtoCategoryId, Integer>{
	
	@Query(value = "SELECT * FROM category WHERE category_id = :category_id", nativeQuery = true)
	DtoCategoryId findByCategoryId(@Param("category_id") Integer category_id);

}
