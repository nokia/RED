package org.robotframework.ide.core.testData.model;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;


public class FilePosition {

    private static final int LESS_THAN = -1;
    private static final int EQUAL = 0;
    private static final int GREATER_THAN = 1;
    private static final int COMPARE_NOT_SET = -2;

    public static final int NOT_SET = IRobotLineElement.NOT_SET;
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
        return new FilePosition(NOT_SET, NOT_SET, NOT_SET);
    }


    public boolean isBefore(FilePosition other) {
        return (compare(other) == LESS_THAN);
    }


    public boolean isAfter(FilePosition other) {
        return (compare(other) == GREATER_THAN);
    }


    public boolean isSamePlace(FilePosition other) {
        return (compare(other) == EQUAL);
    }


    public int compare(FilePosition other) {
        int result;
        if (other != null) {
            int otherOffset = other.getOffset();
            int otherLine = other.getLine();
            int otherColumn = other.getColumn();

            result = compare(offset, otherOffset);
            if (result == COMPARE_NOT_SET) {
                result = compare(line, otherLine);
            }
            if (result == COMPARE_NOT_SET) {
                result = compare(column, otherColumn);
            }

            if (result == COMPARE_NOT_SET) {
                result = EQUAL;
            }
        } else {
            result = GREATER_THAN;
        }

        return result;
    }


    private int compare(int valuePosO1, int valuePosO2) {
        int result;
        if (valuePosO1 != NOT_SET && valuePosO2 != NOT_SET) {
            result = Integer.compare(valuePosO1, valuePosO2);
        } else if (valuePosO1 != NOT_SET) {
            result = GREATER_THAN;
        } else if (valuePosO2 != NOT_SET) {
            result = LESS_THAN;
        } else {
            result = COMPARE_NOT_SET;
        }

        return result;
    }
}
