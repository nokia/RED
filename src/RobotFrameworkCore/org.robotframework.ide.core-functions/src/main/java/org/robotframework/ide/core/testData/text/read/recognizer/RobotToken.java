package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;


public class RobotToken implements IRobotLineElement {

    private int lineNumber = NOT_SET;
    private int startColumn = NOT_SET;
    private StringBuilder text = new StringBuilder();
    private List<IRobotTokenType> types = new LinkedList<>();


    public RobotToken() {
        types.add(RobotTokenType.UNKNOWN);
    }


    @Override
    public int getLineNumber() {
        return lineNumber;
    }


    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    @Override
    public int getStartColumn() {
        return startColumn;
    }


    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }


    @Override
    public StringBuilder getText() {
        return text;
    }


    public void setText(StringBuilder text) {
        this.text = text;
    }


    @Override
    public int getEndColumn() {
        return startColumn + text.length();
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
        return String
                .format("RobotToken [lineNumber=%s, startColumn=%s, text=%s, types=%s]",
                        lineNumber, startColumn, text, types);
    }

}
