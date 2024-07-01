package com.wisesoft.project.services;

import com.wisesoft.project.models.User;
import com.wisesoft.project.modules.SecurityModule;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@CrossOrigin
@RestController
public class UserServices {

    @Autowired
    UserRepository userRepository;
    @Autowired
    SecurityModule securityModule;

    @GetMapping("/users")
    public ResponseEntity<Object> getUsersPage(
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "0") int page
    )
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);
        if (users.getContent().isEmpty()) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(users);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user")
    public ResponseEntity<Object> getUser(
            @RequestParam String userMail,
            @RequestParam String userPassword
    )
    {
        // ---- Security
        if (securityModule.hasInjectionPattern(userMail) || securityModule.hasInjectionPattern(userPassword))
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Injection detected.\""+
                    "}"
            );
        }

        User user = userRepository.getUserByUserMail(userMail);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"There is no account with this mail.\""+
                "}"
        );
        else if (user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"You can not login from users login.\""+
                    "}"
            );
        }
        else if (user.getUserPassword().equals(userPassword))
        {
            return ResponseEntity.ok(user);
        }
        else  return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Wrong password.\""+
                    "}"
            );
    }
    @PostMapping("/admin")
    public ResponseEntity<Object> getAdmin(
            @RequestParam String userMail,
            @RequestParam String userPassword
    )
    {
        // ---- Security
        if (securityModule.hasInjectionPattern(userMail) || securityModule.hasInjectionPattern(userPassword))
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Injection detected.\""+
                    "}"
            );
        }

        User user = userRepository.getUserByUserMail(userMail);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"There is no account with this mail.\""+
                "}"
        );
        else if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"You can not login from admins login.\""+
                    "}"
            );
        }
        else if (user.getUserPassword().equals(userPassword))
        {
            return ResponseEntity.ok(user);
        }
        else  return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Wrong password.\""+
                    "}"
            );
    }

    @PostMapping("/create/user")
    public ResponseEntity<Object> createUser(
            @RequestBody Map<String, Object> userMap
            )
    {
        // ---------- userName
        String userName = null;
        if (userMap.containsKey("userName")) userName = (String) userMap.get("userName");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"Please send an user_name to create an user.\""+
                "}"
        );

        // ---------- userSurname
        String userSurname = null;
        if (userMap.containsKey("userSurname")) userSurname = (String) userMap.get("userSurname");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"Please send an user_surname to create an user.\""+
                "}"
        );

        // ---------- userMail
        String userMail = null;
        if (userMap.containsKey("userMail")) userMail = (String) userMap.get("userMail");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"Please send an user_mail to create an user.\""+
                "}"
        );

        // ---------- userPassword
        String userPassword = null;
        if (userMap.containsKey("userPassword")) userPassword = (String) userMap.get("userPassword");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"Please send an user_password to create an user.\""+
                "}"
        );

        // ---------- userBalance
        double userBalance = 0;
        if (userMap.containsKey("userBalance")) userBalance = (double) userMap.get("userBalance");

        // ---------- isAdmin
        boolean isAdmin = false;
        if (userMap.containsKey("admin")) isAdmin = (boolean) userMap.get("admin");

        // ---------- isActive
        boolean isActive = true;
        if (userMap.containsKey("active")) isActive = (boolean) userMap.get("active");

        // ---------- createdAt
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        if (userMap.containsKey("createdAt")) createdAt = (Timestamp) userMap.get("createdAt");


        // ---------- Security Checks Before Connecting To Database
        if (
                securityModule.hasInjectionPattern(userName) || securityModule.hasInjectionPattern(userSurname)
                || securityModule.hasInjectionPattern(userMail) || securityModule.hasInjectionPattern(userPassword))
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Injection detected.\""+
                    "}"
            );
        }
        if (!securityModule.isValidMail(userMail).isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Invalid mail. "+ securityModule.isValidMail(userMail) +" Email:"+ userMail+"\""+
                    "}"
            );
        }
        if (!securityModule.isValidPassword(userPassword).isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Invalid password. "+ securityModule.isValidPassword(userPassword) +" Password:"+ userPassword+"\""+
                    "}"
            );
        }

        // ---------- Duplication Checks
        User accountWithThisMail = userRepository.getUserByUserMail(userMail);
        if (accountWithThisMail != null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"There is already an account with this mail.\""+
                        "}"
        );
        User accountWithSameNameAndSurname = userRepository.getUserByUserNameAndUserSurname(userName, userSurname);
        if (accountWithSameNameAndSurname != null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"There is already an account with this name and surname.\""+
                        "}"
        );

        // ---------- Creation And Saving To DB
        User user = User.builder().userName(userName).userSurname(userSurname).userMail(userMail).userPassword(userPassword)
                .userBalance(userBalance).isAdmin(isAdmin).isActive(isActive).createdAt(createdAt).build();

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }


    @PutMapping("/update/user")
    public ResponseEntity<Object> updateUser(
            @RequestBody Map<String, Object> userMap
    )
    {
        // ---------- userId
        int userId = 0;
        if (userMap.containsKey("userId")) userId = (int) userMap.get("userId");
        else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"Please send an user_id to update an user.\""+
                "}"
        );

        // ---------- user
        User currentUser = userRepository.getUserByUserId(userId);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"There is no user found in the database with this id:"+ userId +".\""+
                "}"
        );


        // ---------- userName
        String userName = currentUser.getUserName();
        if (userMap.containsKey("userName")) userName = (String) userMap.get("userName");

        // ---------- userSurname
        String userSurname = currentUser.getUserSurname();
        if (userMap.containsKey("userSurname")) userSurname = (String) userMap.get("userSurname");

        // ---------- userMail
        String userMail = currentUser.getUserMail();
        if (userMap.containsKey("userMail")) userMail = (String) userMap.get("userMail");

        // ---------- userPassword
        String userPassword = currentUser.getUserPassword();
        if (userMap.containsKey("userPassword")) userPassword = (String) userMap.get("userPassword");


        // ---------- userBalance
        double userBalance = currentUser.getUserBalance();
        if (userMap.containsKey("userBalance")) userBalance = (double) userMap.get("userBalance");

        // ---------- isAdmin
        boolean isAdmin = currentUser.isAdmin();
        if (userMap.containsKey("admin")) isAdmin = (boolean) userMap.get("admin");

        // ---------- isActive
        boolean isActive = currentUser.isActive();
        if (userMap.containsKey("active")) isActive = (boolean) userMap.get("active");

        // ---------- createdAt
        // ---------- createdAt
        Timestamp createdAt = currentUser.getCreatedAt();
        if (userMap.containsKey("createdAt")) {
            String createdAtStr = (String) userMap.get("createdAt");
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_DATE_TIME);
                createdAt = Timestamp.valueOf(localDateTime);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                        "{" +
                                "\"error\":\"Invalid date format for createdAt.\"" +
                                "}"
                );
            }
        }

        // ---------- Security Checks Before Connecting To Database
        if (
                securityModule.hasInjectionPattern(userName) || securityModule.hasInjectionPattern(userSurname)
                        || securityModule.hasInjectionPattern(userMail) || securityModule.hasInjectionPattern(userPassword))
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Injection detected.\""+
                    "}"
            );
        }
        if (!securityModule.isValidName(userName).isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Invalid user name. "+ securityModule.isValidName(userName) +" Name:"+ userName+"\""+
                    "}"
            );
        }
        if (!securityModule.isValidSurname(userSurname).isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Invalid user surname. "+ securityModule.isValidSurname(userSurname) +" Surname:"+ userSurname+"\""+
                    "}"
            );
        }
        if (!securityModule.isValidMail(userMail).isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Invalid mail. "+ securityModule.isValidMail(userMail) +" Email:"+ userMail+"\""+
                    "}"
            );
        }
        if (!securityModule.isValidPassword(userPassword).isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"Invalid password. "+ securityModule.isValidPassword(userPassword) +" Password:"+ userPassword+"\""+
                    "}"
            );
        }

        // ---------- Duplication Checks
        User accountWithThisMail = userRepository.getUserByUserMail(userMail);
        if (accountWithThisMail != null && accountWithThisMail != currentUser) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"This mail is already in use on another account.\""+
                "}"
        );
        User accountWithSameNameAndSurname = userRepository.getUserByUserNameAndUserSurname(userName, userSurname);
        if (accountWithSameNameAndSurname != null && accountWithSameNameAndSurname != currentUser) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                "{"+
                        "\"error\":\"There is already an account with this name and surname.\""+
                "}"
        );


        // ---------- DB Conn
        currentUser.setUserName(userName);
        currentUser.setUserSurname(userSurname);
        currentUser.setUserMail(userMail);
        currentUser.setUserPassword(userPassword);
        currentUser.setUserBalance(userBalance);
        currentUser.setAdmin(isAdmin);
        currentUser.setActive(isActive);
        currentUser.setCreatedAt(createdAt);


        userRepository.save(currentUser);

        return ResponseEntity.ok(currentUser);
    }

    @DeleteMapping("/delete/user")
    public ResponseEntity<Object> deleteUser(
            @RequestParam int ownUserId,
            @RequestParam int userIdWhichIsGonnaBeDeleted
    )
    {
        User thisUser = userRepository.getUserByUserId(ownUserId);
        User userThatIsGoingToBeDeleted = userRepository.getUserByUserId(userIdWhichIsGonnaBeDeleted);
        if (thisUser == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"There is no account with this id.\"" + ownUserId +
                    "}"
            );
        }
        else if (userThatIsGoingToBeDeleted == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"There is no account with this id.\"" + userIdWhichIsGonnaBeDeleted +
                    "}"
            );
        }
        else if (!thisUser.isAdmin())
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    "{"+
                            "\"error\":\"You can't delete an account since you are not an admin.\""+
                    "}"
            );
        }
        else
        {
            userRepository.delete(userThatIsGoingToBeDeleted);
            return ResponseEntity.ok(userThatIsGoingToBeDeleted);
        }
    }

    //          Exception Handler Module
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error! " + ex.getMessage() + "\"}");
    }
}
