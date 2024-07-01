package com.wisesoft.project.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    private String categoryName;
    private String categoryLink;
    private Integer categoryParentId;
    private Boolean displayOnHomePage;
    @Transient
    private List<Category> childrenCategories;

    public Category(String categoryName, String categoryLink, Integer categoryParentId){
        this.categoryName = categoryName;
        this.categoryLink = categoryLink;
        this.categoryParentId = categoryParentId;
    }
    public Category(Integer categoryId, String categoryName, String categoryLink, Integer categoryParentId, Boolean displayOnHomePage)
    {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryLink = categoryLink;
        this.categoryParentId = categoryParentId;
        this.displayOnHomePage = displayOnHomePage;
    }



}