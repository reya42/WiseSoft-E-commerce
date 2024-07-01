package com.wisesoft.project.services;

import com.wisesoft.project.models.Banner;
import com.wisesoft.project.models.Brand;
import com.wisesoft.project.models.Image;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.BannerRepository;
import com.wisesoft.project.repositories.BrandRepository;
import com.wisesoft.project.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@CrossOrigin
@RestController
public class BrandServices {
    @Autowired
    BrandRepository brandRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ExtraModules extraModules;

    @GetMapping("/brands")
    public List<Brand> getBrands(){
        return brandRepository.findAll();
    }

    @GetMapping("/brands/{brand_link}")
    public Brand getBrand(@PathVariable String brand_link){
        Brand thisBrand = brandRepository.findByBrandLink(brand_link);
        if (thisBrand != null){
            thisBrand.setBanners(
                    bannerRepository.findAllByBrandId(
                            thisBrand.getBrandId()
                    )
            );
            return thisBrand;
        }
        else {
            throw new RuntimeException("Cannot find a brand on this link: "+brand_link);
        }
    }

    @GetMapping("/brands/{brand_link}/banners")
    public List<Banner> listBrandBanners(@PathVariable String brand_link){
        Brand brand = brandRepository.findByBrandLink(brand_link);
        return bannerRepository.findAllByBrandId(brand.getBrandId());
    }

    @PostMapping("/create/brand")
    public String createBrand(@RequestBody Map<String,Object> brandMap){
        try {
            if (brandMap == null){
                throw new RuntimeException("Please send a request body... A brand needs brandName to be created.");
            }
            String brandName = brandMap.containsKey("brandName") ? (String) brandMap.get("brandName") : "";
            String brandLink = brandMap.containsKey("brandLink") ?
                    extraModules.sanitizeLink((String) brandMap.get("brandLink"))
                    :
                    extraModules.sanitizeLink(brandName);
            String brandDescription = brandMap.containsKey("brandDescription") ? (String) brandMap.get("brandDescription") : "";
            String brandDetail = brandMap.containsKey("brandDetail") ? (String) brandMap.get("brandDetail") : "";

            if (brandName.isEmpty()){
                throw new RuntimeException("You can't leave brandName empty");
            }
            // If it is not empty then check if there is already a brand with this name
            if (!brandRepository.findAllByBrandName(brandName).isEmpty()){
                throw new RuntimeException("There is already a brand with this name: "+brandName);
            }

            // Check if there is already a brand with this link
            if (!brandRepository.findAllByBrandLink(brandLink).isEmpty()){
                throw new RuntimeException("There is already a brand with this link: "+brandLink);
            }

            Brand brand = new Brand(brandName, brandLink, brandDescription,brandDetail);

            brandRepository.save(brand);
            return "Brand created successfully";
        }
        catch (Exception ex){
            throw new RuntimeException("An error occurred while creating brand: " +ex.getMessage());
        }
    }


    @PutMapping("/update/brand")
    public String updateBrand(@RequestBody Brand brand){
        try {
            if (brand == null){
                throw new RuntimeException("Please send a brand body to update it...");
            }
            // if there is brand and there are some null values in the data it contains set them to default or to empty
            brand.setBrandName(brand.getBrandName() == null ? "" : brand.getBrandName());
            brand.setBrandLink(brand.getBrandLink() == null ?
                    extraModules.sanitizeLink(brand.getBrandName())
                    :
                    extraModules.sanitizeLink(brand.getBrandLink())
                );
            brand.setBrandLink(brand.getBrandLink().isEmpty() ?
                    extraModules.sanitizeLink(brand.getBrandName())
                    :
                    extraModules.sanitizeLink(brand.getBrandLink())
            );
            brand.setBrandDescription(
                    brand.getBrandDescription() == null ? "" : brand.getBrandDescription()
            );
            brand.setBrandDetail(
                    brand.getBrandDetail() == null ? "" : brand.getBrandDetail()
            );

            // Find if there is really a product with this id
            Brand thisBrand = brandRepository.findByBrandId(brand.getBrandId());
            if (thisBrand == null) {
                throw new RuntimeException("There is no brand with this id: "+brand.getBrandId());
            }

            // If brand's name empty then throw error
            if(brand.getBrandName().isEmpty()){
                throw new RuntimeException("You can't leave brand's name empty");
            }

            // Check if there is already a brand with this brand's name then check if its this brand.
            if (brandRepository.findByBrandName(brand.getBrandName()) != null &&
                !brandRepository.findByBrandName(brand.getBrandName()).getBrandId()
                        .equals(brand.getBrandId())
            )
            {
                throw new RuntimeException("There is already a brand with this name: "+ brand.getBrandName());
            }

            // Check if there is already a brand with this brand's link then check if its this brand.
            if (!brandRepository.findAllByBrandLink(brand.getBrandLink()).isEmpty() &&
                !brandRepository.findByBrandLink(brand.getBrandLink()).getBrandId()
                        .equals(brand.getBrandId())
                )
            {
                throw new RuntimeException("There is already a brand with this link: "+brand.getBrandLink());
            }
            // We can update the brand if it's id, name and link are safe
            thisBrand.setBrandName(brand.getBrandName());
            thisBrand.setBrandLink(brand.getBrandLink());
            thisBrand.setBrandActive(brand.isBrandActive());
            thisBrand.setBrandDetail(brand.getBrandDetail());
            thisBrand.setBrandDescription(brand.getBrandDescription());
            brandRepository.save(thisBrand);
            return "Successfully updated this brand: "+brand.getBrandName();
    }
        catch (Exception ex){
            throw new RuntimeException("An error occurred while updating brand: " +ex.getMessage());
        }
    }

    @PutMapping("/update/brand/{brandLink}/change-image")
    public String uploadImage(
            @PathVariable String brandLink,
            @RequestParam MultipartFile image)
    {
        try{
            Brand brand = brandRepository.findByBrandLink(brandLink);
            if (brand == null) {
                throw new RuntimeException("There is no brand with this brand link: "+brandLink);
            }
            Image brandsExistingImage = brand.getBrandImage();
            String imageSaveDirectory = "brand-images/" + brand.getBrandLink() + "/";
            if (brandsExistingImage != null)
            {
                // Delete image from system
                extraModules.deleteImage(brandsExistingImage,imageSaveDirectory,true);
            }
            if (image != null){
                Image savedImage = extraModules.createImage(image,imageSaveDirectory,null);
                brand.setBrandImage(savedImage);
                brandRepository.save(brand);

                return "Image uploaded successfully: " + savedImage.getImageName();
            }
            else{
                brand.setBrandImage(null);
                brandRepository.save(brand);
                return "Image updated for this brand:" + brand.getBrandName() +"successfully to null.";
            }
        }
        catch (Exception ex){
            throw new RuntimeException("An error occurred while trying to upload an image to the brand. "+ex.getMessage());
        }
    }

    @Autowired
    BannerRepository bannerRepository;
    @Autowired
    BannerServices bannerController;

    @DeleteMapping("/delete/brand")
    public String deleteBrandWithId(@RequestParam Integer brandId){
        try {
            Brand thisBrand = brandRepository.findByBrandId(brandId);
            if (thisBrand == null) {
                throw new RuntimeException("There is no brand with this id: "+brandId);
            }
            else {
                if (thisBrand.getBrandImage() != null){
                    String directory = "C:/Users/recep/OneDrive/Documents/wisesoft_saved_images/brand-images/" + thisBrand.getBrandLink() + "/";
                    extraModules.deleteImage(thisBrand.getBrandImage(),directory,true);
                }
                // Delete all the banners that contain this brand's id in it.
                List<Banner> banners = bannerRepository.findAllByBrandId(brandId);
                for (Banner banner : banners){
                    bannerController.deleteBanner(banner.getBannerId());
                }

                brandRepository.delete(thisBrand);
                return "Brand deleted successfully";
            }
        }
        catch (Exception ex){
            throw new RuntimeException("An error occurred during delete process: ");
        }
    }
    @DeleteMapping("/delete/brands/{brandLink}")
    public String deleteBrand(@PathVariable String brandLink){
        try {
            Brand thisBrand = brandRepository.findByBrandLink(brandLink);
            if (thisBrand == null) {
                throw new RuntimeException("There is no brand on this link: "+brandLink);
            }
            else {
                if (thisBrand.getBrandImage() != null){
                    String directory = "C:/Users/recep/OneDrive/Documents/wisesoft_saved_images/brand-images/" + thisBrand.getBrandLink() + "/";
                    extraModules.deleteImage(thisBrand.getBrandImage(),directory,true);
                }
                brandRepository.delete(thisBrand);
                return "Brand deleted successfully";
            }
        }
        catch (Exception ex){
            throw new RuntimeException("An error occurred during delete process: ");
        }
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}