package security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BcryptPasswordHasherTest {
    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();
    private static final String TEST_PASS = "secret123";
    private static final String TEST_HASH_INPUT= "something";

    static class ReflectionAccessException extends RuntimeException {
        public ReflectionAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private int invokeGetCost() {
        try {
            Method method = BcryptPasswordHasher.class.getDeclaredMethod("getCost");
            method.setAccessible(true);
            return (int) method.invoke(hasher);
        } catch (NoSuchMethodException e) {
            throw new ReflectionAccessException("'getCost' method was renamed or removed.", e);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionAccessException("Reflection failed for 'getCost'.", e);
        }
    }

    @Test
    @DisplayName("Internal 'getCost' should return a value within valid BCrypt range (6-31)")
    void testGetCostReturnsExpectedValue() {
        int cost = invokeGetCost();
        assertEquals(12, cost, "Default cost is expected to be 12");
        assertTrue(cost >= 6 && cost <= 31, "Cost must be in valid BCrypt range");
    }

    @Test
    void testHashAndVerify() {
        String hashing = hasher.hash(TEST_PASS);

        assertNotNull(hashing);
        assertFalse(hashing.isBlank());

        // verify correct TEST_PASS
        assertTrue(hasher.verify(TEST_PASS, hashing));

        // verify wrong TEST_PASS
        assertFalse(hasher.verify("WrongPass", hashing));

        // verify null TEST_PASS
        assertFalse(hasher.verify(null, hashing));

        // verify null hash
        assertFalse(hasher.verify(TEST_PASS, null));

        // verify blank hash
        assertFalse(hasher.verify(TEST_PASS, ""));
    }

    @Test
    void testHashNullOrBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash("   "));
    }

    @Test
    void testVerifyValidPassword() {
        String hash = hasher.hash(TEST_PASS);
        assertTrue(hasher.verify(TEST_PASS, hash));
    }

    @Test
    void testVerifyWrongPassword() {
        String hash = hasher.hash(TEST_PASS);
        assertFalse(hasher.verify("wrongpass", hash));
    }

    @Test
    void testVerifyNullPassword() {
       String checkHash = hasher.hash(TEST_HASH_INPUT);
        assertFalse(hasher.verify(null, checkHash));
    }

    @Test
    void testVerifyNullHash() {
        assertFalse(hasher.verify(TEST_HASH_INPUT, null));
    }

    @Test
    void testVerifyEmptyPasswordOrHash() {
        String hashCheck = hasher.hash(TEST_HASH_INPUT);
        assertFalse(hasher.verify("", hashCheck));
        assertFalse(hasher.verify(TEST_HASH_INPUT, ""));
    }
}
