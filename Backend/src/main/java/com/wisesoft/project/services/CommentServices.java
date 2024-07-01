package com.wisesoft.project.services;

import com.wisesoft.project.models.*;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.CommentRepository;
import com.wisesoft.project.repositories.ProductRepository;
import com.wisesoft.project.repositories.PurchaseRepository;
import com.wisesoft.project.repositories.UserRepository;
import jakarta.persistence.Transient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class CommentServices {
    @Autowired
    PurchaseRepository purchaseRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ExtraModules extraModules;


    public Boolean setProductRatings(int productId)
    {
        Product product = productRepository.findByProductId(productId);
        if (product == null)  return false;
        else
        {
            List<Comment> comments = commentRepository.getAllByProduct(product);
            int totalRating = 0;
            for (Comment comment : comments)
            {
                totalRating += comment.getCommentRating();
            }
            float averageRating = (float) totalRating / comments.size();

            int numberOfRates = comments.size();

            product.setAverageRating(averageRating);
            product.setNumberOfRates(numberOfRates);
            productRepository.save(product);

            return true;
        }
    }

    @GetMapping("/product/ratings")
    public ResponseEntity<Object> getProductRatings(
            @RequestParam int productId
    )
    {
        Product product = productRepository.findByProductId(productId);
        if (product == null)  return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"There is no product in the database that contain this id\"}");
        else
        {
            setProductRatings(productId);

            return ResponseEntity.ok(
                    "{" +
                                "\"averageRating\":"+ product.getAverageRating() +", " +
                                "\"numberOfRates\":"+ product.getNumberOfRates() +", " +
                          "}"
            );
        }
    }

    @GetMapping("/products/{product_link}/comments")
    public ResponseEntity<Object> getProductsCommentsPage(
            @PathVariable("product_link") String link,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        )
    {
        try{
            Product product = productRepository.findByProductLinkAndIsProductActiveIsTrue(link);

            if (product == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"There is no product in the database that contain this link: "+link+"\"}");

            Pageable pageable = PageRequest.of(page, size, Sort.by("commentCreatedAt").descending());

            Page<Comment> comments = commentRepository.getAllPageByProduct(pageable, product);

            return ResponseEntity.ok(comments);
        }
        catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    @GetMapping("/comments")
    public ResponseEntity<Object> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by("commentCreatedAt").descending());

        Page<Comment> comments = commentRepository.findAll(pageable);

        return ResponseEntity.ok(comments);
    }

    @PostMapping("/create/comment")
    public ResponseEntity<Object> createComment(
            @RequestBody Map<String, Object> commentRequirementsMap,
            @RequestParam int productId
    )
    {
        // --------------- User
        if (!commentRequirementsMap.containsKey("userId")) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"You should send an user_id to comment.\"}");
        int userId = (int) commentRequirementsMap.get("userId");

        User user = userRepository.getUserByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"There is no user in the database with this user id: " + userId + "\"}");

        // --------------- Purchase
        Purchase purchase = purchaseRepository.getByUserAndProductProductId(user, productId);
        if (purchase == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"You cannot comment on this product since you haven't purchased it.\"}");
        else if (!purchase.isDelivered()) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"You cannot comment on this product since you haven't received it yet.\"}");

        Product product = purchase.getProduct();

        // --------------- Comment Rate
        if (!commentRequirementsMap.containsKey("commentRate")) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"You should send a comment_rate to comment\"}");
        int commentRate = (int) commentRequirementsMap.get("commentRate");

        // --------------- Comment Content
        String commentContent = null;
        if (commentRequirementsMap.containsKey("commentContent")) commentContent = (String) commentRequirementsMap.get("commentContent");
        // --------------- Sanitize to prevent injections
        String sanitizedCommentContent = extraModules.sanitizeForSecurity(commentContent);

        // --------------- Creating Comment
        Comment comment =
                Comment.builder()
                .commentCreatedAt(new Timestamp(System.currentTimeMillis()))
                .commentContent(sanitizedCommentContent)
                .commentRating(commentRate)
                .product(product)
                .user(user)
                .isActive(true)
                .build();

        setProductRatings(product.getProductId());
        commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.OK).body(comment);

    }
    @PutMapping("/comments/toggle_activity")
    ResponseEntity<Object> toggleActivity(@RequestParam int commentId)
    {
        try
        {
            Comment comment = commentRepository.getByCommentId(commentId);
            if (comment == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("{\"error\":\"There is no comment with this id: "+ commentId +"\"}");

            comment.setActive(!comment.isActive());

            commentRepository.save(comment);

            setProductRatings(comment.getProduct().getProductId());
            return ResponseEntity.ok(comment);
        }
        catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }
    @PutMapping("/update/comment")
    ResponseEntity<Object> updateComment(@RequestBody Map<String, Object> newComment)
    {
        try
        {

            // ----------- Comment
            if (!newComment.containsKey("commentId")) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"Please send an comment_id to update it.\"}");
            int commentId = (int) newComment.get("commentId");

            Comment comment = commentRepository.getByCommentId(commentId);
            if (comment == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"There is no comment with this id: "+ commentId +"\"}");
            }
            if (comment.isUpdatedOnce()) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"This comment already updated. You can only update once\"}");

            // ----------- Content
            String newContent = (String) newComment.get("comment_content");
            if (newContent == null) newContent = comment.getCommentContent();

            // ----------- Rating
            int newRating = newComment.containsKey("commentRating") ? (int) newComment.get("commentRating") : comment.getCommentRating();

            if (comment.getCommentContent().equals(newContent) && comment.getCommentRating() == newRating)
            {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"Can't update since you did not changed anything in the comment.\"}");
            }
            else
            {
                comment.setCommentCreatedAt(new Timestamp(System.currentTimeMillis()));
                comment.setUpdatedOnce(true);
                comment.setCommentContent(newContent);
                comment.setCommentRating(newRating);

                commentRepository.save(comment);

                setProductRatings(comment.getProduct().getProductId());

                return ResponseEntity.ok(comment);
            }
        }
        catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }
    @PutMapping("/update/comment/by_admin")
    ResponseEntity<Object> updateCommentByAdmin(@RequestBody Map<String, Object> newComment)
    {
        // ----------- Comment
        if (!newComment.containsKey("commentId")) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("{\"error\":\"Please send an comment_id to update it.\"}");
        int commentId = (int) newComment.get("commentId");

        Comment comment = commentRepository.getByCommentId(commentId);
        if (comment == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"There is no comment with this id: "+ commentId +"\"}");
        }

        // ----------- Content
        String newContent = (String) newComment.get("comment_content");
        if (newContent == null) newContent = comment.getCommentContent();

        // ----------- Rating
        int newRating = newComment.containsKey("commentRating") ? (int) newComment.get("commentRating") : comment.getCommentRating();

        // ----------- Updated Once
        boolean updatedOnce = newComment.containsKey("updatedOnce") ? (boolean) newComment.get("updatedOnce") : comment.isUpdatedOnce();

        // ----------- Is Active
        boolean isActive = newComment.containsKey("active") ? (boolean) newComment.get("active") : comment.isActive();

        // ----------- Created At
        Timestamp createdAt = newComment.containsKey("commentCreatedAt") ? (Timestamp) newComment.get("commentCreatedAt") : comment.getCommentCreatedAt();

        comment.setUpdatedOnce(updatedOnce);
        comment.setCommentContent(newContent);
        comment.setCommentRating(newRating);
        comment.setActive(isActive);
        comment.setCommentCreatedAt(createdAt);

        commentRepository.save(comment);

        setProductRatings(comment.getProduct().getProductId());

        return ResponseEntity.ok(comment);
    }
    @DeleteMapping("/delete/comment")
    public ResponseEntity<Object> deleteComment(@RequestParam int commentId)
    {
        Comment comment = commentRepository.getByCommentId(commentId);
        if (comment == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"There is no comment with this id: "+ commentId +"\"}");
        else {
            commentRepository.delete(comment);
            setProductRatings(comment.getProduct().getProductId());
            return ResponseEntity.ok(comment);
        }
    }
    //          Exception Handler Module
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}
