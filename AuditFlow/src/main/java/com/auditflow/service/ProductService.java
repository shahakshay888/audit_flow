package com.auditflow.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.auditflow.dto.ProductRequestDto;
import com.auditflow.entity.Category;
import com.auditflow.entity.Product;
import com.auditflow.repository.CategoryRepository;
import com.auditflow.repository.ProductRepository;

@Service
public class ProductService {
	
	@Autowired
    private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
    
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public Product createProduct(ProductRequestDto productRequestDto) {
    	if (Objects.nonNull(productRequestDto)) {
    		Category category = getCategory(productRequestDto.getCategory());
    		Product product = new Product();
    		product.setCategory(category);
    		product.setPrice(productRequestDto.getPrice());
    		product.setName(productRequestDto.getName());
    		productRepository.save(product);
    		kafkaTemplate.send("product-topic", "Product Created: " + product.getId());
    		return product;
    	} else {
    		throw new RuntimeException("Request data not found");
    	}
    }

    public Product updateProduct(Long id, ProductRequestDto productRequestDto) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		Category category = getCategory(productRequestDto.getCategory());
        existingProduct.setName(productRequestDto.getName());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setCategory(category);
        Product updatedProduct = productRepository.save(existingProduct);
        kafkaTemplate.send("product-topic", "Product Updated: " + updatedProduct.getId());
        return updatedProduct;
    }

    public void deleteProduct(Long id) {
    	Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.deleteById(id);
        kafkaTemplate.send("product-topic", "Product Deleted: " + id);
    }
    
    public List<Product> getAllProducts(){
    	return productRepository.findAll();
    }
    
    Category getCategory(String categoryName) {
    	Optional<Category> categoryOptional = categoryRepository.findByNameIgnoreCase(categoryName);
		Category category = null;
		if (categoryOptional.isPresent()) {
			category = categoryOptional.get();
		} else {
			category = new Category();
			category.setName(categoryName);
			category = categoryRepository.save(category);
		}
		return category;
    }
}
