package util.list;

public interface IListFormattingStrategy {
    boolean hasFormat(String line);

    String applyFormat(String line, int n);

    String removeFormat(String line);
}
