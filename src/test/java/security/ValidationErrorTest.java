package security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorTest {

    @Test
    void validationError_equals_hashcode_and_toString() {
        Validation.ValidationError a = new Validation.ValidationError("key", new Object[]{"v"});
        Validation.ValidationError b = new Validation.ValidationError("key", new Object[]{"v"});
        Validation.ValidationError c = new Validation.ValidationError("other", new Object[]{"v"});

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertTrue(a.toString().contains("key"));
    }

    @Test
    void validationError_args_null_returnsEmptyArray() {
        Validation.ValidationError ve = new Validation.ValidationError("k", null);
        assertNotNull(ve.args());
        assertEquals(0, ve.args().length);
    }
}

