package org.robotframework.ide.core.testData.text;

/**
 * Represents position as point {@code (x;y)} in file, where: {@code x} is line
 * number and {@code y} is column.
 * 
 * @author wypych
 * @since Robot Framework 2.9.1-alpha
 * @since JDK 1.7 update 74
 */
public class LinearPosition {

    private final int line;
    private final int column;


    public LinearPosition() {
        this(-1, -1);
    }


    public LinearPosition(final int line, final int column) {
        this.line = line;
        this.column = column;
    }


    public int getLine() {
        return line;
    }


    public int getColumn() {
        return column;
    }


    public String toString() {
        return String.format("[line=%s, column=%s]", line, column);
    }
}
