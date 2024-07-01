package com.wisesoft.project.services;

import com.wisesoft.project.models.Category;
import com.wisesoft.project.models.Image;
import com.wisesoft.project.models.Product;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.CategoryRepository;
import com.wisesoft.project.repositories.ImageRepository;
import com.wisesoft.project.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class CategoryServices {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ExtraModules extraModules;

    @GetMapping("/categories")
    public List<Category> getCategories(){
        return categoryRepository.findAll();
    }

    @GetMapping("/categories/parent-to-children")
    public ResponseEntity<List<Category>> getAncestorCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        List<Category> rootCategories = categoryRepository.findAllByCategoryParentIdIsNull();
        rootCategories.forEach(rootCategory -> extraModules.setChildrenCategories(rootCategory, allCategories));
        return ResponseEntity.ok(rootCategories);
    }

    @GetMapping("/home/categories")
    List<Category> getAllCategoriesOnHome()
    {
        return categoryRepository.getAllByDisplayOnHomePageIsTrue();
    }
    @GetMapping("/category/{category_link}")
    public ResponseEntity<?> getCategoryByCategoryLink(@PathVariable String category_link) {
        try {
            Category parentCategory = categoryRepository.findByCategoryLink(category_link);
            if (parentCategory != null) {
                return ResponseEntity.ok(parentCategory);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no category with this link!");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error:\"\"An error occurred while getting category from this link: " + category_link + ". " + ex.getMessage()+"\"}");
        }
    }

    @GetMapping("/category/{category_link}/products")
    public ResponseEntity<?> getAllByCategory(@PathVariable String category_link, @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 12, Sort.by("standOutRowNum").descending());

        try {
            Category parentCategory = categoryRepository.findByCategoryLink(category_link);
            if (parentCategory != null) {
                // Getting all the children of this category
                List<Category> categories = extraModules.getAllChildCategories(parentCategory.getCategoryId());
                categories.add(parentCategory);
                Page<Product> products = productRepository.findByCategories(categories, pageable);
                for (Product product : products.getContent()) {
                    List<Image> images = imageRepository.findByProductId(product.getProductId());
                    product.setImages(images);
                }
                return ResponseEntity.ok(products);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no category with this link!");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error:\"\"An error occurred while getting category from this link: " + category_link + ". " + ex.getMessage()+"\"}");
        }
    }

    @GetMapping("/category/{category_link}/home")
    public Page<Product> getAllByCategoryAndHome(
            @PathVariable String category_link,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("standOutRowNum").descending());

        try {
            Category parentCategory = categoryRepository.findByCategoryLink(category_link);
            if (parentCategory != null) {
                // Getting all the children of this category
                List<Category> categories = extraModules.getAllChildCategories(parentCategory.getCategoryId());
                categories.add(parentCategory);

                Page<Product> products = productRepository.findByCategoriesAndDisplayProductOnHomePage(categories, pageable);
                for (Product product : products.getContent()) {
                    List<Image> images = imageRepository.findByProductId(product.getProductId());
                    product.setImages(images);
                }
                return products;
            } else {
                throw new RuntimeException("There is no category with this link!");
            }
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while getting products from this category link: " + category_link + ". " + ex.getMessage());
        }
    }


    @PostMapping("/create/category")
    public String createCategory(@RequestBody Map<String, Object> categoryMap){
        try{
            String thisCategoryName = categoryMap.containsKey("categoryName") ? (String) categoryMap.get("categoryName") : "";
            String thisCategoryLink = categoryMap.containsKey("categoryLink") ?
                    extraModules.sanitizeLink((String) categoryMap.get("categoryLink"))
                    :
                    extraModules.sanitizeLink(thisCategoryName);
            Integer parentCategoryId = categoryMap.containsKey("parentCategoryId") ? (Integer) categoryMap.get("parentCategoryId") : null;

            // Does category name contain anything in it
            if(thisCategoryName.isEmpty()){
                throw new RuntimeException("You cant leave category name empty.");
            }
            // if it does check if there is already a category with this name
            else if (categoryRepository.findByCategoryName(thisCategoryName) != null){
                throw new RuntimeException("A catagory already exist with this name: "+thisCategoryName);
            }

            // Check if there is already a category with this link
            if (categoryRepository.findByCategoryLink(thisCategoryLink) != null){
                throw new RuntimeException("There is already a category with this link: "+thisCategoryLink);
            }

            // Check if category parent id exist
            if (parentCategoryId != null){
                // If it exists then check if there is a category with this id
                if (categoryRepository.findByCategoryId(parentCategoryId) == null)
                {
                    throw new RuntimeException("There is no category with this parent category id");
                }
            }

            Category thisCategory = new Category(thisCategoryName,thisCategoryLink,parentCategoryId);

            categoryRepository.save(thisCategory);
            return "Succesfully created this category: "+thisCategory.getCategoryName();
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error creating category. "+ ex.getMessage());
        }
    }

    @PutMapping("/update/category")
    public String updateCategory(@RequestBody Map<String, Object> categoryMap){
        try{
            Integer thisCategoryId = categoryMap.containsKey("categoryId") ? (Integer) categoryMap.get("categoryId") : null;

            if (thisCategoryId == null){
                throw new RuntimeException("Please send an id to update a category.");
            }

            String thisCategoryName = categoryMap.containsKey("categoryName") ? (String) categoryMap.get("categoryName") : "";
            String thisCategoryLink = categoryMap.containsKey("categoryLink") ?
                    extraModules.sanitizeLink((String) categoryMap.get("categoryLink"))
                    :
                    extraModules.sanitizeLink(thisCategoryName);
            Integer categoryParentId = categoryMap.containsKey("categoryParentId") ? (Integer) categoryMap.get("categoryParentId") : null;
            Boolean thisDisplayOnHomePage = categoryMap.containsKey("displayOnHomePage") ? (Boolean) categoryMap.get("displayOnHomePage") : null;

            // find category from the id we get. If there is no cat. such like that then we can't update
            Category thisCategory = categoryRepository.findByCategoryId(thisCategoryId);
            if (thisCategory == null){
                throw new RuntimeException("There is no category stored in database with this id: "+thisCategoryId);
            }

            // Does category name contain anything in it
            if(thisCategoryName.isEmpty()){
                throw new RuntimeException("You cant leave category name empty.");
            }
            // If it does check if there is already a category with this name
            else if (categoryRepository.findByCategoryName(thisCategoryName) != null){
                //  If there is a category with this name, and it is not this category then throw error
                if (categoryRepository.findByCategoryName(thisCategoryName) != thisCategory){
                    throw new RuntimeException("A catagory already exist with this name: "+thisCategoryName);
                }
            }

            // Check if there is any other category that contain this link other than this category
            if (categoryRepository.findByCategoryLink(thisCategoryLink) != null && categoryRepository.findByCategoryLink(thisCategoryLink) != thisCategory){
                throw new RuntimeException("There is already a category with this link other than this category.");
            }

            // Check if category parent id exist
            if (categoryParentId != null){
                // If it exists then check if there is a category with this id
                if (categoryRepository.findByCategoryId(categoryParentId) == null)
                {
                    throw new RuntimeException("There is no category with this parent category id");
                }
            }

            if (thisDisplayOnHomePage == null)
            {
                throw new RuntimeException("Please send displayOnHomePage value to continue");
            }

            thisCategory = new Category(thisCategoryId, thisCategoryName, thisCategoryLink, categoryParentId,thisDisplayOnHomePage);

            categoryRepository.save(thisCategory);
            return "Succesfully updated this category: "+thisCategory.getCategoryName();
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error updating category. "+ ex.getMessage());
        }
    }

    @DeleteMapping("/delete/category")
    public String deleteCategory(@RequestParam Integer categoryId,
                                 @RequestParam(defaultValue = "false") Boolean removeProducts,
                                 @RequestParam(defaultValue = "false") Boolean removeChildren) {
        try {
            Category categoryToDelete = categoryRepository.findByCategoryId(categoryId);
            if (categoryToDelete == null) {
                throw new RuntimeException("Cannot find a category with this category id: " + categoryId);
            }

            // If removeChildren is true, delete all child categories and set isActive to false on associated products if removeProducts is also true
            if (removeChildren) {
                List<Category> categories = extraModules.getAllChildCategories(categoryId);
                categories.add(categoryToDelete);

                if (removeProducts) {
                    for (Category singleCategory : categories) {
                        List<Product> productsWithCategory = productRepository.findAllByCategoryAndIsProductActiveIsTrue(singleCategory);
                        for (Product product : productsWithCategory) {
                            product.setProductActive(false); // Set isProductActive to false
                            productRepository.save(product);
                        }
                    }
                }
                categoryRepository.deleteAll(categories); // Delete all categories including the parent
            } else {
                // If removeChildren is false, only update the parent category's children to have a new parent category
                Integer parentCategoryId = categoryToDelete.getCategoryParentId();
                List<Category> childCategories = categoryRepository.findByCategoryParentId(categoryId);
                for (Category childCategory : childCategories) {
                    childCategory.setCategoryParentId(parentCategoryId);
                    categoryRepository.save(childCategory);
                }

                // If removeProducts is true, update all products associated with the parent category to be inactive
                if (removeProducts) {
                    List<Product> productsWithCategory = productRepository.findAllByCategoryAndIsProductActiveIsTrue(categoryToDelete);
                    for (Product product : productsWithCategory) {
                        product.setProductActive(false); // Set isProductActive to false
                        productRepository.save(product);
                    }
                } else {
                    // If removeProducts is false, remove the category reference from all associated products
                    List<Product> productsWithCategory = productRepository.findAllByCategoryAndIsProductActiveIsTrue(categoryToDelete);
                    for (Product product : productsWithCategory) {
                        product.setCategory(null);
                        productRepository.save(product);
                    }
                }
            }

            // Delete the parent category
            categoryRepository.delete(categoryToDelete);
            return "Category deleted successfully. All the children have been reassigned to their grandparent category if available.";
        } catch (Exception ex) {
            throw new RuntimeException("Error deleting the category. " + ex.getMessage());
        }
    }

    //          Exception Handler Module
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}
