/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

import com.google.common.base.Optional;

public class DocumentUtilitiesTest {

    @Test
    public void whenOffsetHitsOutsideTheCell_nothingIsReturned() throws BadLocationException {
        final IDocument document = new Document("cell   ${var}   cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 14);

        assertThat(variableRegion.isPresent()).isFalse();
    }

    @Test
    public void whenOffsetHitsTheCellOutsideVariable_nothingIsReturned() throws BadLocationException {
        final IDocument document = new Document("cell  abc ${var} def  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 8);

        assertThat(variableRegion.isPresent()).isFalse();
    }

    @Test
    public void whenOffsetHitsInvalidVariable_nothingIsReturned_1() throws BadLocationException {
        final IDocument document = new Document("cell  ${var  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 9);

        assertThat(variableRegion.isPresent()).isFalse();
    }

    @Test
    public void whenOffsetHitsInvalidVariable_nothingIsReturned_2() throws BadLocationException {
        final IDocument document = new Document("cell  var}  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 7);

        assertThat(variableRegion.isPresent()).isFalse();
    }

    @Test
    public void whenOffsetHitsTheVariable_thenItsRegionIsReturned() throws BadLocationException {
        final IDocument document = new Document("cell  ${var}  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 9);

        assertThat(variableRegion.isPresent()).isTrue();
        assertThat(variableRegion.get()).isEqualTo(new Region(6, 6));
    }

    @Test
    public void whenOffsetHitsTheCellOutsideVariable_nothingIsReturned_2() throws BadLocationException {
        final IDocument document = new Document("cell  abc ${var} def  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 17);

        assertThat(variableRegion.isPresent()).isFalse();
    }

    @Test
    public void whenOffsetHitsTheVariableInsideTheCell_thenItsRegionIsReturned() throws BadLocationException {
        final IDocument document = new Document("cell  abc ${var} def  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 12);

        assertThat(variableRegion.isPresent()).isTrue();
        assertThat(variableRegion.get()).isEqualTo(new Region(10, 6));
    }

    @Test
    public void whenOffsetHitsTheOuterVariableInsideTheCell_thenItsRegionIsReturned_1() throws BadLocationException {
        final IDocument document = new Document("cell  abc ${outer${var}outer} def  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 14);

        assertThat(variableRegion.isPresent()).isTrue();
        assertThat(variableRegion.get()).isEqualTo(new Region(10, 19));
    }

    @Test
    public void whenOffsetHitsTheOuterVariableInsideTheCell_thenItsRegionIsReturned_2() throws BadLocationException {
        final IDocument document = new Document("cell  abc ${outer${var}outer} def  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 25);

        assertThat(variableRegion.isPresent()).isTrue();
        assertThat(variableRegion.get()).isEqualTo(new Region(10, 19));
    }

    @Test
    public void whenOffsetHitsTheInnerVariableInsideTheCell_thenItsRegionIsReturned() throws BadLocationException {
        final IDocument document = new Document("cell  abc ${outer${var}outer} def  cell");

        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, 20);

        assertThat(variableRegion.isPresent()).isTrue();
        assertThat(variableRegion.get()).isEqualTo(new Region(17, 6));
    }

    @Test
    public void snippetIsAbsentWhenTryingToGetNegativeNumberOfLines() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, 12, -1);

        assertThat(snippet.isPresent()).isFalse();
    }

    @Test
    public void snippetIsAbsentWhenTryingToGetLinesBeforeContent() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, -10, 1);

        assertThat(snippet.isPresent()).isFalse();
    }

    @Test
    public void snippetIsAbsentWhenTryingToGetLinesAfterContent() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, 50, 1);

        assertThat(snippet.isPresent()).isFalse();
    }

    @Test
    public void snippetContainsLinesAroundGivenOne_1() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, 12, 0);

        assertThat(snippet.isPresent()).isTrue();
        assertThat(document.get(snippet.get().getOffset(), snippet.get().getLength())).isEqualTo("line3");
    }

    @Test
    public void snippetContainsLinesAroundGivenOne_2() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, 12, 1);

        assertThat(snippet.isPresent()).isTrue();
        assertThat(document.get(snippet.get().getOffset(), snippet.get().getLength())).isEqualTo("line2\nline3\nline4");
    }

    @Test
    public void snippetContainsLinesAroundGivenOne_3() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, 7, 2);

        assertThat(snippet.isPresent()).isTrue();
        assertThat(document.get(snippet.get().getOffset(), snippet.get().getLength()))
                .isEqualTo("line1\nline2\nline3\nline4");
    }

    @Test
    public void snippetContainsLinesAroundGivenOne_4() throws BadLocationException {
        final IDocument document = new Document("line1", "line2", "line3", "line4", "line5");

        final Optional<IRegion> snippet = DocumentUtilities.getSnippet(document, 18, 2);

        assertThat(snippet.isPresent()).isTrue();
        assertThat(document.get(snippet.get().getOffset(), snippet.get().getLength()))
                .isEqualTo("line2\nline3\nline4\nline5");
    }
}
