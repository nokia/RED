/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo.VersionAvailabilityInfoBuilder;

public class EndOfLineBuilder {

    private List<Constant> constant = new ArrayList<>(0);

    private int lineNumber = IRobotLineElement.NOT_SET;

    private int startColumn = IRobotLineElement.NOT_SET;

    private int startOffset = IRobotLineElement.NOT_SET;

    public static EndOfLineBuilder newInstance() {
        return new EndOfLineBuilder();
    }

    public EndOfLineBuilder setEndOfLines(final List<Constant> constant) {
        if (constant == null) {
            this.constant = new ArrayList<>(0);
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
            final Constant myEol = constant.get(0);
            if (myEol == Constant.CR) {
                eol = new CarriageReturnEndOfLine(startOffset, lineNumber, startColumn);
            } else if (myEol == Constant.LF) {
                eol = new LineFeedEndOfLine(startOffset, lineNumber, startColumn);
            } else if (myEol == Constant.EOF) {
                eol = new EndOfFile(startOffset, lineNumber, startColumn);
            }
        } else if (constant.size() == 2) {
            final Constant myEol1 = constant.get(0);
            final Constant myEol2 = constant.get(1);

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

    private static class LFCREndOfLine extends AEndOfLine {

        public LFCREndOfLine(final int startOffset, final int lineNumber, final int startColumn) {
            super(EndOfLineTypes.LFCR, startOffset, lineNumber, startColumn);
        }
    }

    private static class CRLFEndOfLine extends AEndOfLine {

        public CRLFEndOfLine(final int startOffset, final int lineNumber, final int startColumn) {
            super(EndOfLineTypes.CRLF, startOffset, lineNumber, startColumn);
        }
    }

    private static class EndOfFile extends AEndOfLine {

        public EndOfFile(final int startOffset, final int lineNumber, final int startColumn) {
            super(EndOfLineTypes.EOF, startOffset, lineNumber, startColumn);
        }
    }

    private static class LineFeedEndOfLine extends AEndOfLine {

        public LineFeedEndOfLine(final int startOffset, final int lineNumber, final int startColumn) {
            super(EndOfLineTypes.LF, startOffset, lineNumber, startColumn);
        }

    }

    private static class CarriageReturnEndOfLine extends AEndOfLine {

        public CarriageReturnEndOfLine(final int startOffset, final int lineNumber, final int startColumn) {
            super(EndOfLineTypes.CR, startOffset, lineNumber, startColumn);
        }

    }

    private static class UndeclaredEndOfLine extends AEndOfLine {

        public UndeclaredEndOfLine(final int startOffset, final int lineNumber, final int startColumn) {
            super(EndOfLineTypes.NON, startOffset, lineNumber, startColumn);
        }

    }

    private static abstract class AEndOfLine implements IRobotLineElement {

        private final int lineNumber;

        private final int startColumn;

        private final int startOffset;

        private final List<IRobotTokenType> types;

        public AEndOfLine(final EndOfLineTypes type, final int startOffset, final int lineNumber,
                final int startColumn) {
            this.lineNumber = lineNumber;
            this.startColumn = startColumn;
            this.startOffset = startOffset;
            this.types = new ArrayList<>(0);
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
            return new FilePosition(lineNumber, getStartColumn(), getStartOffset());
        }

        @Override
        public String getText() {
            return !getTypes().isEmpty() && !getTypes().get(0).getRepresentation().isEmpty()
                    ? getTypes().get(0).getRepresentation().get(0) : "";
        }

        @Override
        public String getRaw() {
            return !getTypes().isEmpty() && !getTypes().get(0).getRepresentation().isEmpty()
                    ? getTypes().get(0).getRepresentation().get(0) : "";
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
            return String.format("%s [lineNumber=%s, startColumn=%s, startOffset=%s, types=%s]", this.getClass(),
                    lineNumber, startColumn, startOffset, types);
        }

        @Override
        public VersionAvailabilityInfo getVersionInformation() {
            VersionAvailabilityInfo var = null;
            if (!types.isEmpty()) {
                var = types.get(0).findVersionAvailabilityInfo(getRaw());
            }

            return var;
        }

        @Override
        public AEndOfLine copyWithoutPosition() {
            return copy(false);
        }

        @Override
        public AEndOfLine copy() {
            return copy(true);
        }

        private AEndOfLine copy(final boolean posInclude) {
            final EndOfLineBuilder builder = EndOfLineBuilder.newInstance().setEndOfLines(LineReader.Constant.get(this));
            if (posInclude) {
                builder.setLineNumber(this.getLineNumber());
                builder.setStartColumn(this.getStartColumn());
                builder.setStartOffset(this.getStartOffset());
            }
            return (AEndOfLine) builder.buildEOL();
        }

        @Override
        public final int hashCode() {
            return super.hashCode();
        }

        @Override
        public final boolean equals(final Object obj) {
            return super.equals(obj);
        }
    }

    public static enum EndOfLineTypes implements IRobotTokenType {
        /**
         */
        NON,
        /**
         */
        CR(VersionAvailabilityInfoBuilder.create().addRepresentation("\r").build()),
        /**
         */
        LF(VersionAvailabilityInfoBuilder.create().addRepresentation("\n").build()),
        /**
         */
        CRLF(VersionAvailabilityInfoBuilder.create().addRepresentation("\r\n").build()),
        /**
         */
        LFCR(VersionAvailabilityInfoBuilder.create().addRepresentation("\n\r").build()),
        /**
         */
        EOF;

        private final List<String> text = new ArrayList<>(0);

        private final List<VersionAvailabilityInfo> representation = new ArrayList<>(0);

        private EndOfLineTypes(final VersionAvailabilityInfo... representations) {
            for (final VersionAvailabilityInfo vInfo : representations) {
                representation.add(vInfo);
                text.add(vInfo.getRepresentation());
            }
        }

        @Override
        public List<String> getRepresentation() {
            return text;
        }

        @Override
        public List<VersionAvailabilityInfo> getVersionAvailabilityInfos() {
            return representation;
        }

        @Override
        public VersionAvailabilityInfo findVersionAvailabilityInfo(final String text) {
            VersionAvailabilityInfo vaiResult = null;
            for (final VersionAvailabilityInfo vai : representation) {
                if (vai.getRepresentation().equals(text)) {
                    vaiResult = vai;
                    break;
                }
            }
            return vaiResult;
        }
    }
}
