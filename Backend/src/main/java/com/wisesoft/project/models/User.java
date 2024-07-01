package com.wisesoft.project.models;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String userName;
    private String userSurname;
    private String userMail;
    private String userPassword;
    @Getter
    @Setter
    private double userBalance;
    private boolean isAdmin;
    private boolean isActive;
    private Timestamp createdAt;
}
