package com.wisesoft.project.repositories;

import com.wisesoft.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Integer> {
    User getUserByUserId(Integer integer);
    User getUserByUserMail(String mail);
    User getUserByUserNameAndUserSurname(String userName, String userSurname);
}
