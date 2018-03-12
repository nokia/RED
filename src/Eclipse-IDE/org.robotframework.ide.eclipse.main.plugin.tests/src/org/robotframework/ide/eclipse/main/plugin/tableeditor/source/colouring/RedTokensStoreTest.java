/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument.IRobotDocumentParsingListener;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

import com.google.common.collect.Collections2;

public class RedTokensStoreTest {

    @Test
    public void itIsPossibleToInstallStoreOnGivenDocumentOnlyOnce() {
        final RedTokensStore store = new RedTokensStore();

        final RobotDocument doc1 = mock(RobotDocument.class);

        store.installFor(doc1);
        store.installFor(doc1);

        verify(doc1, times(1)).addFirstDocumentListener(any(IDocumentListener.class));
        verify(doc1, times(1)).addParseListener(any(IRobotDocumentParsingListener.class));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToInstallStoreForDocumentWhenItAlreadyHaveOne() {
        final RedTokensStore store = new RedTokensStore();

        store.installFor(mock(RobotDocument.class));
        store.installFor(mock(RobotDocument.class));
    }

    @Test
    public void itIsPossibleToRegisterForDifferentDocument_whenItWasExchanged() {
        final RedTokensStore store = new RedTokensStore();

        final RobotDocument doc1 = mock(RobotDocument.class);
        final RobotDocument doc2 = mock(RobotDocument.class);

        store.installFor(doc1);
        store.inputDocumentAboutToBeChanged(doc1, doc2);
        store.inputDocumentChanged(doc1, doc2);

        store.installFor(doc2);

        verify(doc1, times(1)).addFirstDocumentListener(any(IDocumentListener.class));
        verify(doc1, times(1)).addParseListener(any(IRobotDocumentParsingListener.class));
        verify(doc2, times(1)).addFirstDocumentListener(any(IDocumentListener.class));
        verify(doc2, times(1)).addParseListener(any(IRobotDocumentParsingListener.class));
    }

    @Test
    public void storeIsCleared_whenDocumentIsReparsed() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(0, 5, new Token("t1"));
        store.insert(5, 10, new Token("t2"));
        store.insert(15, 10, new Token("t3"));

        assertThat(store.getTokens()).hasSize(3);
        store.reparsingFinished(null);

        assertThat(store.getTokens()).isEmpty();
    }

    @Test
    public void nothingIsChanged_whenDifferentDocumentChanges() {
        final RobotDocument doc1 = mock(RobotDocument.class);
        when(doc1.hasNewestModel()).thenReturn(false);

        final RedTokensStore store = new RedTokensStore();
        store.installFor(doc1);

        store.insert(0, 5, new Token("t1"));
        store.insert(5, 10, new Token("t2"));
        store.insert(15, 10, new Token("t3"));

        final DocumentEvent event = new DocumentEvent(mock(RobotDocument.class), 1, 2, "new");
        store.documentAboutToBeChanged(event);
        store.documentChanged(event);

        assertThat(store.getTokens()).containsExactly(
                new PositionedTextToken(new Token("t1"), 0, 5),
                new PositionedTextToken(new Token("t2"), 5, 10),
                new PositionedTextToken(new Token("t3"), 15, 10));
    }

    @Test
    public void nothingIsChanged_whenChangedDocumentAlreadyHaveNewestModel() {
        final RobotDocument doc1 = mock(RobotDocument.class);
        when(doc1.hasNewestModel()).thenReturn(true);

        final RedTokensStore store = new RedTokensStore();
        store.installFor(doc1);

        store.insert(0, 5, new Token("t1"));
        store.insert(5, 10, new Token("t2"));
        store.insert(15, 10, new Token("t3"));

        final DocumentEvent event = new DocumentEvent(doc1, 1, 2, "new");
        store.documentAboutToBeChanged(event);
        store.documentChanged(event);

        assertThat(store.getTokens()).containsExactly(
                new PositionedTextToken(new Token("t1"), 0, 5),
                new PositionedTextToken(new Token("t2"), 5, 10),
                new PositionedTextToken(new Token("t3"), 15, 10));
    }

    @Test
    public void positionsAreUpdated_whenChangedDocumentHaveNoNewestModelYet() {
        final RobotDocument doc1 = mock(RobotDocument.class);
        when(doc1.hasNewestModel()).thenReturn(false);

        final RedTokensStore store = new RedTokensStore();
        store.installFor(doc1);

        store.insert(0, 5, new Token("t1"));
        store.insert(5, 10, new Token("t2"));
        store.insert(15, 10, new Token("t3"));

        final DocumentEvent event = new DocumentEvent(doc1, 1, 2, "new");
        store.documentAboutToBeChanged(event);
        store.documentChanged(event);

        assertThat(store.getTokens()).containsExactly(
                new PositionedTextToken(new Token("t1"), 0, 6),
                new PositionedTextToken(new Token("t2"), 6, 10),
                new PositionedTextToken(new Token("t3"), 16, 10));
    }

    @Test
    public void whenTokenIsInserted_itIsPlacedInOrder() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(new Token("marker"), 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5));
        
        for (final List<PositionedTextToken> permutation : Collections2.permutations(positions)) {
            final RedTokensStore store = new RedTokensStore();
            for (final PositionedTextToken positionedToken : permutation) {
                store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
            }

            assertThat(store.getTokens()).hasSize(6);
            assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
            assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
            assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 3));
            assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 6, 4));
            assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(new Token("marker"), 10, 0));
            assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 10, 5));
        }

    }

    @Test
    public void whenTokenIsInsertedAtOffsetOfAlreadyExistingToken_itGetsReplaced() {
        final RedTokensStore store = new RedTokensStore();

        store.insert(10, 5, new Token("t1"));
        assertThat(store.getTokens()).hasSize(1);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 10, 5));

        store.insert(10, 5, new Token("t2"));
        assertThat(store.getTokens()).hasSize(1);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t2"), 10, 5));

        store.insert(10, 7, new Token("t2"));
        assertThat(store.getTokens()).hasSize(1);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t2"), 10, 7));
    }

    @Test
    public void whenTokenIsInsertedAtOffsetOfAlreadyExistingEmptyToken_itIsAddedProperlyAndNoExcetpionIsThrown() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(10, 0, new Token("marker"));
        store.insert(10, 3, new Token("t"));

        assertThat(store.getTokens()).hasSize(2);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("marker"), 10, 0));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t"), 10, 3));
    }

    @Test
    public void whenEmptyTokenIsInsertedAtOffsetOfAlreadyExistingToken_itIsAddedProperlyAndNoExcetpionIsThrown() {
        final RedTokensStore store = new RedTokensStore();
        store.insert(10, 3, new Token("t"));
        store.insert(10, 0, new Token("marker"));

        assertThat(store.getTokens()).hasSize(2);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("marker"), 10, 0));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t"), 10, 3));
    }

    @Test
    public void noTokensAreReturnedForDifferentOffsets_whenStoreIsEmpty() {
        final RedTokensStore store = new RedTokensStore();
        for (int i = -100; i <= 100; i++) {
            assertThat(store.tokensAt(i)).isEmpty();
        }
    }

    @Test
    public void properTokensAreReturnedForDifferentOffsets_whenTokensDomainIsCountinous() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }

        assertThat(store.tokensAt(-1)).isEmpty();
        assertThat(store.tokensAt(0)).containsExactly(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.tokensAt(1)).containsExactly(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.tokensAt(2)).containsExactly(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.tokensAt(3)).containsExactly(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.tokensAt(4)).containsExactly(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.tokensAt(5)).containsExactly(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.tokensAt(6)).containsExactly(new PositionedTextToken(new Token("t4"), 6, 4));
        assertThat(store.tokensAt(7)).containsExactly(new PositionedTextToken(new Token("t4"), 6, 4));
        assertThat(store.tokensAt(8)).containsExactly(new PositionedTextToken(new Token("t4"), 6, 4));
        assertThat(store.tokensAt(9)).containsExactly(new PositionedTextToken(new Token("t4"), 6, 4));
        assertThat(store.tokensAt(10)).containsExactly(new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(11)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(12)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(13)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(14)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(15)).containsExactly(new PositionedTextToken(Token.EOF, 15, 0));
        assertThat(store.tokensAt(16)).isEmpty();
    }

    @Test
    public void properTokensAreReturnedForDifferentOffsets_whenTokensDomainContainsHoles() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t3"), 3, 3), new PositionedTextToken(new Token("t4"), 8, 2),
                new PositionedTextToken(Token.EOF, 10, 0), new PositionedTextToken(new Token("t5"), 10, 5));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }

        assertThat(store.tokensAt(-1)).isEmpty();
        assertThat(store.tokensAt(0)).containsExactly(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.tokensAt(1)).isEmpty();
        assertThat(store.tokensAt(2)).isEmpty();
        assertThat(store.tokensAt(3)).containsExactly(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.tokensAt(4)).containsExactly(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.tokensAt(5)).containsExactly(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.tokensAt(6)).isEmpty();
        assertThat(store.tokensAt(7)).isEmpty();
        assertThat(store.tokensAt(8)).containsExactly(new PositionedTextToken(new Token("t4"), 8, 2));
        assertThat(store.tokensAt(9)).containsExactly(new PositionedTextToken(new Token("t4"), 8, 2));
        assertThat(store.tokensAt(10)).containsExactly(new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(11)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(12)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(13)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(14)).containsExactly(new PositionedTextToken(new Token("t5"), 10, 5));
        assertThat(store.tokensAt(15)).isEmpty();
    }

    @Test
    public void updatingPositionsDoesNothing_whenStoreIsEmpty() {
        final RedTokensStore store = new RedTokensStore();

        for (int offset = 0; offset < 10; offset++) {
            for (int delta = -5; delta <= 5; delta++) {
                store.updatePositions(offset, delta);
                assertThat(store.getTokens()).isEmpty();
            }
        }
    }

    @Test
    public void updatingPositionsDoesNothing_whenDeltaIsZero() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(4, 0);

        assertThat(store.getTokens()).containsExactly(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));
    }

    @Test
    public void whenDeltaIsPositive_firstTokenGetsLongerAndRestIsShifted_1() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(4, 8);

        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 11));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 14, 4));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 18, 0));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 18, 5));
        assertThat(store.getTokens().get(6)).isEqualTo(new PositionedTextToken(Token.EOF, 23, 0));
    }

    @Test
    public void whenDeltaIsPositive_firstTokenGetsLongerAndRestIsShifted_2() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(0, 5);

        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 6));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 6, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 8, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 11, 4));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 15, 0));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 15, 5));
        assertThat(store.getTokens().get(6)).isEqualTo(new PositionedTextToken(Token.EOF, 20, 0));
    }

    @Test
    public void whenDeltaIsPositive_firstTokenGetsLongerAndRestIsShifted_3() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(9, 5);

        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 6, 9));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 15, 0));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 15, 5));
        assertThat(store.getTokens().get(6)).isEqualTo(new PositionedTextToken(Token.EOF, 20, 0));
    }

    @Test
    public void whenDeltaIsPositive_firstTokenGetsLongerAndRestIsShifted_4() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(10, 5);

        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 6, 4));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 10, 0));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 10, 10));
        assertThat(store.getTokens().get(6)).isEqualTo(new PositionedTextToken(Token.EOF, 20, 0));
    }

    @Test
    public void whenDeltaIsNegativeAndDamageIsInsideTheToken_lengthsAndOffsetsAreChangedAccordingly() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(6, -2);

        assertThat(store.getTokens()).hasSize(7);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 6, 2));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 8, 0));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 8, 5));
        assertThat(store.getTokens().get(6)).isEqualTo(new PositionedTextToken(Token.EOF, 13, 0));
    }

    @Test
    public void whenDeltaIsNegativeAndDamageIsInTwoAdjacentTokens_lengthsAndOffsetsAreChangedAccordingly() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(8, -4);

        assertThat(store.getTokens()).hasSize(7);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 6, 2));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 8, 0));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(new Token("t5"), 8, 3));
        assertThat(store.getTokens().get(6)).isEqualTo(new PositionedTextToken(Token.EOF, 11, 0));
    }

    @Test
    public void whenDeltaIsNegativeAndDamageIsLong_lengthsAndOffsetsAreChangedAccordingly() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(5, -7);

        assertThat(store.getTokens()).hasSize(6);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 2));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(Token.EOF, 5, 0));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(new Token("t5"), 5, 3));
        assertThat(store.getTokens().get(5)).isEqualTo(new PositionedTextToken(Token.EOF, 8, 0));
    }

    @Test
    public void whenDeltaIsNegativeAndDamageClearsFirstToken_lengthsAndOffsetsAreChangedAccordingly() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(3, -4);

        assertThat(store.getTokens()).hasSize(5);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t4"), 3, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(Token.EOF, 6, 0));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(new Token("t5"), 6, 5));
    }

    @Test
    public void whenDeltaIsNegativeAndDamageLeavesEofsAdjacent_lengthsAndOffsetsAreChangedAccordinglyEofsAreCleared() {
        final List<PositionedTextToken> positions = newArrayList(new PositionedTextToken(new Token("t1"), 0, 1),
                new PositionedTextToken(new Token("t2"), 1, 2), new PositionedTextToken(new Token("t3"), 3, 3),
                new PositionedTextToken(new Token("t4"), 6, 4), new PositionedTextToken(Token.EOF, 10, 0),
                new PositionedTextToken(new Token("t5"), 10, 5), new PositionedTextToken(Token.EOF, 15, 0));

        final RedTokensStore store = new RedTokensStore();
        for (final PositionedTextToken positionedToken : positions) {
            store.insert(positionedToken.getOffset(), positionedToken.getLength(), positionedToken.getToken());
        }
        store.updatePositions(8, -7);

        assertThat(store.getTokens()).hasSize(5);
        assertThat(store.getTokens().get(0)).isEqualTo(new PositionedTextToken(new Token("t1"), 0, 1));
        assertThat(store.getTokens().get(1)).isEqualTo(new PositionedTextToken(new Token("t2"), 1, 2));
        assertThat(store.getTokens().get(2)).isEqualTo(new PositionedTextToken(new Token("t3"), 3, 3));
        assertThat(store.getTokens().get(3)).isEqualTo(new PositionedTextToken(new Token("t4"), 6, 2));
        assertThat(store.getTokens().get(4)).isEqualTo(new PositionedTextToken(Token.EOF, 8, 0));
    }
}
