package security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BcryptPasswordHasher implements IPasswordHasher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BcryptPasswordHasher.class);
    private final int cost;

    public BcryptPasswordHasher() {
        this.cost = getCost();
    }

    private int getCost() {
        String envCost = System.getenv("BCRYPT_COST");
        try {
            return (envCost != null) ? Integer.parseInt(envCost) : 12;
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid BCRYPT_COST value '{}'. Defaulting to 12.", envCost);
            return 12;
        }
    }

    @Override
    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.withDefaults().hashToString(cost, plainPassword.toCharArray());
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
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
