package com.dev.webflux.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.webflux.app.models.dao.ProductDao;
import com.dev.webflux.app.models.documents.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/products")
public class ProductRestController {

	@Autowired
	private ProductDao productDao;

	private static final Logger log = LoggerFactory.getLogger(ProductController.class);

	@GetMapping
	public Flux<Product> index() {
		Flux<Product> products = productDao.findAll().map(product -> {
			product.setName(product.getName().toUpperCase());
			return product;
		}).doOnNext(prod -> log.info(prod.getName()));
		return products;
	}

	@GetMapping("/{id}")
	public Mono<Product> show(@PathVariable String id) {
		Flux<Product> products = productDao.findAll();
		Mono<Product> product = products.filter(p -> p.getId().equals(id)).next()
				.doOnNext(prod -> log.info(prod.getName()));
		return product;
	}
}
