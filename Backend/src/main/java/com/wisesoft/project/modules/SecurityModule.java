package com.wisesoft.project.modules;

import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SecurityModule {
    // List of dangerous patterns to check for injection attacks
    private static final String[] INJECTION_PATTERNS = {
            // SQL Injection patterns
            "('.+--)|(--)|(\\|)|(%7C)",     // SQL comments
            "([\\\"\\\'][\\s]*((\\bor\\b|\\band\\b))\\s*?[\\\"\\\'].*=)", // OR/AND clauses
            "database","drop","insert","select","alter","table",

            // XSS Injection patterns
            "(<script>(.*?)</script>)", // Simple script tags
            "((<|&lt;)script(>|&gt;).*(<|&lt;)/script(>|&gt;))", // Script tags with escaped characters
            "(<[^>]+(on\\w+|style|xmlns)[^>]*>)", // HTML tags with event handlers or style attributes
            "((<|%3C)[^\\n]+(>|%3E))", // HTML tags encoded as %3C and %3E

            // Email Header Injection patterns
            "\\r|\\n", // Carriage return and new line
            "%0a|%0d", // URL encoded carriage return and new line
            "Content-Type:", "bcc:", "to:", "cc:", "subject:", "from:", "reply-to:", "message-id:", "received:", "return-path:"
    };
    // Regular expression for validating email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
    public String isValidMail(String email)
    {
        if (email == null || email.isEmpty()) {
            return "It is empty.";
        }

        // Validate email format
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            return "Doesn't match the email pattern.";
        }

        // Check for injection patterns
        for (String pattern : INJECTION_PATTERNS) {
            if (email.contains(pattern)) {
                return "Has injection patterns.";
            }
        }

        // Validate the domain part has MX record
        String domain = email.substring(email.indexOf('@') + 1);

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            if (attrs.get("MX") != null) return "";
            else return "There is no MX (Mail Exchange) record for this domain.";

        } catch (NamingException e) {
            return "Error while trying to get MX records.";
        }
    }

    // Password Validation

    // Regular expression for validating password complexity
    private static final String PASSWORD_REGEX =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!\"#$%&'()*+,-./:;<=>?@^_`{|}~])(?=\\S+$).{6,20}$";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    // Set of common passwords (for demonstration purposes, use a larger set in production)
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>();

    static {
        COMMON_PASSWORDS.add("123456");
        COMMON_PASSWORDS.add("password");
        COMMON_PASSWORDS.add("123456789");
        COMMON_PASSWORDS.add("12345678");
        COMMON_PASSWORDS.add("1234567");
        COMMON_PASSWORDS.add("1234567890");
        COMMON_PASSWORDS.add("qwerty");
        COMMON_PASSWORDS.add("abc123");
        COMMON_PASSWORDS.add("password1");
        COMMON_PASSWORDS.add("password12");
        COMMON_PASSWORDS.add("password123");
    }

    // Function to validate password
    public String isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password can't be empty";
        }

        // Validate password complexity
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        if (!matcher.matches()) {
            return "Password doesn't match password pattern";
        }

        // Check against common passwords
        if (COMMON_PASSWORDS.contains(password)) return "Password is common";

        return "";
    }

    // Name Validation
    private static final String NAME_REGEX = "^[\\p{L} .'-]{3,64}$";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    public String isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return "Name can't be empty";
        }

        // Validate name format
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return "Name must be between 3 and 64 characters and can only contain letters, spaces, dots, apostrophes, and hyphens.";
        }

        return "";
    }

    // Surname Validation
    private static final String SURNAME_REGEX = "^[\\p{L} .'-]{3,64}$";
    private static final Pattern SURNAME_PATTERN = Pattern.compile(SURNAME_REGEX);

    public String isValidSurname(String surname) {
        if (surname == null || surname.isEmpty()) {
            return "Surname can't be empty";
        }

        // Validate surname format
        Matcher matcher = SURNAME_PATTERN.matcher(surname);
        if (!matcher.matches()) {
            return "Surname must be between 3 and 64 characters and can only contain letters, spaces, dots, apostrophes, and hyphens.";
        }

        return "";
    }

    public boolean hasInjectionPattern(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (String pattern : INJECTION_PATTERNS) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(input).find()) {
                return true;
            } else if (input.toLowerCase().contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}
