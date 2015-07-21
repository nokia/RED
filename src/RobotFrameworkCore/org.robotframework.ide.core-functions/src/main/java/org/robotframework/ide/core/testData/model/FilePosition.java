package org.robotframework.ide.core.testData.model;

public class FilePosition {

    private final int line;
    private final int column;


    public FilePosition(int line, int column) {
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
        return String.format("FilePosition [line=%s, column=%s]", line, column);
    }

}
