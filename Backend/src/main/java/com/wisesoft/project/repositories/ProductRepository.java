package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Brand;
import com.wisesoft.project.models.Category;
import com.wisesoft.project.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findByProductId(Integer productId);
    Product findByProductLink(String productLink);
    Product findByProductLinkAndIsProductActiveIsTrue(String productLink);
    Page<Product> getAllByDisplayProductOnHomePageIsTrueAndIsProductActiveIsTrue(Pageable pageable);
    Page<Product> getAllByHeroProductIsTrueAndIsProductActiveIsTrue(Pageable pageable);
    Page<Product> findProductsByBrandBrandLinkAndIsProductActiveIsTrue(Pageable pageable,String brandLink);
    Page<Product> findProductsByBrandBrandLinkAndDisplayProductOnHomePageIsTrueAndIsProductActiveIsTrue(Pageable pageable,String brandLink);
    @Override
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCaseAndIsProductActiveIsTrue(Pageable pageable,String productName);
    Page<Product> findByProductDescriptionContainingIgnoreCaseAndIsProductActiveIsTrue(Pageable pageable,String productDescription);
    List<Product> findAllByCategoryAndIsProductActiveIsTrue(Category category);
    @Query("SELECT p FROM Product p WHERE p.category IN :categories")
    Page<Product> findByCategories(List<Category> categories, Pageable pageable);
    @Query("SELECT p FROM Product p WHERE p.category IN :categories AND p.displayProductOnHomePage = TRUE")
    Page<Product> findByCategoriesAndDisplayProductOnHomePage(List<Category> categories, Pageable pageable);
}
