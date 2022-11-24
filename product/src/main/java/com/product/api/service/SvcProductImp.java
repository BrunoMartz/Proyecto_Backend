package com.product.api.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoCategoryId;
import com.product.api.dto.DtoProductGtin;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.entity.Product;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoCategoryId;
import com.product.api.repository.RepoProduct;
import com.product.api.repository.RepoProductGtin;
import com.product.api.repository.RepoProductList;
import com.product.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

	@Autowired
	RepoProduct repo;
	
	@Autowired
	RepoProductList repoProductList;
	
	@Autowired
	RepoProductGtin repoProductGtin;
	
	@Autowired
	RepoCategory repoCategory;
	
	@Autowired
	RepoCategoryId repoCategoryId;
	
	@Override
	public List<DtoProductList> getProducts(Integer category_id) {
		DtoCategoryId saved = repoCategoryId.findByCategoryId(category_id);
		
		if(saved == null)
			throw new ApiException(HttpStatus.BAD_REQUEST, "category does not exist");
		else {
			if(saved.getStatus() == 0)
				throw new ApiException(HttpStatus.BAD_REQUEST, "category is not activated");
		}	
		
		return repoProductList.findByCategory_id(category_id);
	}

	@Override
	public Product getProduct(String gtin) {
		Product product = repo.findByGtin(gtin);
		if (product != null) {
			product.setCategory(repoCategory.findByCategoryId(product.getCategory_id()));
			return product;
		}else
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
	}

	@Override
	public ApiResponse createProduct(Product in) {
		DtoProductGtin saved = repoProductGtin.findByGtin(in.getGtin());
		
		if(saved != null) {
			if(saved.getStatus() == 0) {
				repo.activateProduct(saved.getProduct_id());
				return new ApiResponse("product activated");
			}
		}
		
		try {
			DtoCategoryId categorySaved = repoCategoryId.findByCategoryId(in.getCategory_id());
			if(categorySaved.getStatus() == 0)
				throw new ApiException(HttpStatus.BAD_REQUEST, "category not found");
			in.setStatus(1);
			repo.save(in);
		}catch (DataIntegrityViolationException e) {
			if(e.getLocalizedMessage().contains("gtin"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
			if(e.getLocalizedMessage().contains("product"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
			if (e.contains(SQLIntegrityConstraintViolationException.class))
				throw new ApiException(HttpStatus.BAD_REQUEST, "category not found");
		}catch(NullPointerException npe) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "category not found");
		}
		
		return new ApiResponse("product created");
	}

	@Override
	public ApiResponse updateProduct(Product in, Integer id) {
		Integer updated = 0;
		try {
			Product productSaved = repo.findByProduct_id(id);
			if(productSaved.getStatus() == 0)
				throw new ApiException(HttpStatus.BAD_REQUEST, "product does not exist");
			
			DtoCategoryId categorySaved = repoCategoryId.findByCategoryId(in.getCategory_id());
			if(categorySaved.getStatus() == 0)
				throw new ApiException(HttpStatus.BAD_REQUEST, "category not found");
				
			updated = repo.updateProduct(id, in.getGtin(), in.getProduct(), in.getDescription(), in.getPrice(), in.getStock(), in.getCategory_id());
		}catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("gtin"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
			if (e.getLocalizedMessage().contains("product"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
			if (e.contains(SQLIntegrityConstraintViolationException.class))
				throw new ApiException(HttpStatus.BAD_REQUEST, "category not found");
		}catch(NullPointerException npe) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "category not found");
		}
		if(updated == 0)
			throw new ApiException(HttpStatus.BAD_REQUEST, "product does not exist");
		else
			return new ApiResponse("product updated");
	}

	@Override
	public ApiResponse updateProductStock(String gtin, Integer stock) {
		Product product = getProduct(gtin);
		if(stock > product.getStock())
			throw new ApiException(HttpStatus.BAD_REQUEST, "stock to update is invalid");
		
		repo.updateProductStock(gtin, product.getStock() - stock);
		return new ApiResponse("product stock updated");
	}	
	
	@Override
	public ApiResponse updateProductCategory(String gtin, DtoCategoryId in) {
		Category categorySaved = (Category) repoCategory.findByCategoryId(in.getCategory_id());
		
		if(categorySaved == null) 
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");

		Integer updated = 0;

		updated = repo.updateProductCategory(gtin, in.getCategory_id());

		if(updated == 0)
			throw new ApiException(HttpStatus.BAD_REQUEST, "product does not exist");
		else
			return new ApiResponse("product updated");
		}
	
	@Override
	public ApiResponse deleteProduct(Integer id) {
		if (repo.deleteProduct(id) > 0)
			return new ApiResponse("product removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "product cannot be deleted");
	}

}