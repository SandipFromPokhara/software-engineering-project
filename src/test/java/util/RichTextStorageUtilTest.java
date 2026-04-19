package util;

import org.junit.jupiter.api.Test;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Base64;

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
    // Test that encoding null text results in an empty string in the serialized output
    @Test
    void serialize_WithNullText_ReturnsEmptyEncodedText() {
        StyleSpans<String> spans = new StyleSpansBuilder<String>()
                .add("", 0)
                .create();

        String result = RichTextStorageUtil.serialize(null, spans);

        assertTrue(result.startsWith("RTF1|"));
    }
    //added test to verify that encoding a null style results in an empty string for the style in the serialized output
    @Test
    void serialize_WithNullStyle_EncodesAsEmptyString() {
        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        builder.add(null, 5);

        String serialized = RichTextStorageUtil.serialize("Hello", builder.create());

        assertNotNull(serialized);
        assertTrue(serialized.contains(":")); // ensures run was written
    }
    @Test
    void decode_WithEmptyRunsPart_ReturnsNullSpans() {
        String textEncoded = Base64.getEncoder().encodeToString("Hello".getBytes());

        String stored = "RTF1|" + textEncoded + "|";

        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(stored);

        assertEquals("Hello", decoded.text());
        assertNull(decoded.spans());
    }
    @Test
    void decode_WithPartiallyValidRuns_ProcessesValidOnesOnly() {
        String textEncoded = Base64.getEncoder().encodeToString("Hello".getBytes());
        String styleEncoded = Base64.getEncoder().encodeToString("bold".getBytes());

        String stored = "RTF1|" + textEncoded + "|5:" + styleEncoded + ";badRun";

        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(stored);

        assertEquals(1, decoded.spans().getSpanCount());
    }
    @Test
    void serializeAndDecode_EmptyString() {
        StyleSpans<String> spans = new StyleSpansBuilder<String>()
                .add("", 0)
                .create();

        String serialized = RichTextStorageUtil.serialize("", spans);
        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(serialized);

        assertEquals("", decoded.text());
    }
    @Test
    void decode_WithInvalidBase64Text_ReturnsEmptyText() {
        String stored = "RTF1|%%%INVALID%%%|";

        RichTextStorageUtil.DecodedContent decoded = RichTextStorageUtil.decode(stored);

        assertEquals("", decoded.text());
    }
}