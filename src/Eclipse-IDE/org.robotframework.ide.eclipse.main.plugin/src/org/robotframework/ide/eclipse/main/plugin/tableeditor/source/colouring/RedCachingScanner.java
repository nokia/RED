/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;
import org.robotframework.red.jface.text.rules.IRedTokenScanner;

public class RedCachingScanner implements ITokenScanner {

    private final IRedTokenScanner scanner;
    
    private RobotDocument document;
    private final RedTokensStore tokensStore;

    private Position lastTokenPosition;

    private int currentOffset;
    private int rangeOffset;
    private int rangeLength;


    public RedCachingScanner(final IRedTokenScanner scanner, final RedTokensStore store) {
        this.scanner = scanner;
        this.tokensStore = store;
    }

    @Override
    public void setRange(final IDocument document, final int offset, final int length) {
        this.document = (RobotDocument) document;
        tokensStore.installFor(this.document);

        lastTokenPosition = null;

        rangeOffset = offset;
        rangeLength = length;
        currentOffset = offset;

        scanner.setRange(document, offset, length);
    }

    @Override
    public IToken nextToken() {
        if (currentOffset >= rangeOffset + rangeLength) {
            // we're outside current range, so EOF has to be returned
            lastTokenPosition = new Position(currentOffset, 0);
            scanner.resetPosition();
            return Token.EOF;
        }

        final IToken token;
        final List<PositionedTextToken> entries = tokensStore.tokensAt(currentOffset);

        if (entries.isEmpty()
                || (entries.size() == 1 && entries.get(0).getToken().isEOF() && currentOffset == rangeOffset)) {
            // when there is no cached entry at current offset, or there is only eof from previous
            // section but we are at the beginning of other section

            // the inner scanner may need to change range if it was configured for some other region
            scanner.setRange(document, currentOffset, rangeLength - (currentOffset - rangeOffset));

            token = scanner.nextToken();
            final int offset = scanner.getTokenOffset();
            final int length = scanner.getTokenLength();
            if (token.isEOF()) {
                scanner.resetPosition();
            }

            tokensStore.insert(offset, length, token);
            lastTokenPosition = new Position(offset, length);

        } else if (entries.size() > 1 && currentOffset == rangeOffset) {
            // there is eof from previous section and also cached token from current section begin
            token = entries.get(1).getToken();
            lastTokenPosition = cropPositionToCurrentRange(entries.get(1).getPosition());

        } else {
            // just return cached entry
            token = entries.get(0).getToken();
            lastTokenPosition = cropPositionToCurrentRange(entries.get(0).getPosition());
        }
        currentOffset = lastTokenPosition.getOffset() + lastTokenPosition.getLength();

        return token;
    }

    private Position cropPositionToCurrentRange(final Position position) {
        // we need to crop the position when cached token begins before current range or ends after it
        final Position positionCopy = new Position(position.getOffset(), position.getLength());

        if (rangeOffset > positionCopy.getOffset()) {
            positionCopy.setLength(positionCopy.getLength() - (rangeOffset - positionCopy.getOffset()));
            positionCopy.setOffset(rangeOffset);
        }

        final int delta = positionCopy.getOffset() + positionCopy.getLength() - (rangeOffset + rangeLength);
        if (delta > 0) {
            positionCopy.setLength(positionCopy.getLength() - delta);
        }
        return positionCopy;
    }

    @Override
    public int getTokenOffset() {
        return lastTokenPosition.getOffset();
    }

    @Override
    public int getTokenLength() {
        return lastTokenPosition.getLength();
    }
}
