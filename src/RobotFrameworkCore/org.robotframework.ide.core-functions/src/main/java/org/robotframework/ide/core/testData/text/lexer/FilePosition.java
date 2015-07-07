package org.robotframework.ide.core.testData.text.lexer;

/**
 * Marks position in file as line plus column. The first line should start from
 * one. The first column should also start from one.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class FilePosition {

    /**
     * Represents the first line number
     */
    public static final int THE_FIRST_LINE = 1;
    /**
     * Represents the first column number
     */
    public static final int THE_FIRST_COLUMN = 1;
    private final int line;
    private final int column;


    public FilePosition(final int line, final int column) {
        this.line = line;
        this.column = column;
    }


    public int getLine() {
        return line;
    }


    public int getColumn() {
        return column;
    }


    public static FilePosition createMarkerForFirstLineAndColumn() {
        return new FilePosition(THE_FIRST_LINE, THE_FIRST_COLUMN);
    }


    public static FilePosition createMarkerForFirstLine(final int column) {
        return new FilePosition(THE_FIRST_LINE, column);
    }


    public static FilePosition createMarkerForFirstColumn(final int line) {
        return new FilePosition(line, THE_FIRST_COLUMN);
    }


    @Override
    public String toString() {
        return String.format("[line=%s, column=%s]", line, column);
    }
}
