package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Image;
import com.wisesoft.project.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    public List<Image> findByProductId(Integer productId);
    public Image findByImageId(Integer imageId);
}
