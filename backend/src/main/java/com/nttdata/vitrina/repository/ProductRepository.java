package com.nttdata.vitrina.repository;

import com.nttdata.vitrina.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    @Query("SELECT DISTINCT p.categoryGroup FROM Product p WHERE p.categoryGroup IS NOT NULL AND TRIM(p.categoryGroup) != '' ORDER BY p.categoryGroup ASC")
    List<String> findDistinctCategoryGroups();

    @Query("SELECT DISTINCT p.format FROM Product p WHERE p.format IS NOT NULL AND TRIM(p.format) != '' ORDER BY p.format ASC")
    List<String> findDistinctFormats();
}
