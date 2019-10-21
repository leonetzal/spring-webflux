package com.dev.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.dev.webflux.app.models.documents.Product;

public interface ProductDao extends ReactiveMongoRepository<Product, String>{

}
