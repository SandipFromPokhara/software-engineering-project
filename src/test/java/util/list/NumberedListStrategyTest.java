package util.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberedListStrategyTest {

    private NumberedListStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NumberedListStrategy();
    }

    @Test
    void hasFormat_WithNumber_ReturnsTrue() {
        assertTrue(strategy.hasFormat("1. Sample text"));
        assertTrue(strategy.hasFormat("  42. Indented number"));
        assertTrue(strategy.hasFormat("999. Large number"));
        assertTrue(strategy.hasFormat("1.No space after dot"));
    }

    @Test
    void hasFormat_WithoutNumber_ReturnsFalse() {
        assertFalse(strategy.hasFormat("Plain text"));
        assertFalse(strategy.hasFormat("• Bullet only"));
        assertFalse(strategy.hasFormat(""));
        assertFalse(strategy.hasFormat("Not a 1. number"));
    }

    @Test
    void applyFormat_PlainText_AddsNumber() {
        assertEquals("1. Hello", strategy.applyFormat("Hello", 1));
        assertEquals("5. World", strategy.applyFormat("  World  ", 5));
        assertEquals("42. Test", strategy.applyFormat("Test", 42));
    }

    @Test
    void applyFormat_EmptyText_AddsNumberWithSpace() {
        assertEquals("1. ", strategy.applyFormat("", 1));
        assertEquals("10. ", strategy.applyFormat("   ", 10));
    }

    @Test
    void applyFormat_BulletText_RemovesBulletAndAddsNumber() {
        assertEquals("1. Item", strategy.applyFormat("• Item", 1));
        assertEquals("3. Text", strategy.applyFormat("  • Text", 3));
        assertEquals("5. 1. • Content", strategy.applyFormat("1. • Content", 5));
    }

    @Test
    void applyFormat_UsesProvidedNumber() {
        assertEquals("1. Text", strategy.applyFormat("Text", 1));
        assertEquals("2. Text", strategy.applyFormat("Text", 2));
        assertEquals("100. Text", strategy.applyFormat("Text", 100));
    }

    @Test
    void removeFormat_NumberedText_RemovesNumber() {
        assertEquals("Sample text", strategy.removeFormat("1. Sample text"));
        assertEquals("Text", strategy.removeFormat("  5. Text"));
        assertEquals("Item", strategy.removeFormat("42. Item"));
    }

    @Test
    void removeFormat_PlainText_ReturnsUnchanged() {
        assertEquals("Plain text", strategy.removeFormat("Plain text"));
        assertEquals("• Bullet", strategy.removeFormat("• Bullet"));
    }

    @Test
    void removeFormat_NumberWithoutSpace_RemovesNumberAndDot() {
        assertEquals("Text", strategy.removeFormat("1.Text"));
        assertEquals("Content", strategy.removeFormat("  10.Content"));
    }

    @Test
    void removeFormat_MultipleNumbers_RemovesOnlyFirst() {
        assertEquals("First 2. Second", strategy.removeFormat("1. First 2. Second"));
    }

    @Test
    void removeFormat_EmptyString_ReturnsEmpty() {
        assertEquals("", strategy.removeFormat(""));
    }
}
