package com.auditflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auditflow.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
