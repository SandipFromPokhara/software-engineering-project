package util.list;

// Bullet strategy
public class BulletListStrategy implements IListFormattingStrategy {

    @Override
    public boolean hasFormat(String line) {
        // Consider a line formatted as a bullet if it starts with the bullet character
        // (with or without trailing space). Using trim() removes surrounding whitespace
        // so both "•" and "• " are correctly detected.
        return line.trim().startsWith("•");
    }

    @Override
    public String applyFormat(String line, int number) {
        line = line.replaceFirst("^\\s*\\d+\\.\\s*", ""); // remove numbers first
        return line.trim().isEmpty() ? "• " : "• " + line.trim();
    }

    @Override
    public String removeFormat(String line) {
        return line.replaceFirst("^\\s*•\\s*", "");
    }
}
