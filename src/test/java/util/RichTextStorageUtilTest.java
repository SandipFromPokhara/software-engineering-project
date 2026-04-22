package util;


import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RichTextStorageUtilTest {

    @Test
    void serialize_nullSpans_returnsPlainText() {
        String sample = "Hello World";
        String stored = RichTextStorageUtil.serialize(sample, null);
        assertEquals(sample, stored);
    }

    @Test
    void serialize_and_decode_roundtrip_withRuns() {
        String sample = "abc";
        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        builder.add("bold", 1);
        builder.add("", 2);
        StyleSpans<String> spans = builder.create();

        String stored = RichTextStorageUtil.serialize(sample, spans);
        assertNotNull(stored);
        assertTrue(stored.startsWith("RTF1|"));

        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(stored);
        assertEquals(sample, decoded.text());
        assertNotNull(decoded.spans());
    }

    @Test
    void decode_nullOrBlank_returnsEmptyContent() {
        RichTextStorageUtil.DecodedContent d1 = RichTextStorageUtil.decode(null);
        assertEquals("", d1.text());
        assertNull(d1.spans());

        RichTextStorageUtil.DecodedContent d2 = RichTextStorageUtil.decode("");
        assertEquals("", d2.text());
        assertNull(d2.spans());
    }

    @Test
    void decode_withoutPrefix_returnsOriginalText() {
        String plain = "just plain";
        RichTextStorageUtil.DecodedContent d = RichTextStorageUtil.decode(plain);
        assertEquals(plain, d.text());
        assertNull(d.spans());
    }


    @Test
    void toPlainText_usesDecodeText() {
        String sample = "Hello";
        String stored = RichTextStorageUtil.serialize(sample, null);
        assertEquals(sample, RichTextStorageUtil.toPlainText(stored));
    }
}

