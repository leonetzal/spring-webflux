package com.dev.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.webflux.app.models.dao.CategoryDao;
import com.dev.webflux.app.models.dao.ProductDao;
import com.dev.webflux.app.models.documents.Category;
import com.dev.webflux.app.models.documents.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private ProductDao productDao;
	
	@Autowired
	private CategoryDao categoryDao;
	
	@Override
	public Flux<Product> findAll() {
		return productDao.findAll();
	}

	@Override
	public Mono<Product> findById(String id) {
		return productDao.findById(id);
	}

	@Override
	public Mono<Product> save(Product product) {
		return productDao.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		return productDao.delete(product);
	}

	@Override
	public Flux<Product> findAllWithUpperCase() {
		return productDao.findAll().map(product -> {
			product.setName(product.getName().toUpperCase());
			return product;
		});
	}

	@Override
	public Flux<Product> findAllWithUpperCaseAndRepeat() {
		return findAllWithUpperCase().repeat(5000);
	}

	@Override
	public Flux<Category> findAllCategory() {
		return categoryDao.findAll();
	}

	@Override
	public Mono<Category> findCategoryById(String id) {
		return categoryDao.findById(id);
	}

	@Override
	public Mono<Category> saveCategory(Category category) {
		return categoryDao.save(category);
	}
}
