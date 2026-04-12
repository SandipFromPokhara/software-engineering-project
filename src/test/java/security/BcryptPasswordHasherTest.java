package security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BcryptPasswordHasherTest {
    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();
    private final String password = "secret123";
    private final String hash = "something";


    private int invokeGetCost() {
        try {
            Method method = BcryptPasswordHasher.class.getDeclaredMethod("getCost");
            method.setAccessible(true);
            return (int) method.invoke(hasher);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Refactor Error: 'getCost' method was renamed or removed in BcryptPasswordHasher.", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Reflection failed for 'getCost'. Check JVM security settings.", e);
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
        String hashing = hasher.hash(password);

        assertNotNull(hashing);
        assertFalse(hashing.isBlank());

        // verify correct password
        assertTrue(hasher.verify(password, hashing));

        // verify wrong password
        assertFalse(hasher.verify("WrongPass", hashing));

        // verify null password
        assertFalse(hasher.verify(null, hashing));

        // verify null hash
        assertFalse(hasher.verify(password, null));

        // verify blank hash
        assertFalse(hasher.verify(password, ""));
    }

    @Test
    void testHashNullOrBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> hasher.hash("   "));
    }

    @Test
    void testVerifyValidPassword() {
        String hash = hasher.hash(password);
        assertTrue(hasher.verify(password, hash));
    }

    @Test
    void testVerifyWrongPassword() {
        String hash = hasher.hash(password);
        assertFalse(hasher.verify("wrongpass", hash));
    }

    @Test
    void testVerifyNullPassword() {
       hasher.hash(hash);
        assertFalse(hasher.verify(null, hash));
    }

    @Test
    void testVerifyNullHash() {
        assertFalse(hasher.verify(hash, null));
    }

    @Test
    void testVerifyEmptyPasswordOrHash() {
        hasher.hash(hash);
        assertFalse(hasher.verify("", hash));
        assertFalse(hasher.verify(hash, ""));
    }
}
