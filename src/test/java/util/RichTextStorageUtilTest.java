package util;

import org.junit.jupiter.api.Test;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import static org.junit.jupiter.api.Assertions.*;

class RichTextStorageUtilTest {

    @Test
    void serialize_WithNullSpans_ReturnsRawText() {
        String result = RichTextStorageUtil.serialize("Sample", null);
        assertEquals("Sample", result);
    }

    @Test
    void serializeAndDecode_RoundTripsContent() {
        String originalText = "Hello Bold World";
        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        builder.add("", 6); // "Hello "
        builder.add("-fx-font-weight: bold;", 4); // "Bold"
        builder.add("", 6); // " World"

        StyleSpans<String> spans = builder.create();

        String serialized = RichTextStorageUtil.serialize(originalText, spans);
        assertNotNull(serialized);
        assertTrue(serialized.startsWith("RTF1"));

        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(serialized);

        assertEquals(originalText, decoded.text());
        assertNotNull(decoded.spans());
        assertEquals(3, decoded.spans().getSpanCount());
        assertEquals("", decoded.spans().getStyleSpan(0).getStyle());
        assertEquals("-fx-font-weight: bold;", decoded.spans().getStyleSpan(1).getStyle());
    }

    @Test
    void decode_WithNullOrBlank_ReturnsEmptyContent() {
        RichTextStorageUtil.DecodedContent decodedNull = RichTextStorageUtil.decode(null);
        assertEquals("", decodedNull.text());
        assertNull(decodedNull.spans());

        RichTextStorageUtil.DecodedContent decodedEmpty = RichTextStorageUtil.decode("   ");
        assertEquals("", decodedEmpty.text());
        assertNull(decodedEmpty.spans());
    }

    @Test
    void decode_WithoutPrefix_ReturnsRawTextAndNullSpans() {
        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode("Plain text");
        assertEquals("Plain text", decoded.text());
        assertNull(decoded.spans());
    }

    @Test
    void toPlainText_WithValidSerializedString_ReturnsDecodedText() {
        String originalText = "Hello";
        StyleSpans<String> spans = new StyleSpansBuilder<String>().add("", 5).create();
        String serialized = RichTextStorageUtil.serialize(originalText, spans);

        String plain = RichTextStorageUtil.toPlainText(serialized);
        assertEquals(originalText, plain);
    }

    @Test
    void toPlainText_WithoutPrefix_ReturnsRawText() {
        String plain = RichTextStorageUtil.toPlainText("Just text");
        assertEquals("Just text", plain);
    }
}