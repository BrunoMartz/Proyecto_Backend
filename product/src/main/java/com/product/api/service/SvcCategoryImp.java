package com.product.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoCategoryId;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoCategoryId;
import com.product.api.repository.RepoProductList;
import com.product.exception.ApiException;

@Service
public class SvcCategoryImp implements SvcCategory {
	
	@Autowired
	RepoCategory repo;
	
	@Autowired
	RepoProductList repoProductList;
	
	@Autowired
	RepoCategoryId repoId;
	
	@Override
	public List<Category> getCategories() {
		return repo.findByStatus(1);
	}

	@Override
	public Category getCategory(Integer category_id) {
		Category category = repo.findByCategoryId(category_id);
		
		if (category == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "category does not exists");
		else
			return category;
	}

	@Override
	public ApiResponse createCategory(Category category) {
		Category categorySaved = (Category) repo.findByCategory(category.getCategory());
		
		if(categorySaved != null) {
			if(categorySaved.getStatus() == 0) { 
				repo.activateCategory(categorySaved.getCategory_id());
				return new ApiResponse("category has been activated");
			}
			else
				throw new ApiException(HttpStatus.BAD_REQUEST,"category already exists");
		}
		
		repo.createCategory(category.getCategory());
			
		return new ApiResponse ("category created");
	}

	@Override
	public ApiResponse updateCategory(Integer category_id, Category category) {
		DtoCategoryId categoryIdSaved = repoId.findByCategoryId(category_id);
		
		if(categoryIdSaved == null) 
			throw new ApiException(HttpStatus.NOT_FOUND, "category does not exists");
		else {
			if(categoryIdSaved.getStatus() == 0) 
				throw new ApiException(HttpStatus.BAD_REQUEST, "category is not active");
			else {
				Category categorySaved = repo.findByCategory(category.getCategory());
				
				if(categorySaved != null)
					throw new ApiException(HttpStatus.BAD_REQUEST, "category already exists");
				
				repo.updateCategory(category_id, category.getCategory());
				return new ApiResponse("category updated");
			}
		}
	}

	@Override
	public ApiResponse deleteCategory(Integer category_id) {
		Category categorySaved = (Category) repo.findByCategoryId(category_id);
		
		if(categorySaved == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "category does not exists");
		else {
			List<DtoProductList> list = repoProductList.findByCategory_id(categorySaved.getCategory_id());
			
			if(list.size() != 0) 
				throw new ApiException(HttpStatus.BAD_REQUEST, "category does not exists, category cannot be removed if it has products");
				
			repo.deleteById(category_id);
			return new ApiResponse("category removed");
		}
	}

}
