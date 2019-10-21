package com.dev.webflux.rest.api.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dev.webflux.rest.api.models.documents.Category;

import reactor.core.publisher.Mono;

public interface CategoryDao extends ReactiveMongoRepository<Category, String>{

	public Mono<Category> findByName(String name);
}
