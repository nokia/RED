/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleCommentHandler.E4ToggleCommentHandler;

import com.google.common.base.Splitter;

public class ToggleCommentHandlerTest {

    E4ToggleCommentHandler handler = new E4ToggleCommentHandler();

    @Test
    public void commentMarkIsAdded_whenThereIsNoSuchInSingleLineSelection() {
        final IDocument document = new Document("some line");
        final ITextSelection selection = new TextSelection(document, 0, 0);
        handler.toggleComment(document, selection);

        assertThat(document.get()).isEqualTo("# some line");
    }

    @Test
    public void commentMarkIsRemoved_ifItIsAlreadyThereInSingleLineSelection() {
        final IDocument document = new Document("#some #line");
        final ITextSelection selection = new TextSelection(document, 0, 0);
        handler.toggleComment(document, selection);

        assertThat(document.get()).isEqualTo("some #line");
    }

    @Test
    public void commentMarkIsRemovedWithAdditionalSpace_ifItIsAlreadyThereInSingleLineSelection() {
        final IDocument document = new Document("# some #line");
        final ITextSelection selection = new TextSelection(document, 0, 0);
        handler.toggleComment(document, selection);

        assertThat(document.get()).isEqualTo("some #line");
    }

    @Test
    public void commentMarkIsAdded_whenThereIsNoSuchInMultiLineSelection() {
        final IDocument document = new Document("line1", "line2", "  line3", "line4");
        final ITextSelection selection = new TextSelection(document, 8, 8);
        handler.toggleComment(document, selection);

        final List<String> lines = Splitter.on('\n').omitEmptyStrings().splitToList(document.get());
        assertThat(lines).containsExactly("line1", "# line2", "  # line3", "line4");
    }

    @Test
    public void commentMarkIsRemoved_ifItIsAlreadyThereInMultiLineSelection() {
        final IDocument document = new Document("line1", "# line2", "  #line3", "line4");
        final ITextSelection selection = new TextSelection(document, 8, 8);
        handler.toggleComment(document, selection);

        final List<String> lines = Splitter.on('\n').omitEmptyStrings().splitToList(document.get());
        assertThat(lines).containsExactly("line1", "line2", "  line3", "line4");
    }

    @Test
    public void commentMarkIsAlwaysAdded_ifThereIsAtLeastOneLineWithoutCommentMark_1() {
        final IDocument document = new Document("line1", "# line2", "line3", "# line4", "line5");
        final ITextSelection selection = new TextSelection(document, 8, 16);
        handler.toggleComment(document, selection);

        final List<String> lines = Splitter.on('\n').omitEmptyStrings().splitToList(document.get());
        assertThat(lines).containsExactly("line1", "# # line2", "# line3", "# # line4", "line5");
    }

    @Test
    public void commentMarkIsAlwaysAdded_ifThereIsAtLeastOneLineWithoutCommentMark_2() {
        final IDocument document = new Document("# line1", "line2", "# line3", "line4", "# line5");
        final ITextSelection selection = new TextSelection(document, 8, 16);
        handler.toggleComment(document, selection);

        final List<String> lines = Splitter.on('\n').omitEmptyStrings().splitToList(document.get());
        assertThat(lines).containsExactly("# line1", "# line2", "# # line3", "# line4", "# line5");
    }
}
