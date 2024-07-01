package com.wisesoft.project.repositories;

import com.wisesoft.project.models.Product;
import com.wisesoft.project.models.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Page<Question> findAllByProduct(Pageable pageable, Product product);
    List<Question> findAllByProduct(Product product);
    Question findByContentContaining(String questionContent);
    Question findById(int id);
}
