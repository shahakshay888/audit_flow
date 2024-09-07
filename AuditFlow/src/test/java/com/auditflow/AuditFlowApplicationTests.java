package com.auditflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.auditflow.dto.ProductRequestDto;
import com.auditflow.entity.Product;
import com.auditflow.repository.CategoryRepository;
import com.auditflow.repository.ProductRepository;
import com.auditflow.service.ProductService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AuditFlowApplicationTests {

	@Mock
    private ProductRepository productRepository;
	
	@Mock
	private CategoryRepository categoryRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private ProductService productService;

    @Test
    public void testCreateProductSuccess() {
    	Product product = new Product();
    	product.setName("Test Product");
        product.setPrice(100.00);

        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setName("Test Product");
        productRequestDto.setPrice(100.00);
        Product createdProduct = productService.createProduct(productRequestDto);

        assertNotNull(createdProduct);
        assertEquals("Test Product", createdProduct.getName());
        verify(productRepository, times(1)).save(createdProduct);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }
    
    @Test
    void testCreateProductFailure() {
        Product product = new Product();
        product.setName("Test Product");
        
        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setName("Test Product");
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Database Error"));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
        	productService.createProduct(productRequestDto);
        });

        assertEquals("Database Error", exception.getMessage());
        verify(productRepository, times(0)).save(product);
        verify(kafkaTemplate, times(0)).send(anyString(), anyString());
    }
    
    @Test
    void testUpdateProductSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Updated Product");
        product.setPrice(150.00);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setId(1L);
        productRequestDto.setName("Updated Product");
        productRequestDto.setPrice(150.00);

        Product updatedProduct = productService.updateProduct(1L, productRequestDto);

        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getName());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }
    
    @Test
    void testUpdateProductFailureNotFound() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Updated Product");

        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setId(1L);
        productRequestDto.setName("Updated Product");

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(1L, productRequestDto);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(0)).save(any(Product.class));
        verify(kafkaTemplate, times(0)).send(anyString(), anyString());
    }

    @Test
    public void testDeleteProductSuccess() {
    	Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(anyLong());

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).deleteById(1L);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }
    
    @Test
    void testDeleteProductFailureNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(1L);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(0)).deleteById(anyLong());
        verify(kafkaTemplate, times(0)).send(anyString(), anyString());
    }

}
