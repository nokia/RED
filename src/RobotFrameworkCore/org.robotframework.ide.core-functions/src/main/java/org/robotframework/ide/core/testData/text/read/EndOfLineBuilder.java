/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.text.read.LineReader.Constant;


public class EndOfLineBuilder {

    private List<Constant> constant = new LinkedList<>();
    private int lineNumber = IRobotLineElement.NOT_SET;
    private int startColumn = IRobotLineElement.NOT_SET;
    private int startOffset = IRobotLineElement.NOT_SET;


    public static EndOfLineBuilder newInstance() {
        return new EndOfLineBuilder();
    }


    public EndOfLineBuilder setEndOfLines(final List<Constant> constant) {
        if (constant == null) {
            this.constant = new LinkedList<>();
        } else {
            this.constant.clear();
            this.constant.addAll(constant);
        }

        return this;
    }


    public EndOfLineBuilder setLineNumber(final int lineNumber) {
        if (lineNumber > IRobotLineElement.NOT_SET) {
            this.lineNumber = lineNumber;
        } else {
            this.lineNumber = IRobotLineElement.NOT_SET;
        }

        return this;
    }


    public EndOfLineBuilder setStartColumn(final int startColumn) {
        if (startColumn > IRobotLineElement.NOT_SET) {
            this.startColumn = startColumn;
        } else {
            this.startColumn = IRobotLineElement.NOT_SET;
        }

        return this;
    }


    public EndOfLineBuilder setStartOffset(final int startOffset) {
        if (startOffset > IRobotLineElement.NOT_SET) {
            this.startOffset = startOffset;
        } else {
            this.startOffset = IRobotLineElement.NOT_SET;
        }

        return this;
    }


    public IRobotLineElement buildEOL() {
        IRobotLineElement eol = null;
        if (constant.size() == 1) {
            Constant myEol = constant.get(0);
            if (myEol == Constant.CR) {
                eol = new CarritageReturnEndOfLine(startOffset, lineNumber,
                        startColumn);
            } else if (myEol == Constant.LF) {
                eol = new LineFeedEndOfLine(startOffset, lineNumber,
                        startColumn);
            } else if (myEol == Constant.EOF) {
                eol = new EndOfFile(startOffset, lineNumber, startColumn);
            }
        } else if (constant.size() == 2) {
            Constant myEol1 = constant.get(0);
            Constant myEol2 = constant.get(1);

            if (myEol1 == Constant.CR && myEol2 == Constant.LF) {
                eol = new CRLFEndOfLine(startOffset, lineNumber, startColumn);
            } else if (myEol1 == Constant.LF && myEol2 == Constant.CR) {
                eol = new LFCREndOfLine(startOffset, lineNumber, startColumn);
            }
        }

        if (eol == null) {
            eol = new UndeclaredEndOfLine(startOffset, lineNumber, startColumn);
        }

        return eol;
    }

    private class LFCREndOfLine extends AEndOfLine {

        public LFCREndOfLine(int startOffset, int lineNumber, int startColumn) {
            super(EndOfLineTypes.LFCR, startOffset, lineNumber, startColumn);
        }
    }

    private class CRLFEndOfLine extends AEndOfLine {

        public CRLFEndOfLine(int startOffset, int lineNumber, int startColumn) {
            super(EndOfLineTypes.CRLF, startOffset, lineNumber, startColumn);
        }
    }

    private class EndOfFile extends AEndOfLine {

        public EndOfFile(int startOffset, int lineNumber, int startColumn) {
            super(EndOfLineTypes.EOF, startOffset, lineNumber, startColumn);
        }
    }

    private class LineFeedEndOfLine extends AEndOfLine {

        public LineFeedEndOfLine(int startOffset, int lineNumber,
                int startColumn) {
            super(EndOfLineTypes.LF, startOffset, lineNumber, startColumn);
        }

    }

    private class CarritageReturnEndOfLine extends AEndOfLine {

        public CarritageReturnEndOfLine(int startOffset, int lineNumber,
                int startColumn) {
            super(EndOfLineTypes.CR, startOffset, lineNumber, startColumn);
        }

    }

    private class UndeclaredEndOfLine extends AEndOfLine {

        public UndeclaredEndOfLine(int startOffset, int lineNumber,
                int startColumn) {
            super(EndOfLineTypes.NON, startOffset, lineNumber, startColumn);
        }

    }

    private abstract class AEndOfLine implements IRobotLineElement {

        private int lineNumber;
        private int startColumn;
        private int startOffset;
        private final List<IRobotTokenType> types;


        public AEndOfLine(EndOfLineTypes type, int startOffset, int lineNumber,
                int startColumn) {
            this.lineNumber = lineNumber;
            this.startColumn = startColumn;
            this.startOffset = startOffset;
            this.types = new LinkedList<>();
            this.types.add(type);
        }


        @Override
        public int getLineNumber() {
            return lineNumber;
        }


        @Override
        public int getStartColumn() {
            return startColumn;
        }


        @Override
        public int getEndColumn() {
            return startColumn + getText().length();
        }


        @Override
        public int getStartOffset() {
            return startOffset;
        }


        @Override
        public FilePosition getFilePosition() {
            return new FilePosition(lineNumber, getStartColumn(),
                    getStartOffset());
        }


        @Override
        public StringBuilder getText() {
            return new StringBuilder(getTypes().get(0).getRepresentation()
                    .get(0));
        }


        @Override
        public StringBuilder getRaw() {
            return new StringBuilder(getTypes().get(0).getRepresentation()
                    .get(0));
        }


        @Override
        public List<IRobotTokenType> getTypes() {
            return types;
        }


        @Override
        public boolean isDirty() {
            return false;
        }


        @Override
        public String toString() {
            return String
                    .format("%s [lineNumber=%s, startColumn=%s, startOffset=%s, types=%s]",
                            this.getClass(), lineNumber, startColumn,
                            startOffset, types);
        }
    }

    public static enum EndOfLineTypes implements IRobotTokenType {
        NON, CR("\r"),

        LF("\n"),

        CRLF("\r\n"),

        LFCR("\n\r"),

        EOF;

        private List<String> representation = new LinkedList<>();


        private EndOfLineTypes(final String... representations) {
            representation.addAll(Arrays.asList(representations));
        }


        @Override
        public List<String> getRepresentation() {
            return representation;
        }
    }
}
