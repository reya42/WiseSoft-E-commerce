package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Brand;
import com.wisesoft.project.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Page<Brand> findAll(Pageable pageable);
    Brand findByBrandId(Integer brandId);
    List<Brand> findAllByBrandName(String brandName);
    List<Brand> findAllByBrandLink(String brandLink);
    Brand findByBrandName(String brandName);
    Brand findByBrandLink(String brandLink);
}