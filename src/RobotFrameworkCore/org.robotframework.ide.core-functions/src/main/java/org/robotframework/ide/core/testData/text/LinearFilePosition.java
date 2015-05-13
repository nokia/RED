package org.robotframework.ide.core.testData.text;

public class LinearFilePosition {

    private final int line;
    private final int column;


    public LinearFilePosition(int line, int column) {
        this.line = line;
        this.column = column;
    }


    public int getLine() {
        return line;
    }


    public int getColumn() {
        return column;
    }


    @Override
    public String toString() {
        return String.format("[line=%s, column=%s]", line, column);
    }
}
