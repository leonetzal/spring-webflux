package com.dev.webflux.rest.api.models.services;

import com.dev.webflux.rest.api.models.documents.Category;
import com.dev.webflux.rest.api.models.documents.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

	public Flux<Product> findAll();
	
	public Flux<Product> findAllWithUpperCase();
	
	public Flux<Product> findAllWithUpperCaseAndRepeat();
	
	public Mono<Product> findById(String id);
	
	public Mono<Product> save(Product product);
	
	public Mono<Void> delete(Product product);
	
	public Flux<Category> findAllCategory();
	
	public Mono<Category> findCategoryById(String id);
	
	public Mono<Category> saveCategory(Category category);
	
	public Mono<Product> findProductByName(String name);
	
	public Mono<Category> findCategoryByName(String name);
}
