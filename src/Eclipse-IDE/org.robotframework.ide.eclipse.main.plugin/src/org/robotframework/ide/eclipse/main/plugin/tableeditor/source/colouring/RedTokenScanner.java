/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;
import org.robotframework.red.jface.text.rules.IRedTokenScanner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

public class RedTokenScanner implements IRedTokenScanner {

    private RobotDocument document;

    private final List<ISyntaxColouringRule> rules;

    private Deque<IRobotLineElement> tokensToAnalyze;

    private List<RobotLine> lines;

    private Position lastTokenPosition;

    private int rangeOffset;
    private int rangeLength;
    private int rangeLine;
    private int currentOffsetInToken;

    public RedTokenScanner(final ISyntaxColouringRule... rules) {
        this.rules = newArrayList(rules);
    }

    @Override
    public void resetPosition() {
        lastTokenPosition = null;
    }

    @Override
    public void setRange(final IDocument document, final int offset, final int length) {
        this.document = (RobotDocument) document;

        if (lastTokenPosition == null || lastTokenPosition.getOffset() + lastTokenPosition.getLength() != offset) {
            this.tokensToAnalyze = null;
            this.lines = null;

            this.rangeOffset = offset;
            this.rangeLength = length;
            this.rangeLine = DocumentUtilities.getLine(document, offset);

            this.currentOffsetInToken = 0;
        }
    }

    @Override
    public IToken nextToken() {
        return nextToken(new Supplier<Deque<IRobotLineElement>>() {

            @Override
            public Deque<IRobotLineElement> get() {
                try {
                    lines = document.getNewestModel().getFileContent();
                    return new RedTokensQueueBuilder().buildQueue(rangeOffset, rangeLength, lines, rangeLine);
                } catch (final InterruptedException e) {
                    throw new UnableToScanTokensException("Unable to build tokens queue", e);
                }
            }
        });
    }

    @VisibleForTesting
    IToken nextToken(final Supplier<Deque<IRobotLineElement>> elementsQueueSupplier) {
        if (tokensToAnalyze == null) {
            tokensToAnalyze = elementsQueueSupplier.get();

            final IRobotLineElement firstToken = tokensToAnalyze.peekFirst();
            if (firstToken != null) {
                currentOffsetInToken = rangeOffset - firstToken.getStartOffset();
            }
        }

        if (tokensToAnalyze.isEmpty()) {
            lastTokenPosition = lastTokenPosition == null ? new Position(0, 0)
                    : new Position(getTokenOffset() + getTokenLength(), 0);
            return Token.EOF;
        }
        final IRobotLineElement nextToken = tokensToAnalyze.poll();
        for (final ISyntaxColouringRule rule : rules) {
            if (!rule.isApplicable(nextToken)) {
                continue;
            }
            final Optional<PositionedTextToken> tok = rule.evaluate(nextToken, currentOffsetInToken, lines);
            if (tok.isPresent()) {
                final PositionedTextToken textToken = tok.get();
                lastTokenPosition = textToken.getPosition();

                if (lastTokenPosition.offset + lastTokenPosition.length >= nextToken.getStartOffset()
                        + nextToken.getText().length()) {
                    // rule have consumed whole Robot Token
                    currentOffsetInToken = 0;
                } else {
                    // the token needs more coloring, so return it to queue and shift the
                    // offset
                    currentOffsetInToken = lastTokenPosition.getOffset() + lastTokenPosition.getLength()
                            - nextToken.getStartOffset();
                    tokensToAnalyze.addFirst(nextToken);
                }
                return textToken.getToken();
            }
        }
        lastTokenPosition = new Position(nextToken.getStartOffset() + currentOffsetInToken,
                nextToken.getText().length() - currentOffsetInToken);
        currentOffsetInToken = 0;
        return ISyntaxColouringRule.DEFAULT_TOKEN;
    }

    @Override
    public int getTokenOffset() {
        return lastTokenPosition.getOffset();
    }

    @Override
    public int getTokenLength() {
        return lastTokenPosition.getLength();
    }

    public static class UnableToScanTokensException extends IllegalStateException {

        private static final long serialVersionUID = 1L;

        public UnableToScanTokensException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
