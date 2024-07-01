package com.wisesoft.project.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "brands")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer brandId;
    private String brandName;
    private String brandLink;
    private String brandDescription;
    private String brandDetail;
    @OneToOne
    @JoinColumn(name = "brandImageId")
    private Image brandImage = null;
    private boolean isBrandActive = true;
    @Transient
    List<Banner> banners;
    public Brand(String brandName) {
        this.brandName = brandName;
    }

    public Brand(String brandName, String brandLink, String brandDescription,String brandDetail) {
        this.brandName = brandName;
        this.brandLink = brandLink;
        this.brandDescription = brandDescription;
        this.brandDetail = brandDetail;
    }


}