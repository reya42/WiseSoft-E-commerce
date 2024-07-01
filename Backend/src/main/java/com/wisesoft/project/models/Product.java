package com.wisesoft.project.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Products")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "productBrandId")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "productCategoryId")
        private Category category;

    private String productName;
    private String productLink;
    private boolean isProductActive = true;
    @JoinColumn(name = "display_product_on_home_page")
    private boolean displayProductOnHomePage = false;
    private boolean isNew = true;
    private boolean heroProduct = false;
    private boolean isThereShippingFee = true;
    private Double productShippingFee = 65.0;
    private Double productDiscountedPrice = -1.0;
    private String productDescription;
    private String productSpecs;
    private String productWhatsInTheBox;
    private Double productPurchasePrice = 0.0;
    private Double productSellPrice;
    private int productStock = 0;
    private int standOutRowNum = 0;
    @Transient
    private List<Image> images = new ArrayList<>();
    private int numberOfRates = 0;
    private float averageRating = 0;
}