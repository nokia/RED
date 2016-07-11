/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.IChildElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

import com.google.common.base.Optional;

public class RobotLine implements IChildElement<RobotFile> {

    private final RobotFile parent;

    private int lineNumber = -1;

    private List<IRobotLineElement> lineElements = new ArrayList<>(0);

    private Optional<SeparatorType> separatorForLine = Optional.absent();

    private IRobotLineElement eol = EndOfLineBuilder.newInstance()
            .setEndOfLines(null)
            .setLineNumber(IRobotLineElement.NOT_SET)
            .setStartColumn(IRobotLineElement.NOT_SET)
            .setStartOffset(IRobotLineElement.NOT_SET)
            .buildEOL();

    public RobotLine(final int lineNumber, final RobotFile parent) {
        this.lineNumber = lineNumber;
        this.parent = parent;
    }

    public void setSeparatorType(final SeparatorType separatorForLine) {
        this.separatorForLine = Optional.fromNullable(separatorForLine);
    }

    public Optional<SeparatorType> getSeparatorForLine() {
        return separatorForLine;
    }

    @Override
    public RobotFile getParent() {
        return parent;
    }

    public List<IRobotLineElement> getLineElements() {
        return lineElements;
    }

    public void setLineElements(final List<IRobotLineElement> lineElements) {
        this.lineElements = lineElements;
    }

    public void addLineElement(final IRobotLineElement lineElement) {
        this.lineElements.add(lineElement);
    }

    public void addLineElementAt(final int position, final IRobotLineElement lineElement) {
        this.lineElements.add(position, lineElement);
    }

    public void setLineElementAt(final int position, final IRobotLineElement lineElement) {
        this.lineElements.set(position, lineElement);
    }

    public Optional<Integer> getElementPositionInLine(final int offset, final PositionCheck posCheckStrategy) {
        Optional<Integer> pos = Optional.absent();
        final int size = lineElements.size();
        for (int i = 0; i < size; i++) {
            final IRobotLineElement e = lineElements.get(i);
            if (posCheckStrategy.meets(e, offset)) {
                pos = Optional.of(i);
                break;
            }
        }

        return pos;
    }

    public static enum PositionCheck {
        STARTS {

            @Override
            public boolean meets(final IRobotLineElement element, final int offset) {
                return (element.getFilePosition().getOffset() == offset);
            }
        },
        INSIDE {

            @Override
            public boolean meets(final IRobotLineElement element, final int offset) {
                return (element.getStartOffset() >= offset
                        && offset <= (element.getStartOffset() + (element.getEndColumn() - element.getStartColumn())));
            }
        },
        ENDS

        {

            @Override
            public boolean meets(final IRobotLineElement element, final int offset) {
                return (element.getStartOffset() + (element.getEndColumn() - element.getStartColumn()) == offset);
            }
        };

        public abstract boolean meets(final IRobotLineElement element, final int offset);
    }

    public Optional<Integer> getElementPositionInLine(final IRobotLineElement elem) {
        Optional<Integer> pos = Optional.absent();
        final int size = lineElements.size();
        for (int i = 0; i < size; i++) {
            if (lineElements.get(i) == elem) {
                pos = Optional.of(i);
                break;
            }
        }

        return pos;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public IRobotLineElement getEndOfLine() {
        return this.eol;
    }

    public void setEndOfLine(final List<Constant> endOfLine, final int currentOffset, final int currentColumn) {
        this.eol = EndOfLineBuilder.newInstance()
                .setEndOfLines(endOfLine)
                .setStartColumn(currentColumn)
                .setStartOffset(currentOffset)
                .setLineNumber(lineNumber)
                .buildEOL();
    }

    @Override
    public String toString() {
        return String.format("RobotLine [lineNumber=%s, lineElements=%s, endOfLine=%s]", lineNumber, lineElements, eol);
    }

}
