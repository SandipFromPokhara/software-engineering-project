package util;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class RichTextStorageUtil {
    private static final String PREFIX = "RTF1|";

    private RichTextStorageUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String serialize(String text, StyleSpans<String> spans) {
        String safeText = text == null ? "" : text;
        if (spans == null) {
            return safeText;
        }

        StringBuilder runs = new StringBuilder();
        for (StyleSpan<String> span : spans) {
            if (!runs.isEmpty()) {
                runs.append(';');
            }
            String style = span.getStyle() == null ? "" : span.getStyle();
            runs.append(span.getLength()).append(':').append(encodeBase64(style));
        }

        return PREFIX + encodeBase64(safeText) + "|" + runs;
    }

    public static DecodedContent decode(String stored) {
        if (stored == null || stored.isBlank()) {
            return new DecodedContent("", null);
        }
        if (!stored.startsWith(PREFIX)) {
            return new DecodedContent(stored, null);
        }

        String payload = stored.substring(PREFIX.length());
        String[] parts = payload.split("\\|", 2);
        if (parts.length == 0) {
            return new DecodedContent("", null);
        }

        String text = decodeBase64(parts[0]);
        String runsPart = parts.length > 1 ? parts[1] : "";
        if (runsPart.isBlank()) {
            return new DecodedContent(text, null);
        }

        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        String[] runs = runsPart.split(";");
        for (String run : runs) {
            addRunIfValid(builder, run);
        }

        return new DecodedContent(text, builder.create());
    }

    private static void addRunIfValid(StyleSpansBuilder<String> builder, String run) {
        if (run == null || run.isBlank()) {
            return;
        }
        String[] pieces = run.split(":", 2);
        if (pieces.length < 2) {
            return;
        }
        try {
            int length = Integer.parseInt(pieces[0]);
            String style = decodeBase64(pieces[1]);
            builder.add(style, length);
        } catch (NumberFormatException ignored) {
            // Skip malformed length entries.
        }
    }

    public static String toPlainText(String stored) {
        return decode(stored).text();
    }

    private static String encodeBase64(String value) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String decodeBase64(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    public record DecodedContent(String text, StyleSpans<String> spans) {}
}
