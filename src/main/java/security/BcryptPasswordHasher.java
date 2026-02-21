package util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BcryptPasswordHasher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BcryptPasswordHasher.class);
    private static final int BCRYPT_COST = getCost();

    private BcryptPasswordHasher() {
    }

    private static int getCost() {
        String envCost = System.getenv("BCRYPT_COST");
        try {
            return (envCost != null) ? Integer.parseInt(envCost) : 12;
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid BCRYPT_COST value '{}'. Defaulting to 12.", envCost);
            return 12;
        }
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
            return result.verified;
        } catch (Exception e) {
            LOGGER.error("BCrypt verification failed due to malformed hash format", e);
            return false;
        }
    }
}
