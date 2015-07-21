package org.robotframework.ide.core.testData.text.read.columnSeparators;

public class Separator {

    public static final int NOT_SET = -1;
    private int lineNumber = NOT_SET;
    private int startColumn = NOT_SET;
    private StringBuilder text = new StringBuilder();
    private SeparatorType type = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;

    public static enum SeparatorType {
        TABULATOR_OR_DOUBLE_SPACE, PIPE
    }


    public int getLineNumber() {
        return lineNumber;
    }


    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public int getStartColumn() {
        return startColumn;
    }


    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }


    public int getEndColumn() {
        return startColumn + text.length();
    }


    public StringBuilder getText() {
        return text;
    }


    public void setText(StringBuilder text) {
        this.text = text;
    }


    public SeparatorType getType() {
        return type;
    }


    public void setType(SeparatorType type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return String.format(
                "Separator [lineNumber=%s, startColumn=%s, text=%s, type=%s]",
                lineNumber, startColumn, text, type);
    }
}
