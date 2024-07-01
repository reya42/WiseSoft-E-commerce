package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Brand;
import com.wisesoft.project.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByCategoryId(Integer categoryId);
    Category findByCategoryName(String categoryName);
    Category findByCategoryLink(String categoryLink);
    List<Category> findAllByCategoryParentIdIsNull();
    List<Category> findByCategoryParentId(Integer parentCategoryId);
    List<Category> getAllByDisplayOnHomePageIsTrue();
}
