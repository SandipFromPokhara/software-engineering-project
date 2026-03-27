package util.bulletList;

// Numbered list strategy
public class NumberedListStrategy implements ListFormattingStrategy {

    @Override
    public boolean hasFormat(String line) {
        return line.trim().matches("^\\d+\\..*");
    }

    @Override
    public String applyFormat(String line, int number) {
        if (line.contains("•")) {
            line = line.replaceFirst("^.*?•\\s*", ""); // remove bullets first
        }
        return line.trim().isEmpty() ? number + ". " : number + ". " + line.trim();
    }

    @Override
    public String removeFormat(String line) {
        return line.replaceFirst("^\\s*\\d+\\.\\s*", "");
    }
}