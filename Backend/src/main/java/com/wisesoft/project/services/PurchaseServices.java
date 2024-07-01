package com.wisesoft.project.services;

import com.wisesoft.project.models.Comment;
import com.wisesoft.project.models.Product;
import com.wisesoft.project.models.Purchase;
import com.wisesoft.project.models.User;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.repositories.CommentRepository;
import com.wisesoft.project.repositories.ProductRepository;
import com.wisesoft.project.repositories.PurchaseRepository;
import com.wisesoft.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@CrossOrigin
@RestController
public class PurchaseServices {
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

    @GetMapping("/users/purchases/{user_id}")
    ResponseEntity<Object> getPurchasesOfUser(
            @PathVariable("user_id") int userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        try {
            User user = userRepository.getUserByUserId(userId);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no user with this id: " + userId);

            Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseCreatedAt").descending());

            Page<Purchase> purchases = purchaseRepository.getAllByUser(user, pageable);

            if (purchases.getContent().isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).body("You haven't purchased anything yet.");

            return ResponseEntity.ok().body(purchases);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @GetMapping("/purchases")
    public ResponseEntity<Object> getAllPurchases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by("purchaseCreatedAt").descending());

        Page<Purchase> purchases = purchaseRepository.findAll(pageable);

        return ResponseEntity.ok(purchases);
    }

    @PostMapping("/create/purchase")
    ResponseEntity<Object> createPurchase(
            @RequestParam int userId,
            @RequestParam int productId
    )
    {
        try
        {
            User user = userRepository.getUserByUserId(userId);
            if (user==null) throw new RuntimeException("There is no user with this id: " + userId);

            Product product = productRepository.findByProductId(productId);
            if (product==null) throw new RuntimeException("There is no product with this id: " + productId);

            double newBalance = user.getUserBalance();
            if (product.getProductDiscountedPrice() != -1)
            {
                if (user.getUserBalance() <= product.getProductDiscountedPrice())
                {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("{\"error\": \"Your balance is not enough to buy this product.\"");
                }
                else
                {
                    newBalance = newBalance - product.getProductDiscountedPrice();
                }
            }
            else if (user.getUserBalance() <= product.getProductSellPrice())
            {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"Your balance is not enough to buy this product.\"");
            }
            else
            {
                newBalance = newBalance - product.getProductSellPrice();
            }

            user.setUserBalance(newBalance);
            userRepository.save(user);

            Purchase purchase = Purchase.builder().user(user).product(product).purchaseCreatedAt(new Timestamp(System.currentTimeMillis())).build();

            purchaseRepository.save(purchase);

            return ResponseEntity.ok(purchase);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    @PutMapping("/purchases/toggle_delivery")
    ResponseEntity<Object> toggleDelivery(
            @RequestParam int purchaseId
    )
    {
        try {
            Purchase purchase = purchaseRepository.getByPurchaseId(purchaseId);
            if (purchase == null) throw new RuntimeException("There is no purchase with this id.");

            purchase.setDelivered(!purchase.isDelivered());
            purchaseRepository.save(purchase);

            return ResponseEntity.ok("Purchase's delivery successfully changed");
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    @DeleteMapping("/delete/purchase")
    ResponseEntity<Object> deletePurchase(@RequestParam int purchaseId)
    {
        try
        {
            Purchase purchase = purchaseRepository.getByPurchaseId(purchaseId);
            if (purchase == null)
            {
                throw new RuntimeException("There is no purchase with this id.");
            }
            else
            {
                purchaseRepository.delete(purchase);
                return ResponseEntity.ok("Purchase successfully deleted.");
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
