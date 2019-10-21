package com.dev.webflux.rest.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dev.webflux.rest.api.models.documents.Category;
import com.dev.webflux.rest.api.models.documents.Product;
import com.dev.webflux.rest.api.models.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SpringBootWebfluxRestApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ProductService productService;

	@Value("${config.base.endpoint}")
	private String url;

	@Test
	public void listTest() {
		webTestClient.get().uri(url).accept(MediaType.APPLICATION_JSON_UTF8).exchange().expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8).expectBodyList(Product.class)
				.consumeWith(response -> {
					List<Product> products = response.getResponseBody();
					products.forEach(p -> {
						System.out.println(p.getName());
					});
					Assertions.assertThat(products.size() > 0).isTrue();
				});
		// .hasSize(10);
	}

	@Test
	public void viewTest() {
		Product product = productService.findProductByName("Acer Aspire 5 Slim").block();
		webTestClient.get().uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
				.accept(MediaType.APPLICATION_JSON_UTF8).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				/*
				 * .expectBody() .jsonPath("$.id").isNotEmpty()
				 * .jsonPath("$.name").isEqualTo("Acer Aspire 5 Slim");
				 */
				.expectBody(Product.class).consumeWith(response -> {
					Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getId().length() > 0).isTrue();
					Assertions.assertThat(p.getName()).isEqualTo("Acer Aspire 5 Slim");
				});
	}

	@Test
	public void createTest() {
		Category category = productService.findCategoryByName("Computacion").block();
		Product product = new Product("HP Omen 15' Gammer", 30050.43, category);
		webTestClient.post().uri(url).contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(product), Product.class).exchange()
				.expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8).expectBody()
				.jsonPath("$.product.id").isNotEmpty().jsonPath("$.product.name").isEqualTo("HP Omen 15' Gammer")
				.jsonPath("$.product.category.name").isEqualTo("Computacion");
	}

	@Test
	public void create2Test() {
		Category category = productService.findCategoryByName("Computacion").block();
		Product product = new Product("HP Omen 15' Gammer", 30050.43, category);
		webTestClient.post().uri(url).contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(product), Product.class).exchange()
				.expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
				}).consumeWith(response -> {
					Object o = response.getResponseBody().get("product");
					Product p = new ObjectMapper().convertValue(o, Product.class);
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getName()).isEqualTo("HP Omen 15' Gammer");
					Assertions.assertThat(p.getCategory().getName()).isNotEmpty();
				});
	}

	@Test
	public void editTest() {
		Product product = productService.findProductByName("Acer Aspire 5 Slim").block();
		Category category = productService.findCategoryByName("Computacion").block();
		Product productEdited = new Product("HP Omen 15' Gammer", 30050.43, category);
		webTestClient.put().uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
				.contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(productEdited), Product.class).exchange().expectStatus().isCreated().expectHeader()
				.contentType(MediaType.APPLICATION_JSON_UTF8).expectBody().jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("HP Omen 15' Gammer").jsonPath("$.category.name")
				.isEqualTo("Computacion");
	}

	@Test
	public void deleteTest() {
		Product product = productService.findProductByName("Lenovo IdeaPad 330s-14AST Laptop 14").block();
		webTestClient.delete().uri(url + "/{id}", Collections.singletonMap("id", product.getId())).exchange()
				.expectStatus().isNoContent().expectBody().isEmpty();
		webTestClient.get().uri(url + "/{id}", Collections.singletonMap("id", product.getId())).exchange()
				.expectStatus().isNotFound().expectBody().isEmpty();
	}
}
