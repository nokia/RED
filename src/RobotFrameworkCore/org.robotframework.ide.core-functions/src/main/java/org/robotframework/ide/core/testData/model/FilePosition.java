package org.robotframework.ide.core.testData.model;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;


public class FilePosition {

    private final int line;
    private final int column;
    private final int offset;


    public FilePosition(int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.offset = offset;
    }


    public int getLine() {
        return line;
    }


    public int getColumn() {
        return column;
    }


    public int getOffset() {
        return offset;
    }


    @Override
    public String toString() {
        return String.format("FilePosition [line=%s, column=%s, offset=%s]",
                line, column, offset);
    }


    public static FilePosition createNotSet() {
        int NOT_SET = IRobotLineElement.NOT_SET;
        return new FilePosition(NOT_SET, NOT_SET, NOT_SET);
    }
}
