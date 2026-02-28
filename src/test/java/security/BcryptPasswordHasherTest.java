package security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BcryptPasswordHasherTest {
    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();

    @Test
    void testGetCostReturnsExpectedValue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getCostMethod = BcryptPasswordHasher.class.getDeclaredMethod("getCost");
        getCostMethod.setAccessible(true); // allow access to private method

        int cost = (int) getCostMethod.invoke(hasher);
        assertTrue(cost >= 6 && cost <= 31, "Cost should be in valid BCrypt range");
    }

    @Test
    void testHashAndVerify() {
        String password = "Pass123!";
        String hash = hasher.hash(password);

        assertNotNull(hash);
        assertFalse(hash.isBlank());

        // verify correct password
        assertTrue(hasher.verify(password, hash));

        // verify wrong password
        assertFalse(hasher.verify("WrongPass", hash));

        // verify null password
        assertFalse(hasher.verify(null, hash));

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
        String password = "secret123";
        String hash = hasher.hash(password);
        assertTrue(hasher.verify(password, hash));
    }

    @Test
    void testVerifyWrongPassword() {
        String password = "secret123";
        String hash = hasher.hash(password);
        assertFalse(hasher.verify("wrongpass", hash));
    }

    @Test
    void testVerifyNullPassword() {
        String hash = hasher.hash("something");
        assertFalse(hasher.verify(null, hash));
    }

    @Test
    void testVerifyNullHash() {
        assertFalse(hasher.verify("something", null));
    }

    @Test
    void testVerifyEmptyPasswordOrHash() {
        String hash = hasher.hash("something");
        assertFalse(hasher.verify("", hash));
        assertFalse(hasher.verify("something", ""));
    }
}