/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

import com.google.common.base.Preconditions;

/**
 * Slightly modified version of {@link org.eclipse.jface.text.rules.BufferedRuleBasedScanner}
 * 
 * @author Michal Anglart
 */
public class SuiteSourceTokenScanner extends RuleBasedScanner {

    private final static int DEFAULT_BUFFER_SIZE = 500;

    private final int bufferSize;
    protected final char[] buffer;
    protected int buffStart;
    protected int buffEnd;
    private int documentLength;

    protected int numberOfCellSeparators;

    private int numberOfCharsInCurrentSeparator;

    public SuiteSourceTokenScanner(final IRule[] rules) {
        this(rules, DEFAULT_BUFFER_SIZE);
    }

    private SuiteSourceTokenScanner(final IRule[] rules, final int size) {
        Preconditions.checkArgument(size > 0);
        this.bufferSize = size;
        this.buffer = new char[size];
        this.numberOfCellSeparators = UNDEFINED;
        this.numberOfCharsInCurrentSeparator = 0;
        setRules(rules);
    }

    public final int numberOfCellSeparatorsInLineBeforeOffset() {
        if (numberOfCellSeparators == UNDEFINED) {
            numberOfCellSeparators = calculateNumberOfCells();
        }
        return numberOfCellSeparators;
    }

    protected int calculateNumberOfCells() {
        return getNumberOfCellsSeparators(lineContentBeforeCurrentPosition());
    }

    private int getNumberOfCellsSeparators(final String lineContentBefore) {
        if (lineContentBefore.isEmpty()) {
            return 0;
        }
        final String withoutTabs = lineContentBefore.replaceAll("\t", "  ")
                .replaceAll(" \\| ", "   ")
                .replaceFirst("^\\| ", "  ");

        int spacesRegions = 0;
        int currentNumberOfSpaces = 0;
        for (int i = 0; i < withoutTabs.length(); i++) {
            if (withoutTabs.charAt(i) == ' ') {
                currentNumberOfSpaces++;
            } else if (currentNumberOfSpaces == 1) {
                currentNumberOfSpaces = 0;
            } else if (currentNumberOfSpaces > 1) {
                spacesRegions++;
                currentNumberOfSpaces = 0;
            }
        }
        // maybe spaces were suffix of line content
        if (currentNumberOfSpaces > 1) {
            spacesRegions++;
        }

        numberOfCharsInCurrentSeparator = currentNumberOfSpaces;
        numberOfCellSeparators = spacesRegions;
        return numberOfCellSeparators;
    }

    public String lineContentBeforeCurrentPosition() {
        try {
            final IRegion lineInfo = fDocument.getLineInformationOfOffset(fOffset);
            final int from = lineInfo.getOffset();
            final int length = fOffset - from;
            if (from >= buffStart && fOffset <= buffEnd) {
                return String.copyValueOf(buffer, from - buffStart, length);
            } else {
                return fDocument.get(from, length);
            }
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Shifts the buffer so that the buffer starts at the
     * given document offset.
     *
     * @param offset
     *            the document offset at which the buffer starts
     */
    private void shiftBuffer(final int offset) {
        buffStart = offset;
        buffEnd = Math.min(buffStart + bufferSize, documentLength);

        try {
            final String content = fDocument.get(buffStart, buffEnd - buffStart);
            content.getChars(0, buffEnd - buffStart, buffer, 0);
        } catch (final BadLocationException x) {
        }
    }

    @Override
    public void setRange(final IDocument document, final int offset, final int length) {
        super.setRange(document, offset, length);

        documentLength = document.getLength();
        shiftBuffer(offset);
        numberOfCellSeparators = UNDEFINED;
    }

    @Override
    public int read() {
        fColumn = UNDEFINED;
        if (fOffset >= fRangeEnd) {
            ++fOffset;
            return EOF;
        }

        if (fOffset == buffEnd) {
            shiftBuffer(buffEnd);
        } else if (fOffset < buffStart || buffEnd < fOffset) {
            shiftBuffer(fOffset);
        }

        final int ch = buffer[fOffset++ - buffStart];
        updateNumberOfCellsAfterRead(ch);
        return ch;
    }

    protected void updateNumberOfCellsAfterRead(final int ch) {
        if (ch == '\n' || ch == '\r') {
            numberOfCellSeparators = 0;
            numberOfCharsInCurrentSeparator = 0;
        } else if (ch == ' ' || ch == '\t') {
            final int before = numberOfCharsInCurrentSeparator;
            numberOfCharsInCurrentSeparator += 1 + (ch == '\t' ? 1 : 0);
            final boolean shouldIncrement = before < 2 && numberOfCharsInCurrentSeparator > 1;
            if (shouldIncrement) {
                numberOfCellSeparators = numberOfCellSeparators == UNDEFINED ? 1 : numberOfCellSeparators + 1;
            }
        } else {
            numberOfCharsInCurrentSeparator = 0;
        }
    }

    @Override
    public void unread() {
        if (fOffset == buffStart) {
            shiftBuffer(Math.max(0, buffStart - (bufferSize / 2)));
        }
        --fOffset;
        fColumn = UNDEFINED;
        final int idx = fOffset - buffStart;
        updateNumberOfCellsAfterUnread(idx < bufferSize ? buffer[idx] : EOF);
    }

    protected void updateNumberOfCellsAfterUnread(final int ch) {
        if (ch == '\n' || ch == '\r' || ch == EOF) {
            numberOfCellSeparators = UNDEFINED;
            numberOfCharsInCurrentSeparator = 0;
        } else if (ch == ' ' || ch == '\t') {
            final int before = numberOfCharsInCurrentSeparator;
            numberOfCharsInCurrentSeparator -= 1 + (ch == '\t' ? 1 : 0);
            final boolean shouldDecrement = before > 1 && numberOfCharsInCurrentSeparator < 2
                    && numberOfCellSeparators > 0;
            if (shouldDecrement) {
                numberOfCellSeparators--;
            }
        }
    }

    public static class SuiteTsvSourceTokenScanner extends SuiteSourceTokenScanner {

        public SuiteTsvSourceTokenScanner(final IRule[] rules) {
            super(rules);
        }

        @Override
        protected int calculateNumberOfCells() {
            final String lineContent = lineContentBeforeCurrentPosition();
            final String lineContentBefore = lineContent;
            int separators = 0;
            for (final char ch : lineContentBefore.toCharArray()) {
                if (ch == '\t') {
                    separators++;
                }
            }
            return separators;
        }

        @Override
        protected void updateNumberOfCellsAfterRead(final int ch) {
            if (ch == '\n' || ch == '\r') {
                numberOfCellSeparators = 0;
            } else if (ch == '\t' && numberOfCellSeparators != UNDEFINED) {
                numberOfCellSeparators++;
            }
        }

        @Override
        protected void updateNumberOfCellsAfterUnread(final int ch) {
            if (ch == '\n' || ch == '\r') {
                numberOfCellSeparators = UNDEFINED;
            } else if (ch == '\t' && numberOfCellSeparators != UNDEFINED) {
                numberOfCellSeparators--;
            }
        }
    }
}
