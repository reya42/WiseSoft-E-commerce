package com.wisesoft.project.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banners")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bannerId;
    private Integer brandId;
    @ManyToOne
    @JoinColumn(name = "bannerImageId")
    private Image image;
    private Boolean displayOnHomePage = false;
    private String bannerRouteLink;
}
