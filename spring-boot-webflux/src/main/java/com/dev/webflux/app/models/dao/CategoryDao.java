package com.dev.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dev.webflux.app.models.documents.Category;

public interface CategoryDao extends ReactiveMongoRepository<Category, String>{

}
