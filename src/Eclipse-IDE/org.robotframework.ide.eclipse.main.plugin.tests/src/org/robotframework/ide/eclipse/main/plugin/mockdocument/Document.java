/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockdocument;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;

/**
 * @author Michal Anglart
 *
 */
public class Document implements IDocument {

    private StringBuilder documentText;

    public Document() {
        this.documentText = new StringBuilder();
    }

    public Document(final IDocument document) {
        this(document.get());
    }

    public Document(final String content) {
        this.documentText = new StringBuilder(content);
    }

    public Document(final List<String> content) {
        this.documentText = new StringBuilder();
        for (final String line : content) {
            this.documentText.append(line);
            this.documentText.append("\n");
        }
    }

    public Document(final String firstLine, final String... lines) {
        this.documentText = new StringBuilder(firstLine);
        this.documentText.append("\n");

        for (final String line : lines) {
            this.documentText.append(line);
            this.documentText.append("\n");
        }
    }

    private void assertOffsetWithinLimits(final int offset) throws BadLocationException {
        if (offset < 0 || offset >= documentText.length()) {
            throw new BadLocationException();
        }
    }

    @Override
    public char getChar(final int offset) throws BadLocationException {
        assertOffsetWithinLimits(offset);
        return documentText.charAt(offset);
    }

    @Override
    public int getLength() {
        return documentText.length();
    }

    @Override
    public String get() {
        return documentText.toString();
    }

    @Override
    public String get(final int offset, final int length) throws BadLocationException {
        assertOffsetWithinLimits(offset);
        assertOffsetWithinLimits(Math.max(0, offset + length - 1));

        return documentText.substring(offset, offset + length);
    }

    @Override
    public void set(final String text) {
        documentText = new StringBuilder(text);
    }

    @Override
    public void replace(final int offset, final int length, final String text) throws BadLocationException {
        assertOffsetWithinLimits(offset);
        assertOffsetWithinLimits(Math.max(0, offset + length - 1));

        documentText.replace(offset, offset + length, text);
    }

    @Override
    public void addDocumentListener(final IDocumentListener listener) {
        // nothing to do currrently
    }

    @Override
    public void removeDocumentListener(final IDocumentListener listener) {
        // nothing to do currrently
    }

    @Override
    public void addPrenotifiedDocumentListener(final IDocumentListener documentAdapter) {
        // nothing to do currrently
    }

    @Override
    public void removePrenotifiedDocumentListener(final IDocumentListener documentAdapter) {
        // nothing to do currrently
    }

    @Override
    public void addPositionCategory(final String category) {
        // nothing to do currrently
    }

    @Override
    public void removePositionCategory(final String category) throws BadPositionCategoryException {
        // nothing to do currrently
    }

    @Override
    public String[] getPositionCategories() {
        return null;
    }

    @Override
    public boolean containsPositionCategory(final String category) {
        return false;
    }

    @Override
    public void addPosition(final Position position) throws BadLocationException {
        // nothing to do currrently
    }

    @Override
    public void removePosition(final Position position) {
        // nothing to do currrently
    }

    @Override
    public void addPosition(final String category, final Position position)
            throws BadLocationException, BadPositionCategoryException {
        // nothing to do currrently

    }

    @Override
    public void removePosition(final String category, final Position position) throws BadPositionCategoryException {
        // nothing to do currrently
    }

    @Override
    public Position[] getPositions(final String category) throws BadPositionCategoryException {
        return null;
    }

    @Override
    public boolean containsPosition(final String category, final int offset, final int length) {
        return false;
    }

    @Override
    public int computeIndexInCategory(final String category, final int offset)
            throws BadLocationException, BadPositionCategoryException {
        return 0;
    }

    @Override
    public void addPositionUpdater(final IPositionUpdater updater) {
        // nothing to do currrently
    }

    @Override
    public void removePositionUpdater(final IPositionUpdater updater) {
        // nothing to do currrently
    }

    @Override
    public void insertPositionUpdater(final IPositionUpdater updater, final int index) {
        // nothing to do currrently
    }

    @Override
    public IPositionUpdater[] getPositionUpdaters() {
        return null;
    }

    @Override
    public String[] getLegalContentTypes() {
        return null;
    }

    @Override
    public String getContentType(final int offset) throws BadLocationException {
        return null;
    }

    @Override
    public ITypedRegion getPartition(final int offset) throws BadLocationException {
        return null;
    }

    @Override
    public ITypedRegion[] computePartitioning(final int offset, final int length) throws BadLocationException {
        return null;
    }

    @Override
    public void addDocumentPartitioningListener(final IDocumentPartitioningListener listener) {
        // nothing to do currrently
    }

    @Override
    public void removeDocumentPartitioningListener(final IDocumentPartitioningListener listener) {
        // nothing to do currrently
    }

    @Override
    public void setDocumentPartitioner(final IDocumentPartitioner partitioner) {
        // nothing to do currrently
    }

    @Override
    public IDocumentPartitioner getDocumentPartitioner() {
        return null;
    }

    @Override
    public int getLineLength(final int line) throws BadLocationException {
        return getLineInformation(line).getLength();
    }

    @Override
    public int getLineOfOffset(final int offset) throws BadLocationException {
        assertOffsetWithinLimits(offset);

        int noOfLines = 0;
        for (int i = 0; i < offset; i++) {
            if (documentText.charAt(i) == '\n') {
                noOfLines++;
            }
        }
        return noOfLines;
    }

    @Override
    public int getLineOffset(final int line) throws BadLocationException {
        return getLineInformation(line).getOffset();
    }

    @Override
    public IRegion getLineInformation(final int line) throws BadLocationException {
        if (line < 0 || line > getNumberOfLines()) {
            throw new BadLocationException();
        }

        int currentLineStart = 0;

        int noOfLines = 0;
        for (int i = 0; i < documentText.length(); i++) {
            if (documentText.charAt(i) == '\n' || i == documentText.length() - 1) {
                if (noOfLines == line) {
                    return new Region(currentLineStart, i - currentLineStart);
                }
                noOfLines++;
                currentLineStart = i + 1;
            }
        }
        throw new BadLocationException();
    }

    @Override
    public IRegion getLineInformationOfOffset(final int offset) throws BadLocationException {
        return getLineInformation(getLineOfOffset(offset));
    }

    @Override
    public int getNumberOfLines() {
        return computeNumberOfLines(documentText.toString());
    }

    @Override
    public int getNumberOfLines(final int offset, final int length) throws BadLocationException {
        assertOffsetWithinLimits(offset);
        assertOffsetWithinLimits(Math.max(0, offset + length - 1));

        int noOfLines = 0;
        for (int i = offset; i < offset + length; i++) {
            if (documentText.charAt(i) == '\n') {
                noOfLines++;
            }
        }
        return noOfLines;
    }

    @Override
    public int computeNumberOfLines(final String text) {
        int noOfLines = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                noOfLines++;
            }
        }
        return noOfLines;
    }

    @Override
    public String[] getLegalLineDelimiters() {
        return new String[] { "\n" };
    }

    @Override
    public String getLineDelimiter(final int line) throws BadLocationException {
        return "\n";
    }

    @Override
    public int search(final int startOffset, final String findString, final boolean forwardSearch, final boolean caseSensitive,
            final boolean wholeWord) throws BadLocationException {
        return 0;
    }

    @Override
    public String toString() {
        return documentText.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Document) {
            final Document that = (Document) obj;
            return this.get().equals(that.get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return documentText.hashCode();
    }
}
