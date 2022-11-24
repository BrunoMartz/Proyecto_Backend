package com.product.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Bruno Martinez Enriquez
 * Clase que representa a la entidad category.
 */

@Entity
@Table(name = "category")
public class Category{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private int category_id;
    
	@NotNull
	@Column(name = "category")
    private String category;

	@Column(name = "status")
	@Min(value = 0, message="status must be 0 or 1")
	@Max(value = 1, message="status must be 0 or 1")
	@JsonIgnore
    private int status;

	public Category() {}
	
	public Category(int category_id, String category) {
		this.category_id = category_id;
		this.category = category;
	}

	/** 
	 * @return el id de la categoría.
	 */
	public int getCategory_id() {
		return category_id;
	}
	
	/**
	 * @param category_id
	 */
	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}
	
	/**
	 * @return el nombre de la categoría.
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * @param category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Category [category_id=" + category_id + ", category=" + category + "]";
	}
    
}
