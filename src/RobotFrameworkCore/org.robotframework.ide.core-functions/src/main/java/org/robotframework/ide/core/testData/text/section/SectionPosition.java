package org.robotframework.ide.core.testData.text.section;

public class SectionPosition {

    public static final int NOT_SET = -1;
    private int lineNumber = NOT_SET;
    private int columnNumber = NOT_SET;
    private int offset = NOT_SET;


    public int getLineNumber() {
        return lineNumber;
    }


    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public int getColumnNumber() {
        return columnNumber;
    }


    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }


    public int getOffset() {
        return offset;
    }


    public void setOffset(int offset) {
        this.offset = offset;
    }


    public static SectionPosition create(final int lineNumber,
            final int columnNumber, final int offset) {
        SectionPosition sec = SectionPosition.create(lineNumber, columnNumber);
        sec.setOffset(offset);

        return sec;
    }


    public static SectionPosition create(final int lineNumber,
            final int columnNumber) {
        SectionPosition secPos = new SectionPosition();
        secPos.setLineNumber(lineNumber);
        secPos.setColumnNumber(columnNumber);

        return secPos;
    }


    public static SectionPosition copy(final SectionPosition pos) {
        return SectionPosition.create(pos.getLineNumber(),
                pos.getColumnNumber(), pos.getOffset());
    }
}
