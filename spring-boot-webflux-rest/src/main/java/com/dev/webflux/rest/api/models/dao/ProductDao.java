package com.dev.webflux.rest.api.models.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dev.webflux.rest.api.models.documents.Product;

import reactor.core.publisher.Mono;

public interface ProductDao extends ReactiveMongoRepository<Product, String>{

	public Mono<Product> findByName(String name);
	
	@Query("{ 'name' : ?0 }")
	public Mono<Product> queryFindByName(String name);
}
