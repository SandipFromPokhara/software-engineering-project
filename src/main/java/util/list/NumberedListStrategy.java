package util.list;

// Numbered list strategy
public class NumberedListStrategy implements IListFormattingStrategy {

    @Override
    public boolean hasFormat(String line) {
        return line.trim().matches("^\\d+\\..*");
    }

    @Override
    public String applyFormat(String line, int number) {
        if (line.trim().startsWith("•")) {
            line = line.replaceFirst("^\\s*•\\s*", "");
        }
        return line.trim().isEmpty() ? number + ". " : number + ". " + line.trim();
    }

    @Override
    public String removeFormat(String line) {
        return line.replaceFirst("^\\s*\\d+\\.\\s*", "");
    }
}
