/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;
import org.robotframework.red.jface.text.rules.IRedTokenScanner;

public class RedCachingScannerTest {

    @Test
    public void whenStoreIsEmpty_tokensAreScannedUsingInternalScanner_1() {
        final RedTokensStore store = new RedTokensStore();

        final IRedTokenScanner internalScanner = createInternalScanner(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5),
                new PositionedTextToken(Token.EOF, 15, 0));

        final RedCachingScanner cachingScanner = new RedCachingScanner(internalScanner, store);

        final List<PositionedTextToken> tokens = readTokens(cachingScanner, 0, 15);

        final List<PositionedTextToken> expected = newArrayList(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5));

        assertThat(tokens).containsExactlyElementsOf(expected);
        assertThat(store.getTokens()).containsExactlyElementsOf(expected);
    }

    @Test
    public void whenStoreIsEmpty_tokensAreScannedUsingInternalScanner_2() {
        final RedTokensStore store = new RedTokensStore();

        final IRedTokenScanner internalScanner = createInternalScanner(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5),
                new PositionedTextToken(Token.EOF, 15, 0));

        final RedCachingScanner cachingScanner = new RedCachingScanner(internalScanner, store);

        final List<PositionedTextToken> tokens = readTokens(cachingScanner, 1, 9);

        final List<PositionedTextToken> expected = newArrayList(
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4));

        assertThat(tokens).containsExactlyElementsOf(expected);
        assertThat(store.getTokens()).containsExactlyElementsOf(expected);
    }
    
    @Test
    public void whenStoreHasPreviousSectionOnly_nextSectionIsScannedUsingInternalScanner() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(0, 1, new Token("t1"));
        store.insert(1, 2, new Token("t2"));
        store.insert(3, 3, new Token("t3"));
        store.insert(6, 0, Token.EOF);
        
        final IRedTokenScanner internalScanner = createInternalScanner(
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5),
                new PositionedTextToken(Token.EOF, 15, 0));

        final RedCachingScanner cachingScanner = new RedCachingScanner(internalScanner, store);

        final List<PositionedTextToken> tokens = readTokens(cachingScanner, 6, 9);

        final List<PositionedTextToken> expected = newArrayList(
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5));

        assertThat(tokens).containsExactlyElementsOf(expected);
        assertThat(store.getTokens()).containsExactly(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(Token.EOF, 6, 0),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5));
    }

    @Test
    public void whenStoreHasTokensCached_theyAreReturnedWithoutUsingInternalScanner() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(0, 1, new Token("t1"));
        store.insert(1, 2, new Token("t2"));
        store.insert(3, 3, new Token("t3"));
        store.insert(6, 4, new Token("t4"));
        store.insert(10, 5, new Token("t5"));
        store.insert(15, 0, Token.EOF);
        
        final IRedTokenScanner internalScanner = mock(IRedTokenScanner.class);
        final RedCachingScanner cachingScanner = new RedCachingScanner(internalScanner, store);

        final List<PositionedTextToken> tokens = readTokens(cachingScanner, 0, 15);

        final List<PositionedTextToken> expected = newArrayList(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5));

        assertThat(tokens).containsExactlyElementsOf(expected);
        verify(internalScanner, never()).nextToken();
    }
    
    @Test
    public void whenStoreHasTokensCachedAndNewSectionIsScanned_itIsReturnedFromCacheCorrectly() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(0, 1, new Token("t1"));
        store.insert(1, 2, new Token("t2"));
        store.insert(3, 3, new Token("t3"));
        store.insert(6, 0, Token.EOF);
        store.insert(6, 4, new Token("t4"));
        store.insert(10, 5, new Token("t5"));
        store.insert(15, 0, Token.EOF);

        final IRedTokenScanner internalScanner = mock(IRedTokenScanner.class);
        final RedCachingScanner cachingScanner = new RedCachingScanner(internalScanner, store);

        final List<PositionedTextToken> tokens = readTokens(cachingScanner, 6, 9);

        final List<PositionedTextToken> expected = newArrayList(
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5));

        assertThat(tokens).containsExactlyElementsOf(expected);
        verify(internalScanner, never()).nextToken();
    }

    @Test
    public void whenOnlyPartOfSectionIsCached_theRestIsProperlyReturnedUsingInternalScanner() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(0, 1, new Token("t1"));
        store.insert(1, 2, new Token("t2"));
        store.insert(3, 3, new Token("t3"));

        final IRedTokenScanner internalScanner = createInternalScanner(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5),
                new PositionedTextToken(Token.EOF, 15, 0));
        final RedCachingScanner cachingScanner = new RedCachingScanner(internalScanner, store);

        final List<PositionedTextToken> tokens = readTokens(cachingScanner, 0, 15);

        final List<PositionedTextToken> expected = newArrayList(
                new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2),
                new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4),
                new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(tokens).containsExactlyElementsOf(expected);
        assertThat(store.getTokens()).containsExactlyElementsOf(expected);
    }

    private static List<PositionedTextToken> readTokens(final ITokenScanner scanner, final int offset,
            final int length) {
        final List<PositionedTextToken> result = new ArrayList<>();

        scanner.setRange(mock(RobotDocument.class), offset, length);
        IToken token = scanner.nextToken();
        while (!token.isEOF()) {
            result.add(new PositionedTextToken(token, scanner.getTokenOffset(), scanner.getTokenLength()));
            token = scanner.nextToken();
        }
        return result;
    }

    private static IRedTokenScanner createInternalScanner(final PositionedTextToken... tokensToReturn) {
        return new IRedTokenScanner() {

            private final List<PositionedTextToken> tokens = newArrayList(tokensToReturn);

            private Position lastPosition;

            private int current;

            @Override
            public void setRange(final IDocument document, final int offset, final int length) {
                lastPosition = null;
                current = 0;
                for (final PositionedTextToken token : tokensToReturn) {
                    if (token.getPosition().includes(offset)) {
                        break;
                    }
                    current++;
                }
            }

            @Override
            public IToken nextToken() {
                final PositionedTextToken token = tokens.get(current++);
                this.lastPosition = token.getPosition();
                return token.getToken();
            }

            @Override
            public int getTokenOffset() {
                return lastPosition.getOffset();
            }

            @Override
            public int getTokenLength() {
                return lastPosition.getLength();
            }

            @Override
            public void resetPosition() {
                lastPosition = null;
            }
        };
    }
}
