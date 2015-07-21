package org.robotframework.ide.core.testData.text.read.columnSeparators;

public abstract class ALineSeparator {

    protected final int lineNumber;
    protected final String line;


    protected ALineSeparator(final int lineNumber, final String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }


    public abstract Separator next();


    public abstract boolean hasNext();


    public int getLineNumber() {
        return lineNumber;
    }


    public String getLine() {
        return line;
    }
}
