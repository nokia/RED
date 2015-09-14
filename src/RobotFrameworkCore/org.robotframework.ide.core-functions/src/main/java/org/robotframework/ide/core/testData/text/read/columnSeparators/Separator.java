/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;


public class Separator implements IRobotLineElement {

    private FilePosition fp = new FilePosition(NOT_SET, NOT_SET, NOT_SET);
    private StringBuilder raw = new StringBuilder();
    private StringBuilder text = new StringBuilder();
    private SeparatorType type = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
    private boolean isDirty = false;
    private boolean wasFirstInit = false;

    public static enum SeparatorType implements IRobotTokenType {
        TABULATOR_OR_DOUBLE_SPACE("\t", "  "), PIPE("| ", " | ", "\t|", "|\t",
                "\t|\t");

        private final List<String> representationForNew = new LinkedList<>();


        public List<String> getRepresentation() {
            return representationForNew;
        }


        private SeparatorType(String... representation) {
            representationForNew.addAll(Arrays.asList(representation));
        }
    }


    @Override
    public int getLineNumber() {
        return fp.getLine();
    }


    public void setLineNumber(int lineNumber) {
        fp = new FilePosition(lineNumber, fp.getColumn(), fp.getOffset());
    }


    @Override
    public int getStartColumn() {
        return fp.getColumn();
    }


    public void setStartColumn(int startColumn) {
        fp = new FilePosition(fp.getLine(), startColumn, fp.getOffset());
    }


    @Override
    public int getEndColumn() {
        int endColumn = NOT_SET;

        if (fp.getColumn() != NOT_SET) {
            endColumn = fp.getColumn() + text.length();
        }

        return endColumn;
    }


    public void setStartOffset(int startOffset) {
        fp = new FilePosition(fp.getLine(), fp.getColumn(), startOffset);
    }


    @Override
    public int getStartOffset() {
        return fp.getOffset();
    }


    @Override
    public StringBuilder getText() {
        return text;
    }


    public void setText(StringBuilder text) {
        if (wasFirstInit) {
            isDirty = true;
        } else {
            wasFirstInit = true;
        }
        this.text = text;
    }


    @Override
    public StringBuilder getRaw() {
        return raw;
    }


    public void setRaw(StringBuilder raw) {
        this.raw = raw;
    }


    @Override
    public List<IRobotTokenType> getTypes() {
        List<IRobotTokenType> s = new LinkedList<>();
        s.add(type);
        return s;
    }


    public void setType(SeparatorType type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return String.format("Separator [filePos=%s, text=%s, type=%s]", fp,
                text, type);
    }


    @Override
    public FilePosition getFilePosition() {
        return fp;
    }


    @Override
    public boolean isDirty() {
        return isDirty;
    }
}
