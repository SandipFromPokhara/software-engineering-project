package util.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletListStrategyTest {

    private BulletListStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BulletListStrategy();
    }

    @Test
    void hasFormatWithBulletReturnsTrue() {
        assertTrue(strategy.hasFormat("• Sample text"));
        assertTrue(strategy.hasFormat("  • Indented bullet"));
        assertFalse(strategy.hasFormat("1. • Combined format"));
    }

    @Test
    void hasFormatWithoutBulletReturnsFalse() {
        assertFalse(strategy.hasFormat("Plain text"));
        assertFalse(strategy.hasFormat("1. Numbered only"));
        assertFalse(strategy.hasFormat(""));
    }

    @Test
    void applyFormatPlainTextAddsBullet() {
        assertEquals("• Hello", strategy.applyFormat("Hello", 1));
        assertEquals("• World", strategy.applyFormat("  World  ", 1));
    }

    @Test
    void applyFormatEmptyTextAddsBulletWithSpace() {
        assertEquals("• ", strategy.applyFormat("", 1));
        assertEquals("• ", strategy.applyFormat("   ", 1));
    }

    @Test
    void applyFormatNumberedTextRemovesNumberAndAddsBullet() {
        assertEquals("• Item", strategy.applyFormat("1. Item", 1));
        assertEquals("• Text", strategy.applyFormat("  5. Text", 1));
        assertEquals("• Content", strategy.applyFormat("42. Content", 1));
    }

    @Test
    void applyFormatIgnoresNumberParameter() {
        assertEquals("• Text", strategy.applyFormat("Text", 1));
        assertEquals("• Text", strategy.applyFormat("Text", 99));
        assertEquals("• Text", strategy.applyFormat("Text", -1));
    }

    @Test
    void removeFormatBulletTextRemovesBullet() {
        assertEquals("Sample text", strategy.removeFormat("• Sample text"));
        assertEquals("Text", strategy.removeFormat("  • Text"));
        assertEquals("1. • Item", strategy.removeFormat("1. • Item"));
    }

    @Test
    void removeFormatPlainTextReturnsUnchanged() {
        assertEquals("Plain text", strategy.removeFormat("Plain text"));
        assertEquals("1. Numbered", strategy.removeFormat("1. Numbered"));
    }

    @Test
    void removeFormatMultipleBulletsRemovesOnlyFirst() {
        assertEquals("First • Second", strategy.removeFormat("• First • Second"));
    }

    @Test
    void removeFormatEmptyStringReturnsEmpty() {
        assertEquals("", strategy.removeFormat(""));
    }
}
