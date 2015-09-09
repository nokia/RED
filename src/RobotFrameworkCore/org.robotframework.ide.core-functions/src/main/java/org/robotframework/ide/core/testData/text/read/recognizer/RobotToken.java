/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;


public class RobotToken implements IRobotLineElement {

    private FilePosition fp = new FilePosition(NOT_SET, NOT_SET, NOT_SET);
    private StringBuilder raw = new StringBuilder();
    private StringBuilder text = new StringBuilder();
    private List<IRobotTokenType> types = new LinkedList<>();


    public RobotToken() {
        types.add(RobotTokenType.UNKNOWN);
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
        return types;
    }


    public void setType(final IRobotTokenType type) {
        types.clear();
        types.add(type);
    }


    @Override
    public String toString() {
        return String.format("RobotToken [filePosition=%s, text=%s, types=%s]",
                fp, text, types);
    }


    @Override
    public FilePosition getFilePosition() {
        return fp;
    }
}
