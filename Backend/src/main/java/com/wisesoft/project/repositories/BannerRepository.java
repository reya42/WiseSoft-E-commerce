package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Integer> {
    List<Banner> findByDisplayOnHomePageIsTrue();
    Banner findByBannerRouteLink(String bannerRouteLink);
    Banner findByBannerId(Integer bannerId);
    List<Banner> findAllByBrandId(Integer brandId);
    Banner findByImageImageId(Integer imageId);
}