package com.wisesoft.project.services;

import com.wisesoft.project.models.Banner;
import com.wisesoft.project.models.Brand;
import com.wisesoft.project.models.Image;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.BannerRepository;
import com.wisesoft.project.repositories.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
public class BannerServices {
    @Autowired
    BannerRepository bannerRepository;
    @Autowired
    ExtraModules extraModules;
    @Autowired
    BrandRepository brandRepository;

    @GetMapping("/banners")
    public List<Banner> getBanners(){
        return bannerRepository.findAll();
    }
    @GetMapping("/banners/home")
    public List<Banner> getHomePageBanners(){
        return bannerRepository.findByDisplayOnHomePageIsTrue();
    }
    @PostMapping("/create/banners")
    public String createBanner(
            @RequestParam MultipartFile image,
            @RequestParam(defaultValue = "") String bannerRouteLink,
            @RequestParam(defaultValue = "-1") Integer brandId,
            @RequestParam(defaultValue = "false") Boolean displayOnHomePage
    )
    {
        try {
            Banner banner = new Banner();
            // Do body contain image?
            if (image == null){
                throw new RuntimeException("Please send an image in the body. Remember to set key as image.");
            }
            // Do body contain bannerRouteLink?
            if (bannerRouteLink.isEmpty()){
                throw new RuntimeException("Please send a route link in the body. Remember to set key as bannerRouteLink.");
            }
            // If it does contain bannerRouteLink check if there is another banner containing that link
            else if (bannerRepository.findByBannerRouteLink(bannerRouteLink) != null){
                throw new RuntimeException("There is already a banner with this route link: "+bannerRouteLink);
            }
            String parentDir = "banners/";
            // Check if it contains brand id. If it does then check if a brand with that id exist
            if (brandId != -1){
                Brand brand = brandRepository.findByBrandId(brandId);
                if (brand == null){
                    throw new RuntimeException("There is no brand with this brand id: "+brandId);
                }
                else {
                    parentDir = parentDir + brand.getBrandLink() + "/";
                    banner.setBrandId(brandId);
                }
            }
            else {  // If there is no Brand id sent inside the body then set brand id to null
                banner.setBrandId(null);
            }

            // Clarify image as if there wasn't in the body we couldn't be in this step
            Image savedImage = extraModules.createImage(
                    image,
                    parentDir,
                    null
            );
            // Set Image
            banner.setImage(savedImage);
            // Set Route Link
            banner.setBannerRouteLink(bannerRouteLink);
            // If there was displayOnHomePage set displayOnHomePage to that variable if there wasn't then set it to false
            banner.setDisplayOnHomePage(displayOnHomePage);
            // Finally save banner
            Banner savedBanner = bannerRepository.save(banner);
            return "Banner successfully uploaded. Banner id: "+savedBanner.getBannerId();
        }
        catch (Exception e){
            throw new RuntimeException("An error occurred while creating a banner: "+e.getMessage());
        }
    }
    @PutMapping("/update/banners")
    public String updateBanner(
            @RequestParam(defaultValue = "-1") Integer bannerId,
            @RequestParam MultipartFile image,
            @RequestParam(defaultValue = "") String bannerRouteLink,
            @RequestParam(defaultValue = "-1") Integer brandId,
            @RequestParam(defaultValue = "false") Boolean displayOnHomePage
    )
    {
        try {
            if (bannerId == -1){
                throw new RuntimeException("Please send a bannerId to update that banner");
            } else if (bannerRepository.findByBannerId(bannerId) == null) {
                throw new RuntimeException("There is no banner with this id: "+bannerId);
            }

            Banner banner = bannerRepository.findByBannerId(bannerId);

            String newParentDir = "banners/";
            // Check if it contains brand id. If it does then check if a brand with that id exist
            if (brandId != -1){
                Brand brand = brandRepository.findByBrandId(brandId);
                if (brand == null){
                    throw new RuntimeException("There is no brand with this brand id: "+brandId);
                }
                else {
                    newParentDir = newParentDir + brand.getBrandLink() + "/";
                    banner.setBrandId(brandId);
                }
            }
            else {  // If there is no Brand id sent inside the body then set brand id to null
                banner.setBrandId(null);
            }

            // Do body contain image?
            if (image != null){
                // We will check if the banner had any brands before. Because we will check if the image is same with the brand's link
                String oldParentDir = "banners/";
                if (banner.getBrandId() != null){
                    Brand brand = brandRepository.findByBrandId(brandId);
                    oldParentDir = "banners/" + brand.getBrandLink() + "/";
                }
                // Check if these images are same
                String thisImagesDirWithOldParentDir = extraModules.save_Dir + oldParentDir + image.getOriginalFilename();
                String savedImagesDir = banner.getImage().getImageDirectory();
                // If they are not the same then delete the old one and update it with the new one
                if (!thisImagesDirWithOldParentDir.equals(savedImagesDir)){
                    // Temporarily set banner's image as null and save it to handle the foreign key
                    Image oldImage = banner.getImage();
                    banner.setImage(null);
                    bannerRepository.save(banner);

                    extraModules.deleteImage(
                            oldImage,
                            extraModules.save_Dir + oldParentDir,
                            true
                    );

                    Image newSavedImage = extraModules.createImage(
                            image,
                            newParentDir,
                            null
                    );

                    // Set Image
                    banner.setImage(newSavedImage);
                }
            }
            // If it doesn't contain image then we won't change it. Because we don't want any banner which doesn't containing an image

            // Do body contain bannerRouteLink?
            if (bannerRouteLink.isEmpty()){
                throw new RuntimeException("Please send a route link in the body. Remember to set key as bannerRouteLink.");
            }
            // If it does contain bannerRouteLink check if there is another banner containing that link other than this banner
            Banner bannerWithTheSameRouteLink = bannerRepository.findByBannerRouteLink(bannerRouteLink);

            if (bannerWithTheSameRouteLink != null){
                if (bannerWithTheSameRouteLink != banner){
                    throw new RuntimeException("There is already a banner other then this with this route link: "+bannerRouteLink);
                }
            }


            // Set Route Link
            banner.setBannerRouteLink(bannerRouteLink);
            // If there was displayOnHomePage set displayOnHomePage to that variable if there wasn't then set it to false
            banner.setDisplayOnHomePage(displayOnHomePage);
            // Finally save banner
            Banner savedBanner = bannerRepository.save(banner);
            return "Banner successfully uploaded. Banner id: "+savedBanner.getBannerId();
        }
        catch (Exception e){
            throw new RuntimeException("An error occurred while updating the banner: "+e.getMessage());
        }
    }
    @DeleteMapping("/delete/banners")
    public String deleteBanner(@RequestParam(defaultValue = "-1") Integer bannerId){
        try {
            if (bannerId == -1){
                throw new RuntimeException("Please send bannerId as parameter.");
            }
            else if (bannerRepository.findByBannerId(bannerId) == null){
                throw new RuntimeException("There is no banner with this id: "+bannerId);
            }
            else {
                Banner banner = bannerRepository.findByBannerId(bannerId);
                        // Delete image
                // Set null as always
                Image oldImage = banner.getImage();
                banner.setImage(null);
                bannerRepository.save(banner);

                String oldParentDir = "banners/";
                if (banner.getBrandId() != null){
                    Brand brand = brandRepository.findByBrandId(banner.getBrandId());
                    oldParentDir = "banners/" + brand.getBrandLink() + "/";
                }

                extraModules.deleteImage(
                        oldImage,
                        extraModules.save_Dir + oldParentDir,
                        true
                );

                bannerRepository.delete(banner);

                return "Successfully deleted the banner which were containing this bannerId: "+bannerId;
            }
        }
        catch (Exception e){
            throw new RuntimeException("An error occurred while deleting the banner: "+e.getMessage());
        }
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}
