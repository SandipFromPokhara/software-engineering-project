package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

//Utility class for hashing passwords
public class PasswordHasher {

    //Hashes a password using SHA-256 algorithm
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    //Verifies a password against a hashed password
    public static boolean verifyPassword(String password, String hashedPassword) {
        String newHash = hashPassword(password);
        return newHash.equals(hashedPassword);
    }
}
