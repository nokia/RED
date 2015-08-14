package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;


public class Separator implements IRobotLineElement {

    private int lineNumber = NOT_SET;
    private int startColumn = NOT_SET;
    private int startOffset = NOT_SET;
    private StringBuilder raw = new StringBuilder();
    private StringBuilder text = new StringBuilder();
    private SeparatorType type = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;

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


    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }


    @Override
    public int getStartOffset() {
        return startOffset;
    }


    @Override
    public int getEndColumn() {
        return startColumn + text.length();
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
        List<IRobotTokenType> s = new LinkedList<>();
        s.add(type);
        return s;
    }


    public void setType(SeparatorType type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return String
                .format("Separator [lineNumber=%s, startColumn=%s, startOffset=%s, text=%s, type=%s]",
                        lineNumber, startColumn, startOffset, text, type);
    }
}
