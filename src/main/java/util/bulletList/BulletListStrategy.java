package util.bulletList;

// Bullet strategy
public class BulletListStrategy implements IListFormattingStrategy {

    @Override
    public boolean hasFormat(String line) {
        return line.trim().startsWith("• ");
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
