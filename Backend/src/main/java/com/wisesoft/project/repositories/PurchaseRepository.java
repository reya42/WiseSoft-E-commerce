package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Product;
import com.wisesoft.project.models.Purchase;
import com.wisesoft.project.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    Page<Purchase> getAllByUser(User user, Pageable pageable);
    Purchase getByUserAndProduct(User user, Product product);
    Purchase getByUserAndProductProductId(User user, int productId);
    Purchase getByPurchaseId(int id);
}
