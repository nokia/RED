/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import org.rf.ide.core.testdata.model.table.ECompareResult;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;

public class FilePosition {

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
        return String.format("FilePosition [line=%s, column=%s, offset=%s]", line, column, offset);
    }

    public static FilePosition createNotSet() {
        return new FilePosition(NOT_SET, NOT_SET, NOT_SET);
    }

    public boolean isNotSet() {
        return (getLine() == NOT_SET && getColumn() == NOT_SET && getOffset() == NOT_SET);
    }

    public boolean isBefore(FilePosition other) {
        return (compare(other) == ECompareResult.LESS_THAN.getValue());
    }

    public boolean isAfter(FilePosition other) {
        return (compare(other) == ECompareResult.GREATER_THAN.getValue());
    }

    public boolean isSamePlace(FilePosition other) {
        return (compare(other) == ECompareResult.EQUAL_TO.getValue());
    }

    public int compare(FilePosition other) {
        return compare(other, true);
    }

    public int compare(FilePosition other, boolean shouldCheckOffset) {
        ECompareResult result;
        if (other != null) {
            int otherOffset = other.getOffset();
            int otherLine = other.getLine();
            int otherColumn = other.getColumn();

            if (shouldCheckOffset) {
                result = compare(offset, otherOffset);
            } else {
                result = ECompareResult.COMPARE_NOT_SET;
            }

            if (result == ECompareResult.COMPARE_NOT_SET || result == ECompareResult.EQUAL_TO) {
                result = compare(line, otherLine);
            }
            if (result == ECompareResult.COMPARE_NOT_SET || result == ECompareResult.EQUAL_TO) {
                result = compare(column, otherColumn);
            }

            if (result == ECompareResult.COMPARE_NOT_SET) {
                result = ECompareResult.EQUAL_TO;
            }
        } else {
            result = ECompareResult.GREATER_THAN;
        }

        return result.getValue();
    }

    private ECompareResult compare(int valuePosO1, int valuePosO2) {
        ECompareResult result;
        if (valuePosO1 != NOT_SET && valuePosO2 != NOT_SET) {
            result = ECompareResult.map(Integer.compare(valuePosO1, valuePosO2));
        } else if (valuePosO1 != NOT_SET) {
            result = ECompareResult.LESS_THAN;
        } else if (valuePosO2 != NOT_SET) {
            result = ECompareResult.GREATER_THAN;
        } else {
            result = ECompareResult.COMPARE_NOT_SET;
        }

        return result;
    }
}
