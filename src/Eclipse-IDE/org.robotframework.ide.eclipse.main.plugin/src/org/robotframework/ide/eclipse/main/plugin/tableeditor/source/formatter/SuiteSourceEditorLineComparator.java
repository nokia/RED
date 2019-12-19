/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Based on org.eclipse.jdt.internal.ui.text.LineComparator.
 *
 * This implementation of <code>IRangeComparator</code> compares lines of a document.
 * The lines are compared using a DJB hash function.
 */
class SuiteSourceEditorLineComparator implements IRangeComparator {

    private final IDocument fDocument;

    private final List<Integer> fHashes;

    /**
     * Create a line comparator for the given document.
     *
     * @param document
     */
    SuiteSourceEditorLineComparator(final IDocument document) {
        fDocument = document;

        final Integer[] nulls = new Integer[fDocument.getNumberOfLines()];
        fHashes = new ArrayList<>(Arrays.asList(nulls));
    }

    /*
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
     */
    @Override
    public int getRangeCount() {
        return fDocument.getNumberOfLines();
    }

    /*
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int, org.eclipse.compare.rangedifferencer.IRangeComparator, int)
     */
    @Override
    public boolean rangesEqual(final int thisIndex, final IRangeComparator other, final int otherIndex) {
        try {
            return getHash(thisIndex).equals(((SuiteSourceEditorLineComparator) other).getHash(otherIndex));
        } catch (final BadLocationException e) {
            return false;
        }
    }

    /*
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int, int, org.eclipse.compare.rangedifferencer.IRangeComparator)
     */
    @Override
    public boolean skipRangeComparison(final int length, final int maxLength, final IRangeComparator other) {
        return false;
    }

    /**
     * @param line the number of the line in the document to get the hash for
     * @return the hash of the line
     * @throws BadLocationException if the line number is invalid
     */
    private Integer getHash(final int line) throws BadLocationException {
        Integer hash = fHashes.get(line);
        if (hash == null) {
            final IRegion lineRegion = fDocument.getLineInformation(line);
            final String lineContents = fDocument.get(lineRegion.getOffset(), lineRegion.getLength());
            hash = Integer.valueOf(computeDJBHash(lineContents));
            fHashes.set(line, hash);
        }
        return hash;
    }

    /**
     * Compute a hash using the DJB hash algorithm
     *
     * @param string the string for which to compute a hash
     * @return the DJB hash value of the string
     */
    private int computeDJBHash(final String string) {
        int hash = 5381;
        for (int i = 0; i < string.length(); i++) {
            final char ch = string.charAt(i);
            hash = (hash << 5) + hash + ch;
        }
        return hash;
    }
}
