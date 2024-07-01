package com.wisesoft.project.services;

import com.wisesoft.project.models.Image;
import com.wisesoft.project.models.Product;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.BannerRepository;
import com.wisesoft.project.repositories.ImageRepository;
import com.wisesoft.project.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;


import java.util.List;

@CrossOrigin
@RestController
public class ImageServices {
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    BannerRepository bannerRepository;
    @Autowired
    ExtraModules extraModules;

    @GetMapping("/images")
    public List<Image> getAllImages(){
        return imageRepository.findAll();
    }

    @GetMapping("/images/{image_id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer image_id)
    {
        try {
            if (image_id==null)
            {
                throw new RuntimeException("Please send an image id to get an image from system.");
            }
            Image image =imageRepository.findByImageId(image_id);
            if (image==null)
            {
                throw new RuntimeException("There is no image stored in the database with this id: "+image_id);
            }
            // Image's path
            String imagePath = image.getImageDirectory();
            // Getting image as byte
            Path path = Paths.get(imagePath);
            byte[] imageAsByte = Files.readAllBytes(path);

            // return image as byte
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageAsByte);
        } catch (Exception ex) {
            throw new RuntimeException("An error occured while getting image: "+ex.getMessage());
        }
    }

    @GetMapping("/images/product")
    public List<Image> getProductImages(@RequestParam Integer productId){
        try {
            if (productRepository.findById(productId).isEmpty()){
                throw new RuntimeException("There is no product with this id: "+productId);
            }
            return imageRepository.findByProductId(productId);
        }
        catch (Exception ex){
            throw new RuntimeException("An error occurred while trying to get images of a product. "+ex.getMessage());
        }
    }

    @PostMapping("/upload/image")
    public String uploadImageToSystem(@RequestParam MultipartFile image){
        try {
            Image savedImage = extraModules.createImage(image, "",null);
            return savedImage.getImageName();
        }
        catch (Exception ex){
            throw new RuntimeException("Couldn't upload the image to file system: " + ex.getMessage());
        }
    }

        @PostMapping("/upload/image/product")
    public String uploadProductImage(@RequestParam MultipartFile image,
                                     @RequestParam  int productId){
        try {

            // Find the product that has a product_id value of productId variable
            Product thisProduct = productRepository.findByProductId(productId);
            if (thisProduct == null){
                throw new RuntimeException("There is no product with this id: "+productId);
            }
            String saveParentDirectory = "product-images/" + thisProduct.getProductLink() +"/";

            Image savedImage = extraModules.createImage(image,saveParentDirectory,thisProduct.getProductId());

            return "Image uploaded successfully: " + savedImage.getImageName();
        }
        catch (Exception ex){
            throw new RuntimeException("Couldn't upload the image to file system: " + ex.getMessage());
        }
    }

    @DeleteMapping("/delete/image")
    public String deleteImage(@RequestParam int imageId){
        try {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("There is no image with this id: "+ imageId));

            extraModules.deleteImage(image,"",false);

            return "Image deleted successfully from database";
        }
        catch (Exception ex){
            throw new RuntimeException("Couldn't delete the image: " + ex.getMessage());
        }
    }
    @DeleteMapping("/delete/image/product")
    public String deleteProductImage(
            @RequestParam Integer imageId
    ) {
        try{
            // Find the image if it exists
            Image image = imageRepository.findByImageId(imageId);
            if (image == null) throw new RuntimeException("There is no image with this id: "+imageId);
            // Get the id of the product which stored in this image
            Integer productId = image.getProductId();
            //Continue if productId is not null
            if (productId == null){
                throw new RuntimeException("There is no product image with this id: "+imageId+" (Image exist but it has no reference to any product)");
            }
            // Find the product if it exists
            Product thisProduct = productRepository.findByProductId(productId);
            if (thisProduct == null){
                throw new RuntimeException("There is no product stored in database with this id: "+productId);
            }

            // Delete image
            String imageParentDir = "product-images/" + thisProduct.getProductLink() +"/";
            extraModules.deleteImage(image,imageParentDir,false);

            return "Successfully deleted the image which is associated with this product: "+thisProduct.getProductName()+ " and has this image id: "+imageId;
        }catch (Exception ex){
            throw new RuntimeException("Error deleting the image with this image id: "+imageId+". "+ex.getMessage());
        }
    }
    @DeleteMapping("/delete/image/product/all")
    public String deleteProductsAllImages(@RequestParam Integer productId) {
        try{
            // Find the product if it exists
            Product thisProduct = productRepository.findByProductId(productId);
            if (thisProduct == null){
                throw new RuntimeException("There is no product stored in database with this id: "+productId);
            }
            // Find its images if it exists
            List<Image> images = imageRepository.findByProductId(productId);
            // Delete those images
            if (images == null) throw new RuntimeException("There is no image associated with this product id: "+ productId);
            for (Image image : images) {
                String imageParentDir = "product-images/" + thisProduct.getProductLink() +"/";
                extraModules.deleteImage(image,imageParentDir,true);
            }
            return "Successfully deleted all the images associated with this product id: "+productId;
        }catch (Exception ex){
            throw new RuntimeException("Error deleting all of the images associated with this product id: "+productId+". "+ex.getMessage());
        }
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}