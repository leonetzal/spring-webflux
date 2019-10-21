package com.dev.webflux.rest.client.api.models.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;

import static org.springframework.http.MediaType.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.dev.webflux.rest.client.api.models.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService{
	
	@Autowired
	private WebClient.Builder webClient;

	@Override
	public Flux<Product> findAll() {
		return webClient.build().get().accept(APPLICATION_JSON_UTF8)
				.exchange()
				.flatMapMany(response -> response.bodyToFlux(Product.class));
	}

	@Override
	public Mono<Product> findById(String id) {
		return webClient.build().get()
				.uri("/{id}", Collections.singletonMap("id", id))
				.accept(APPLICATION_JSON_UTF8)
				.retrieve()
				.bodyToMono(Product.class);
	}

	@Override
	public Mono<Product> save(Product product) {
		return webClient.build().post()
		.accept(APPLICATION_JSON_UTF8)
		.contentType(APPLICATION_JSON_UTF8)
		.syncBody(product)
		.retrieve()
		.bodyToMono(Product.class);
	}

	@Override
	public Mono<Product> update(Product product, String id) {
		return webClient.build().put()
				.uri("/{id}", Collections.singletonMap("id", id))
				.accept(APPLICATION_JSON_UTF8)
				.contentType(APPLICATION_JSON_UTF8)
				.syncBody(product)
				.retrieve()
				.bodyToMono(Product.class);
	}

	@Override
	public Mono<Void> delete(String id) {
		return webClient.build().delete()
				.uri("/{id}", Collections.singletonMap("id", id))
				.retrieve()
				.bodyToMono(Void.class);
	}

	@Override
	public Mono<Product> upload(FilePart file, String id) {
		MultipartBodyBuilder parts = new MultipartBodyBuilder();
		parts.asyncPart("file", file.content(), DataBuffer.class).headers(h -> {
			h.setContentDispositionFormData("file", file.filename());
		});
		
		return webClient.build().post()
				.uri("/upload/{id}", Collections.singletonMap("id", id))
				.contentType(MULTIPART_FORM_DATA)
				.syncBody(parts.build())
				.retrieve()
				.bodyToMono(Product.class);
	}
}
