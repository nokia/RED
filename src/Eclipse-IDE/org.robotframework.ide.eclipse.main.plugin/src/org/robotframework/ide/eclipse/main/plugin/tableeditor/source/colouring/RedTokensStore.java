/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument.IRobotDocumentParsingListener;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;

public class RedTokensStore implements ITextInputListener, IDocumentListener, IRobotDocumentParsingListener {

    private RobotDocument document;

    private final List<PositionedTextToken> tokens = new ArrayList<>();

    public void installFor(final RobotDocument document) {
        if (this.document != null && this.document != document) {
            throw new IllegalStateException("The document got replaced!");
        }
        if (this.document == null) {
            this.document = document;
            // it has to know first about changes in order to update tokens, so that
            // painting will use properly adjusted cached tokens
            this.document.addFirstDocumentListener(this);
            this.document.addParseListener(this);
        }
    }

    @Override
    public void inputDocumentAboutToBeChanged(final IDocument oldInput, final IDocument newInput) {
        // document will be different, so we need to install it again when needed
        if (this.document != null) {
            document.removeDocumentListener(this);
            document.removeParseListener(this);
        }
        document = null;
    }

    @Override
    public void inputDocumentChanged(final IDocument oldInput, final IDocument newInput) {
        // nothing to do
    }

    @Override
    public void documentAboutToBeChanged(final DocumentEvent event) {
        // nothing to do
    }

    @Override
    public void documentChanged(final DocumentEvent event) {
        if (event.getDocument() != document) {
            return;
        }
        synchronized (this) {
            if (document.hasNewestModel()) {
                return;
            }
            final int damageOffset = event.getOffset();
            final int delta = event.getText().length() - event.getLength();
            updatePositions(damageOffset, delta);
        }
    }

    @Override
    public void reparsingFinished(final RobotFileOutput parsedOutput) {
        synchronized (this) {
            // when reparsing has been finished we can remove all the tokens, so that
            // the scanner will take tokens directly from reparsed output instead
            // of a store
            tokens.clear();
        }
    }

    public void reset() {
        synchronized (this) {
            tokens.clear();
        }
    }

    List<PositionedTextToken> tokensAt(final int offset) {
        final Range<Integer> range = entriesAt(offset);
        final ArrayList<PositionedTextToken> entries = new ArrayList<>();
        if (range == null) {
            return entries;
        }
        for (int i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
            entries.add(tokens.get(i));
        }
        return entries;
    }

    private Range<Integer> entriesAt(final int offset) {
        if (tokens.isEmpty()) {
            return null;
        }
        int foundItemIndex = binarySearch(offset);
        if (foundItemIndex < 0) {
            foundItemIndex = -foundItemIndex - 1;
            if (foundItemIndex == 0 || tokens.get(foundItemIndex - 1).getOffset()
                    + tokens.get(foundItemIndex - 1).getLength() <= offset) {
                return null;
            }
            // -1 additionally because it lays in previous segment
            foundItemIndex--;
        }
        int min = foundItemIndex;
        int i = foundItemIndex - 1;
        while (i >= 0 && tokens.get(i).getOffset() == offset) {
            min--;
            i--;
        }

        int max = foundItemIndex;
        i = foundItemIndex + 1;
        while (i < tokens.size() && tokens.get(i).getOffset() == offset) {
            max++;
            i++;
        }
        return Range.closed(min, max);
    }

    public void insert(final int offset, final int length, final IToken token) {
        final int foundItemIndex = binarySearch(offset);
        if (foundItemIndex >= 0) {
            if (tokens.get(foundItemIndex).getLength() == 0) {
                // we allow to have marker tokens of 0 length at the same position
                tokens.add(foundItemIndex + 1, new PositionedTextToken(token, offset, length));
            } else if (length == 0) {
                tokens.add(foundItemIndex, new PositionedTextToken(token, offset, length));
            } else {
                tokens.set(foundItemIndex, new PositionedTextToken(token, offset, length));
            }
        } else {
            final int wouldBeIndex = -foundItemIndex - 1;
            tokens.add(wouldBeIndex, new PositionedTextToken(token, offset, length));
        }
    }

    @VisibleForTesting
    void updatePositions(final int damageOffset, final int delta) {
        if (tokens.isEmpty() || delta == 0) {
            // either there is nothing to update, or the damage will be colored with old style
            // and changed later
            return;
        }

        final int foundItemIndex = binarySearch(damageOffset);
        final int startIndex = foundItemIndex >= 0 ? foundItemIndex : Math.max(0, -foundItemIndex - 2);
        final PositionedTextToken firstEntry = tokens.get(startIndex);
        if (delta > 0) {
            firstEntry.setLength(firstEntry.getLength() + delta);

            for (int i = startIndex + 1; i < tokens.size(); i++) {
                final PositionedTextToken entry = tokens.get(i);
                entry.setOffset(entry.getOffset() + delta);
            }
        } else {
            int toRemove = -delta;
            
            int length = firstEntry.getLength();
            firstEntry.setLength(
                    Math.max(damageOffset - firstEntry.getOffset(), firstEntry.getLength() - toRemove));
            int removedSoFar = length - firstEntry.getLength();
            toRemove -= removedSoFar;
            
            int i = startIndex + 1;
            while (i < tokens.size()) {
                final PositionedTextToken entry = tokens.get(i);
                
                entry.setOffset(entry.getOffset() - removedSoFar);

                if (toRemove > 0) {
                    length = entry.getLength();
                    entry.setLength(Math.max(0, length - toRemove));
                    removedSoFar += length - entry.getLength();
                    toRemove -= length - entry.getLength();
                }

                if (entry.getLength() == 0 && (!entry.getToken().isEOF() || tokens.get(i - 1).getToken().isEOF())) {
                    tokens.remove(i);
                } else {
                    i++;
                }
            }
            if (tokens.get(startIndex).getLength() == 0 && !tokens.get(startIndex).getToken().isEOF()) {
                tokens.remove(startIndex);
            }
        }
    }

    private int binarySearch(final int offset) {
        // works similarly to Collections#binarySearch() although does not require comparator and
        // works only on offsets
        int low = 0;
        int high = tokens.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int cmp = Integer.compare(tokens.get(mid).getOffset(), offset);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // found
            }
        }
        return -(low + 1); // not found
    }

    public List<PositionedTextToken> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return tokens.toString();
    }
}
