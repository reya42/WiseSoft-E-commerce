package com.wisesoft.project.services;


import com.wisesoft.project.models.Product;
import com.wisesoft.project.models.Question;
import com.wisesoft.project.models.User;
import com.wisesoft.project.modules.ExtraModules;
import com.wisesoft.project.modules.SecurityModule;
import com.wisesoft.project.repositories.*;
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
public class QuestionServices {
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ExtraModules extraModules;
    @Autowired
    SecurityModule securityModule;

    @GetMapping("/question")
    private ResponseEntity<Object> getQuestionWithId(
            @RequestParam int questionId
    )
    {
        Question question = questionRepository.findById(questionId);

        return ResponseEntity.ok(question);
    }

    @GetMapping("/questions")
    private ResponseEntity<Object> getAllQuestionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "false") boolean isHereToAnswer
    )
    {
        Pageable pageable;
        if (!isHereToAnswer) pageable = PageRequest.of(page, size, Sort.by("questionCreatedAt").descending());
        else pageable = PageRequest.of(page, size, Sort.by("questionAnsweredAt").ascending());

        Page<Question> questions = questionRepository.findAll(pageable);

        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/{product_link}")
    private ResponseEntity<Object> getRecentQuestionsOfAProductPage(
            @PathVariable("product_link") String product_link,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "false") boolean isHereToAnswer
    )
    {
        Pageable pageable;
        if (isHereToAnswer) pageable = PageRequest.of(page, size, Sort.by("questionCreatedAt").descending());
        else pageable = PageRequest.of(page, size, Sort.by("questionAnsweredAt").ascending());

        Product product = productRepository.findByProductLink(product_link);

        if (product == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"There is no product on this link: "+ product_link +"\"}"
        );

        Page<Question> questions = questionRepository.findAllByProduct(pageable, product);

        return ResponseEntity.ok(questions);
    }

    @PostMapping("/create/question")
    private ResponseEntity<Object> createQuestion(
            @RequestBody Map<String, Object> questionMap
        )
    {
        // ------------ User
        int userId = 0;
        if (questionMap.containsKey("userId")) userId = (int) questionMap.get("userId");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"Please send an user id to continue.\"}"
        );

        User user = userRepository.getUserByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"There is no user in the database with this id: "+ userId +"\"}"
        );


        // ------------ Product
        int productId = 0;
        if (questionMap.containsKey("productId")) productId = (int) questionMap.get("productId");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"Please send an product id to continue.\"}"
        );

        Product product = productRepository.findByProductId(productId);
        if (product == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"There is no product in the database with this id: "+ userId +"\"}"
        );


        // ------------ content
        String content;
        if (questionMap.containsKey("content")) content = (String) questionMap.get("content");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"Please send a question to continue.\"}"
        );
        if (content.isBlank() || content.length() < 10) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
            "{\"error\":\"Please send a real question to continue.\"}"
        );

        // - Security
        if (securityModule.hasInjectionPattern(content)) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"Injection detected.\"}"
        );

        // - Duplication
        Question questionDup = questionRepository.findByContentContaining(content);
        if (questionDup != null && questionDup.getProduct() == product)  return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"There is already a question for this product which is containing the same content.\"}"
        ); 

        // ------------ Creation and db conn
        Timestamp curTS = new Timestamp(System.currentTimeMillis());
        Question question = Question.builder().user(user).product(product).content(content).questionAnsweredAt(curTS).questionCreatedAt(curTS).build();

        questionRepository.save(question);
        return ResponseEntity.ok(question);
    }

    @PutMapping("/update/question")
    private ResponseEntity<Object> updateQuestion(
            @RequestBody Map<String, Object> questionMap,
            @RequestParam int currentUserId
    ) {
        try {
            // ------------ Question
            int questionId = 0;
            if (questionMap.containsKey("id")) {
                questionId = (int) questionMap.get("id");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":\"Please send a question_id to continue.\"}"
                );
            }

            Question question = questionRepository.findById(questionId);
            if (question == null) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":\"There is no question in the database with this id: " + questionId + "\"}"
                );
            }

            // ---- Default
            User questionUser = question.getUser();
            Product questionProduct = question.getProduct();
            User questionAdmin = question.getAdmin();
            String content = question.getContent();
            String answer = question.getAnswer();
            Timestamp createdAt = question.getQuestionCreatedAt();
            Timestamp answeredAt = question.getQuestionAnsweredAt();
            Boolean isActive = question.isActive();
            boolean answered = false;

            // ------ User That Wants To Update Question

            User currentUser = userRepository.getUserByUserId(currentUserId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":\"There is no user with this id: " + currentUserId + "\"}"
                );
            } else if (currentUser != questionUser && !currentUser.isAdmin()) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":\"You cannot update this question.\"}"
                );
            }

            // ------ Content
            if (questionMap.containsKey("content")) {
                content = (String) questionMap.get("content");
                createdAt = new Timestamp(System.currentTimeMillis());
            }

            if (currentUser.isAdmin()) {
                // ------ Is Active
                if (questionMap.containsKey("isActive")) {
                    isActive = (boolean) questionMap.get("isActive");
                }

                // ------ Answer
                if (questionMap.containsKey("answer")) {
                    questionAdmin = currentUser;
                    answer = (String) questionMap.get("answer");
                    if (!answer.equals(question.getAnswer())) answered = true;
                }
            }

            // ----- Security check
            if (answer != null)
            {
                if (!answer.equals(question.getAnswer()) && securityModule.hasInjectionPattern(answer)) {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                            "{\"error\":\"Injection detected on answer.\"}"
                    );
                }
            }

            if (securityModule.hasInjectionPattern(content)) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":\"Injection detected.\"}"
                );
            }

            // - Duplication
            Question questionDup = questionRepository.findByContentContaining(content);
            if (questionDup != null && questionDup.getProduct() == questionProduct && questionDup != question) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{\"error\":\"There is already a question for this product which is containing the same content.\"}"
                );
            }

            // Creation - DB Connection And Saving
            question.setUser(questionUser);
            question.setProduct(questionProduct);
            question.setAdmin(questionAdmin);
            question.setContent(content);
            question.setAnswer(answer);
            question.setQuestionCreatedAt(createdAt);
            if (answered)
            {
                question.setQuestionAnsweredAt(new Timestamp(System.currentTimeMillis()));
            }
            question.setActive(isActive);

            questionRepository.save(question);

            return ResponseEntity.ok(question);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "{\"error\":\"An unexpected error occurred: " + e.getMessage() + "\"}"
            );
        }
    }

    @DeleteMapping("/delete/question")
    private ResponseEntity<Object> deleteQuestion(
            @RequestParam int questionId
    )
    {
        Question question = questionRepository.findById(questionId);
        if (question == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{\"error\":\"There is no question with this question id.\"}"
        );
        questionRepository.delete(question);
        return ResponseEntity.ok(question);
    }

    //          Exception Handler Module
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}
