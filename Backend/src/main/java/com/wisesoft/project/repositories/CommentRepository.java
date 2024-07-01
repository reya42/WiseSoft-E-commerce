package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Comment;
import com.wisesoft.project.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository  extends JpaRepository<Comment, Integer> {
    Comment getByCommentId(int commentId);
    List<Comment> getAllByProduct(Product product);
    Page<Comment> getAllPageByProduct(Pageable pageable, Product product);

    Page<Comment> findAll(Pageable pageable);
}
