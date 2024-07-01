package com.wisesoft.project.services;

import com.wisesoft.project.models.*;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.io.File;
import java.util.*;

@CrossOrigin
@RestController
public class ProductServices {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    BrandRepository brandRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ExtraModules extraModules;

    //          All products' page
    @GetMapping("/products")
    public ResponseEntity<Object> getAllProductsPageWithImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "standOutRowNum") String descending
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(descending).descending());
        Page<Product> products = productRepository.findAll(pageable);
        for (Product product : products.getContent()) {
            List<Image> images = imageRepository.findByProductId(product.getProductId());
            product.setImages(images);
        }
        return ResponseEntity.ok(products);
    }
    @GetMapping("/products/all")
    public ResponseEntity<Object> getAllProductsWithImages(
            @RequestParam(defaultValue = "0") int page) {

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            List<Image> images = imageRepository.findByProductId(product.getProductId());
            product.setImages(images);
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/hero_products")
    public ResponseEntity<Object> getHeroProducts(@RequestParam(defaultValue = "0") int page)
    {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("standOutRowNum").descending());
        Page<Product> products = productRepository.getAllByHeroProductIsTrueAndIsProductActiveIsTrue(pageable);
        for (Product product : products.getContent()) {
            List<Image> images = imageRepository.findByProductId(product.getProductId());
            product.setImages(images);
        }
        return ResponseEntity.ok(products);
    }
    //          Single product's get for admin
    @GetMapping("/products/admin/{product_link}")
    public ResponseEntity<Object> getProduct(@PathVariable("product_link") String link){

        Product product = productRepository.findByProductLink(link);

        if (product == null) throw new RuntimeException("There is no product in the database that contain this link: "+link);

        List<Image> images = imageRepository.findByProductId(product.getProductId());
        product.setImages(images);

        return ResponseEntity.ok(product);
    }
        @GetMapping("/products/{product_link}")
    public ResponseEntity<Object> getProductDetailed(@PathVariable("product_link") String link){

        Product product = productRepository.findByProductLinkAndIsProductActiveIsTrue(link);

        if (product == null) throw new RuntimeException("There is no product in the database that contain this link: "+link);

        List<Image> images = imageRepository.findByProductId(product.getProductId());
        product.setImages(images);

        return ResponseEntity.ok(product);
    }

    //          Page of products from this single brand
    @GetMapping("/brands/{brand_link}/products")
    public ResponseEntity<Object> getAllByBrand
            (   @PathVariable("brand_link") String brand_link,
                @RequestParam(defaultValue = "0") int page
            ){
        Pageable pageable = PageRequest.of(page, 16, Sort.by("standOutRowNum").descending());

        if (brandRepository.findByBrandLink(brand_link)!=null){
            Page<Product> products = productRepository.findProductsByBrandBrandLinkAndIsProductActiveIsTrue(pageable,brand_link);

            for (Product product : products.getContent()) {
                List<Image> images = imageRepository.findByProductId(product.getProductId());
                product.setImages(images);
            }
            return ResponseEntity.ok(products);
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"There is no brand in database with this link: "+brand_link+"\"}");
        }
    }

    //          Page of home products from this single brand
    @GetMapping("/brands/{brand_link}/home")
    public ResponseEntity<Object> getAllByBrandAndDisplayOnHome
    (   @PathVariable("brand_link") String brand_link,
        @RequestParam(defaultValue = "0") int page
    ){
            Pageable pageable = PageRequest.of(page, 4, Sort.by("standOutRowNum").descending());

        if (brandRepository.findByBrandLink(brand_link)!=null){
            Page<Product> products = productRepository.findProductsByBrandBrandLinkAndDisplayProductOnHomePageIsTrueAndIsProductActiveIsTrue(pageable,brand_link);

            for (Product product : products.getContent()) {
                List<Image> images = imageRepository.findByProductId(product.getProductId());
                product.setImages(images);
            }
            return ResponseEntity.ok(products);
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"There is no brand in database with this link: "+brand_link+"\"}");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String searchTerm,
                                @RequestParam Boolean extendedSearch,
                                @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 16, Sort.by("standOutRowNum").descending());

        Page<Product> nameResults = productRepository.findByProductNameContainingIgnoreCaseAndIsProductActiveIsTrue(pageable, searchTerm);
        if (extendedSearch)
        {   // Search by name
            for (Product product : nameResults.getContent()) {
                List<Image> images = imageRepository.findByProductId(product.getProductId());
                product.setImages(images);
            }
            // Search by description
            Page<Product> descriptionResults = productRepository.findByProductDescriptionContainingIgnoreCaseAndIsProductActiveIsTrue(pageable, searchTerm);
            for (Product product : descriptionResults.getContent()) {
                List<Image> images = imageRepository.findByProductId(product.getProductId());
                product.setImages(images);
            } // Combine the results
            List<Product> combinedResults = Stream.concat(nameResults.getContent().stream(), descriptionResults.getContent().stream())
                    .distinct()
                    .collect(Collectors.toList());
            // List them as pages again and return it.
            return ResponseEntity.ok(new PageImpl<>(combinedResults, pageable, combinedResults.size()));
        }
        else { // If it wasn't extended search, then only search by name
            for (Product product : nameResults.getContent()) {
                List<Image> images = imageRepository.findByProductId(product.getProductId());
                product.setImages(images);
            }
            return ResponseEntity.ok(nameResults);
        }
    }


    //          Create module
    @PostMapping("/create/product")
    public ResponseEntity<Object> createProduct(@RequestBody Map<String, Object> productMap) {
        try {
            //      Defining containing variables
            // Brand
            Integer thisProductBrandId = (Integer) productMap.get("brandId");
            Brand thisProductBrand = null;
            // Category
            Integer thisProductCategoryId = (Integer) productMap.get("categoryId");
            Category thisProductCategory = null;
            // Name
            String thisProductName = productMap.containsKey("productName") ? (String) productMap.get("productName") : "";
            // Link
            String thisProductLink = productMap.containsKey("productLink") ? (String) productMap.get("productLink") : "";
            // Is Active
            boolean thisIsActive = productMap.containsKey("productActive") ? (Boolean) productMap.get("productActive") : true;
            // Display On Home Page
            boolean thisDisplayProductOnHomePage = productMap.containsKey("displayProductOnHomePage") ? (Boolean) productMap.get("displayProductOnHomePage") : false;
            // Is New
            boolean isNew = productMap.containsKey("new") ? (Boolean) productMap.get("new") : true;
            // Is Hero Product
            boolean isHeroProduct = productMap.containsKey("heroProduct") ? (Boolean) productMap.get("heroProduct") : false;
            // Is There Shipping Fee
            boolean isThereShippingFee = productMap.containsKey("thereShippingFee") ? (Boolean) productMap.get("thereShippingFee") : true;
            // Shipping Fee
            Double thisProductShippingFee = 65.0;
            try
            {
                thisProductShippingFee = productMap.containsKey("productShippingFee") ? (Double) productMap.get("productShippingFee") : 65.0;
            }
            catch (Exception ex)
            {
                thisProductShippingFee = productMap.containsKey("productShippingFee") ? ((Integer) productMap.get("productShippingFee")).doubleValue() : 65.0;
            }
            // Discounted price
            Double thisProductDiscountedPrice = -1.0;
            try
            {
                thisProductDiscountedPrice = productMap.containsKey("productDiscountedPrice") ? (Double) productMap.get("productDiscountedPrice") : 65.0;
            }
            catch (Exception ex)
            {
                thisProductDiscountedPrice = productMap.containsKey("productDiscountedPrice") ? ((Integer) productMap.get("productDiscountedPrice")).doubleValue() : 65;
            }
            // Description
            String thisProductDescription = productMap.containsKey("productDescription") ? (String) productMap.get("productDescription") : "";
            // Specs
            String thisProductSpecs = productMap.containsKey("productSpecs") ? (String) productMap.get("productSpecs") : "";
            // What's in the box
            String thisProductWhatsInTheBox = productMap.containsKey("productWhatsInTheBox") ? (String) productMap.get("productWhatsInTheBox") : "";
            // Purchase Price
            Double thisProductPurchasePrice = 0.0;
            try
            {
                thisProductPurchasePrice = productMap.containsKey("productPurchasePrice") ? (Double) productMap.get("productPurchasePrice") : 0.0;
            }
            catch (Exception ex)
            {
                thisProductPurchasePrice = productMap.containsKey("productPurchasePrice") ? ((Integer) productMap.get("productPurchasePrice")).doubleValue() : 0.0;
            }
            // Sell Price
            Double thisProductSellPrice = 0.0;
            try
            {
                thisProductSellPrice = productMap.containsKey("productSellPrice") ? (Double) productMap.get("productSellPrice") : 0.0;
            }
            catch (Exception ex)
            {
                thisProductSellPrice = productMap.containsKey("productSellPrice") ? ((Integer) productMap.get("productSellPrice")).doubleValue() : 0.0;
            }
            // Stock
            int thisProductStock = productMap.containsKey("productStock") ? (Integer) productMap.get("productStock") : 0;
            // Stand Out Number
            int thisStandOutRowNum = productMap.containsKey("standOutRowNum") ? (Integer) productMap.get("standOutRowNum") : 0;

            //          Checking if it is okay to update
            Integer thisProductId = null;
            if (productMap.containsKey("productId")){
                // ID
                thisProductId = (Integer) productMap.get("productId");
                // Check if there is a product with the same id. If there is then throw error
                boolean isThereAProductWithThisId = productRepository.findByProductId(thisProductId) != null;
                if (isThereAProductWithThisId){
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"There is a product stored in database with this id: "+thisProductId+"\"}");
                }
            }
            if (thisProductName.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"Please send a product name to continue.\"}");
            }
            // If product link is empty then change it to sanitized product's name
            if (thisProductLink.isEmpty() || thisProductLink.isBlank()){
                thisProductLink = extraModules.sanitizeLink(thisProductName);
            }
            //  If it exists sanitise it since the user might have changed it and check it if there is already one named that way
            else {
                thisProductLink = extraModules.sanitizeLink(thisProductLink);
            }
            //  If there is a product with this link throw error
            if (productRepository.findByProductLink(thisProductLink) != null)
            {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":" +
                        "\"There is already a product with this link: "+ thisProductLink +
                        " - Product Name: " + productRepository.findByProductLink(thisProductLink).getProductName()
                        + "\"}"
                );
            }
            // Check if the sell price is not correct
            if (thisProductSellPrice == null){
                throw new RuntimeException("Please send a product sell price");
            }else if (thisProductSellPrice <= 0){
                throw new RuntimeException("Product sell price must be more then 0");
            }
            // Check if brand id is not null. If it is not then check if it exists in database if it does not then throw err
            if (thisProductBrandId != null){
                if(brandRepository.findById(thisProductBrandId).isEmpty())
                {
                    throw new RuntimeException("There is no brand with this id: " + thisProductBrandId);
                }
                // If it exists then clarify this product's brand as it
                else {
                    thisProductBrand = brandRepository.findByBrandId(thisProductBrandId);
                }
            }
            // Same process with product's category
            if (thisProductCategoryId != null){
                if(categoryRepository.findById(thisProductCategoryId).isEmpty())
                {
                    throw new RuntimeException("There is no category with this id: "+thisProductCategoryId);
                }
                else {
                    thisProductCategory = categoryRepository.findByCategoryId(thisProductCategoryId);
                }
            }
            // Checking if the stocks are correct
            if (thisProductStock<0){
                throw new RuntimeException("A product's stock must be equal or greater then 0");
            }

            // If product successfully come to this step then build it and save it
            Product product = Product.builder()
                    .brand(thisProductBrand).category(thisProductCategory).productName(thisProductName).productLink(thisProductLink)
                    .isProductActive(thisIsActive).displayProductOnHomePage(thisDisplayProductOnHomePage).isNew(isNew)
                    .heroProduct(isHeroProduct).isThereShippingFee(isThereShippingFee).productShippingFee(thisProductShippingFee)
                    .productDiscountedPrice(thisProductDiscountedPrice).productDescription(thisProductDescription).productSpecs(thisProductSpecs)
                    .productWhatsInTheBox(thisProductWhatsInTheBox).productPurchasePrice(thisProductPurchasePrice)
                    .productSellPrice(thisProductSellPrice).productStock(thisProductStock).standOutRowNum(thisStandOutRowNum).build();
            if (thisProductId != null){
                product.setProductId(thisProductId);
            }
            productRepository.save(product);
            return ResponseEntity.ok(product);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not update the product: " + ex.getMessage()));
        }
    }

    //          Update product module
    @PutMapping("/update/product")
    public ResponseEntity<Object> updateProduct(@RequestBody Map<String, Object> productMap) {
        try {
            //      Defining containing variables
            // ID
            Integer thisProductId = (Integer) productMap.get("productId");
            // Brand
            Integer thisProductBrandId = (Integer) productMap.get("brandId");
            Brand thisProductBrand = null;
            if (productMap.containsKey("brand"))
            {
                Map<String, Object> productBrand = (Map<String, Object>) productMap.get("brand");
                if (productBrand!=null)
                {
                    if (productBrand.containsKey("brandId")){
                        thisProductBrandId = (Integer) productBrand.get("brandId");
                        if (brandRepository.findById(thisProductBrandId).isEmpty())
                        {
                            throw new RuntimeException("There is no brand stored in database with this brand id: "+thisProductBrandId);
                        }
                        else
                        {
                            thisProductBrand = brandRepository.findByBrandId(thisProductBrandId);
                        }
                    }
                }
            }
            // Check if brand id is not null. If it is not then check if it exists in database if it does not then throw err
            else if (thisProductBrandId != null){
                if(brandRepository.findById(thisProductBrandId).isEmpty())
                {
                    throw new RuntimeException("There is no brand with this id: " + thisProductBrandId);
                }
                // If it exists then clarify this product's brand as it
                else {
                    thisProductBrand = brandRepository.findByBrandId(thisProductBrandId);
                }
            }

            // Same process with product's category
            Integer thisProductCategoryId = (Integer) productMap.get("categoryId");
            Category thisProductCategory = null;
            if (productMap.containsKey("category"))
            {
                Map<String, Object> productCategory = (Map<String, Object>) productMap.get("category");
                if (productCategory != null)
                {
                    if (productCategory.containsKey("categoryId")){
                        thisProductCategoryId = (Integer) productCategory.get("categoryId");
                        if (categoryRepository.findById(thisProductCategoryId).isEmpty())
                        {
                            throw new RuntimeException("There is no category stored in database with this category id: "+thisProductBrandId);
                        }
                        else
                        {
                            thisProductCategory = categoryRepository.findByCategoryId(thisProductCategoryId);
                        }
                    }
                }
            }
            // Check if category id is not null. If it is not then check if it exists in database if it does not then throw err
            else if (thisProductCategoryId != null){
                if(categoryRepository.findById(thisProductCategoryId).isEmpty())
                {
                    throw new RuntimeException("There is no category stored in the database with this id: " + thisProductCategoryId);
                }
                // If it exists then clarify this product's category as it
                else {
                    thisProductCategory = categoryRepository.findByCategoryId(thisProductCategoryId);
                }
            }
            // Name
            String thisProductName = productMap.containsKey("productName") ? (String) productMap.get("productName") : "";
            // Link
            String thisProductLink = productMap.containsKey("productLink") ? (String) productMap.get("productLink") : "";
            // Is Active
            boolean thisIsActive = productMap.containsKey("productActive") ? (Boolean) productMap.get("productActive") : true;
            // Display On Home Page
            boolean thisDisplayProductOnHomePage = productMap.containsKey("displayProductOnHomePage") ? (Boolean) productMap.get("displayProductOnHomePage") : false;
            // Is New
            boolean isNew = productMap.containsKey("new") ? (Boolean) productMap.get("new") : true;
            // Is Hero Product
            boolean isHeroProduct = productMap.containsKey("heroProduct") ? (Boolean) productMap.get("heroProduct") : false;
            // Is There Shipping Fee
            boolean isThereShippingFee = productMap.containsKey("thereShippingFee") ? (Boolean) productMap.get("thereShippingFee") : true;
            // Shipping Fee
            Double thisProductShippingFee = 65.0;
            try
            {
                thisProductShippingFee = productMap.containsKey("productShippingFee") ? (Double) productMap.get("productShippingFee") : 65.0;
            }
            catch (Exception ex)
            {
                thisProductShippingFee = productMap.containsKey("productShippingFee") ? ((Integer) productMap.get("productShippingFee")).doubleValue() : 65.0;
            }
            // Discounted price
            Double thisProductDiscountedPrice = -1.0;
            try
            {
                thisProductDiscountedPrice = productMap.containsKey("productDiscountedPrice") ? (Double) productMap.get("productDiscountedPrice") : 65.0;
            }
            catch (Exception ex)
            {
                thisProductDiscountedPrice = productMap.containsKey("productDiscountedPrice") ? ((Integer) productMap.get("productDiscountedPrice")).doubleValue() : 65;
            }
            // Description
            String thisProductDescription = productMap.containsKey("productDescription") ? (String) productMap.get("productDescription") : "";
            // Specs
            String thisProductSpecs = productMap.containsKey("productSpecs") ? (String) productMap.get("productSpecs") : "";
            // What's in the box
            String thisProductWhatsInTheBox = productMap.containsKey("productWhatsInTheBox") ? (String) productMap.get("productWhatsInTheBox") : "";
            // Purchase Price
            Double thisProductPurchasePrice = 0.0;
            try
            {
                thisProductPurchasePrice = productMap.containsKey("productPurchasePrice") ? (Double) productMap.get("productPurchasePrice") : 0.0;
            }
            catch (Exception ex)
            {
                thisProductPurchasePrice = productMap.containsKey("productPurchasePrice") ? ((Integer) productMap.get("productPurchasePrice")).doubleValue() : 0.0;
            }
            // Sell Price
            Double thisProductSellPrice = 0.0;
            try
            {
                thisProductSellPrice = productMap.containsKey("productSellPrice") ? (Double) productMap.get("productSellPrice") : 0.0;
            }
            catch (Exception ex)
            {
                thisProductSellPrice = productMap.containsKey("productSellPrice") ? ((Integer) productMap.get("productSellPrice")).doubleValue() : 0.0;
            }
            // Stock
            int thisProductStock = productMap.containsKey("productStock") ? (Integer) productMap.get("productStock") : 0;
            // Stand Out Number
            int thisStandOutRowNum = productMap.containsKey("standOutRowNum") ? (Integer) productMap.get("standOutRowNum") : 0;

            //          Checking if it is okay to update
            // Check if there is a product with this id
            Product thisProduct = productRepository.findByProductId(thisProductId);
            if (thisProduct == null){
                throw new RuntimeException("There is no product stored in database with this id: "+thisProductId);
            }
            // If product link is empty then change it to sanitized product's name
            if (thisProductLink.isEmpty() || thisProductLink.isBlank()){
                thisProductLink = extraModules.sanitizeLink(thisProductName);
            }
            //  If it exists sanitise it since the user might have changed it and check it if there is already one named that way
            else {
                thisProductLink = extraModules.sanitizeLink(thisProductLink);
            }
            //  If there is a product with this link check if it is this product. If it's not then throw error
            if (productRepository.findByProductLink(thisProductLink) != null &&
                productRepository.findByProductLink(thisProductLink) != thisProduct)
            {
                throw new
                        RuntimeException("There is already a product with this link: "+ thisProductLink +
                        " - Product Name: " + productRepository.findByProductLink(thisProductLink).getProductName()
                );
            }
            // Check if the sell price is not correct
            if (thisProductSellPrice <= 0){
                throw new RuntimeException("Sell price must be more then 0");
            }

            // Checking if the stocks are correct
            if (thisProductStock<0){
                throw new RuntimeException("A product's stock must be equal or greater then 0");
            }
            thisProduct.setBrand(thisProductBrand);
            thisProduct.setCategory(thisProductCategory);
            thisProduct.setProductName(thisProductName);
            thisProduct.setProductLink(thisProductLink);
            thisProduct.setProductActive(thisIsActive);
            thisProduct.setDisplayProductOnHomePage(thisDisplayProductOnHomePage);
            thisProduct.setNew(isNew);
            thisProduct.setHeroProduct(isHeroProduct);
            thisProduct.setThereShippingFee(isThereShippingFee);
            thisProduct.setProductShippingFee(thisProductShippingFee);
            thisProduct.setProductDiscountedPrice(thisProductDiscountedPrice);
            thisProduct.setProductDescription(thisProductDescription);
            thisProduct.setProductSpecs(thisProductSpecs);
            thisProduct.setProductWhatsInTheBox(thisProductWhatsInTheBox);
            thisProduct.setProductPurchasePrice(thisProductPurchasePrice);
            thisProduct.setProductSellPrice(thisProductSellPrice);
            thisProduct.setProductStock(thisProductStock);
            thisProduct.setStandOutRowNum(thisStandOutRowNum);
            productRepository.save(thisProduct);
            return ResponseEntity.ok().body(thisProduct);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not update the product: " + ex.getMessage()));
        }
    }

    //          Stock transactions
    @PutMapping("/update/product/stocks")
    public ResponseEntity<Object> updateStock(@RequestParam Integer productId,
                              @RequestParam Integer productStock) {
        try {

            // Check if there is a product with this id
            Product thisProduct = productRepository.findByProductId(productId);
            if (thisProduct == null){
                throw new RuntimeException("There is no product stored in database with this id: "+productId);
            }

            // Checking if the stocks are correct
            if (productStock<0){
                throw new RuntimeException("A product's stock must be equal or greater then 0");
            }

            thisProduct.setProductStock(productStock);

            productRepository.save(thisProduct);
            return ResponseEntity.ok(thisProduct);
        } catch (Exception ex) {
            throw new RuntimeException("Could not update the product: " + ex.getMessage());
        }
    }

    //          Delete product module
    @DeleteMapping("/delete/product")
    public ResponseEntity<Object> updateProduct(@RequestParam Integer productId) {
        try {
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
            productRepository.delete(thisProduct);
            return ResponseEntity.ok(thisProduct);
        }
        catch (Exception ex) {
            throw new RuntimeException("Could not delete the product: " + ex.getMessage());
        }
    }

    //          Exception Handler Module
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}