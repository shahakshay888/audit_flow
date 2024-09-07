package com.auditflow.dto;

import lombok.Data;

@Data
public class ProductRequestDto {
	
	private Long id;
	
	private String name;
	   
	private double price;

    private String category;
}
