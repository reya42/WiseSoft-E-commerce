package com.wisesoft.project.modules;

import com.wisesoft.project.models.Banner;
import com.wisesoft.project.models.Category;
import com.wisesoft.project.models.Image;
import com.wisesoft.project.repositories.BannerRepository;
import com.wisesoft.project.repositories.CategoryRepository;
import com.wisesoft.project.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class ExtraModules {     // This is the class where I am storing the most used functions around the classes
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ImageRepository imageRepository;
    public String sanitizeLink(String link) {

        // Defined characters that are not allowed in the product link
        String[] forbiddenCharacters = {"!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "`", "{", "|", "}", "~"};

        // Deleting all forbidden characters
        for (String forbiddenChar : forbiddenCharacters) {
            // Escape special characters for regular expression
            link = link.replaceAll("\\Q" + forbiddenChar + "\\E", "");
        }

        // Checking for non-ASCII characters and deleting them
        link = link.replaceAll("\\Q" + "[^\\x00-\\x7F]" + "\\E", "");

        // Replacing spaces with + and / with -
        link = link.replaceAll("\\Q" + " " + "\\E", "-");

        // Lowercase it
        link = link.toLowerCase();

        // Map of Turkish characters to their English counterparts
        Map<String, String> turkishToEnglish = new HashMap<>();
        turkishToEnglish.put("ğ", "g");
        turkishToEnglish.put("ü", "u");
        turkishToEnglish.put("ş", "s");
        turkishToEnglish.put("ı", "i");
        turkishToEnglish.put("ö", "o");
        turkishToEnglish.put("ç", "c");

        // Replace Turkish characters with their English counterparts
        for (Map.Entry<String, String> entry : turkishToEnglish.entrySet()) {
            link = link.replaceAll(
                    entry.getKey(),
                    entry.getValue()
            );
        }
        return link;
    }
    public List<Category> getAllChildCategories(Integer parentCategoryId) {
        // Adding all the children of this category
        List<Category> childCategories = categoryRepository.findByCategoryParentId(parentCategoryId);
        // Calling this function again in all those children and adding them into this list too.
        for (Category childCategory : new ArrayList<>(childCategories)) {
            childCategories.addAll(getAllChildCategories(childCategory.getCategoryId()));
        }
        // Returning all those children the parent has
        return childCategories;
    }
    public void setChildrenCategories(Category category, List<Category> allCategories) {
        List<Category> children = allCategories.stream()
                .filter(c -> c.getCategoryParentId() != null && c.getCategoryParentId().equals(category.getCategoryId()))
                .collect(Collectors.toList());
        category.setChildrenCategories(children);
        children.forEach(child -> setChildrenCategories(child, allCategories));
    }

    public final String save_Dir = "C:/wisesoft_saved_images/";
    public Image createImage(MultipartFile image, String parentDirectory,Integer productId){
        try {
            // Check if the file is null or not an image
            if (image == null || !image.getContentType().startsWith("image/")) {
                throw new RuntimeException("Please send an image file. Remember to use \"image\" as its name.");
            }
            // Original file name to be seperated by
            String originalFilename = image.getOriginalFilename();
            // Separating name part from extension part
            String imageName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            imageName = sanitizeLink(imageName);
            // Extension part
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // Assigning a filepath variable that contains where to save the file information
            String imageDir = save_Dir + parentDirectory + imageName + extension;

            // Checking if the save directory exist if it is not then create those folders
            File directory = new File(save_Dir + parentDirectory);
            if (!directory.exists()){
                if (!directory.mkdirs()){
                    throw new RuntimeException("Directory folders could not be created: "+ directory.getPath());
                }
            }
            // Save image to the database
            Image savedImage = imageRepository.save(
                    Image.builder()
                            .imageName(parentDirectory+imageName+extension)
                            .imageDirectory(imageDir)
                            .productId(productId)
                            .build()
            );
            // Copy image to system
            image.transferTo(new File(imageDir));

            return savedImage;
        }
        catch (Exception ex){
            throw new RuntimeException("An error occurred while creating image: "+ex.getMessage());
        }
    }
    @Autowired
    BannerRepository bannerRepository;
    public Boolean deleteImage(Image image, String parentDirectory, boolean deleteWholeFolder){
        try {
            parentDirectory = save_Dir + parentDirectory;
            // Delete image from system

            File file = new File(image.getImageDirectory());
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully from system");
                } else {
                    throw new RuntimeException("Failed to delete the file from system.");
                }
            } else {
                System.out.println("File does not exist on the system.");
            }

            // Delete the image record from the database
            imageRepository.deleteById(image.getImageId());
            Banner isThereBanner = bannerRepository.findByImageImageId(image.getImageId());
            if (isThereBanner != null)
            {
                bannerRepository.delete(isThereBanner);
            }

            // Delete the product's image directory from file system
            if (deleteWholeFolder){
                File directory = new File(parentDirectory);
                if (directory.exists()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (!f.delete()) {
                                throw new RuntimeException("Failed to delete file: " + f.getAbsolutePath());
                            }
                        }
                    }
                    if (!directory.delete()) {
                        throw new RuntimeException("Failed to delete directory: " + directory.getAbsolutePath());
                    }
                }
            }
            return true;
        }
        catch (Exception e){
            throw new RuntimeException("An error occurred while deleting image: "+e.getMessage());
        }
    }

    public String sanitizeForSecurity(String input) {

        // Defined characters that are not allowed in the product link
        String[] forbiddenCharacters = {"<", ">", "&", "'", ";", "\"", "/", "\\","drop","table","database","create","insert","into","values"};

        // Deleting all forbidden characters
        for (String forbiddenChar : forbiddenCharacters) {
            // Escape special characters for regular expression
            input = input.replaceAll("\\Q" + forbiddenChar + "\\E", "");
        }

        return input;
    }

}
