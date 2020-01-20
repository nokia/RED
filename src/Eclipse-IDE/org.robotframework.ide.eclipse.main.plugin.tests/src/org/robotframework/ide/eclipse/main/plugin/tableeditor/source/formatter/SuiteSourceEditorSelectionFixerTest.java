/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

public class SuiteSourceEditorSelectionFixerTest {

    @Test
    public void selectionIsNotFixed_whenNotNeeded() throws Exception {
        final Document oldDocument = new Document("line1  ", "");
        final Document newDocument = new Document("line1", "");

        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        when(selectionProvider.getSelection()).thenReturn(new TextSelection(oldDocument, 2, 0));

        final SuiteSourceEditorSelectionFixer fixer = new SuiteSourceEditorSelectionFixer(selectionProvider);
        fixer.saveSelection(oldDocument);
        fixer.fixSelection(newDocument);

        verify(selectionProvider).setSelection(new TextSelection(newDocument, 2, 0));
    }

    @Test
    public void selectionIsFixed_whenLineWithoutDelimiterIsCut() throws Exception {
        final Document oldDocument = new Document("line1  ");
        final Document newDocument = new Document("line1");

        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        when(selectionProvider.getSelection()).thenReturn(new TextSelection(oldDocument, 6, 0));

        final SuiteSourceEditorSelectionFixer fixer = new SuiteSourceEditorSelectionFixer(selectionProvider);
        fixer.saveSelection(oldDocument);
        fixer.fixSelection(newDocument);

        verify(selectionProvider).setSelection(new TextSelection(newDocument, 5, 0));
    }

    @Test
    public void selectionIsFixed_whenLineWithDelimiterIsCut() throws Exception {
        final Document oldDocument = new Document("line1", "line2      ", "line3");
        final Document newDocument = new Document("line1", "line2", "line3");

        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        when(selectionProvider.getSelection()).thenReturn(new TextSelection(oldDocument, 13, 2));

        final SuiteSourceEditorSelectionFixer fixer = new SuiteSourceEditorSelectionFixer(selectionProvider);
        fixer.saveSelection(oldDocument);
        fixer.fixSelection(newDocument);

        verify(selectionProvider).setSelection(new TextSelection(newDocument, 10, 0));
    }

    @Test
    public void selectionIsFixed_whenLinesAreCutAndSelectionContainsSeveralLines() throws Exception {
        final Document oldDocument = new Document("line1", "      ", "line2  ", "    ");
        final Document newDocument = new Document("line1", "", "line2", "");

        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        when(selectionProvider.getSelection()).thenReturn(new TextSelection(oldDocument, 9, 14));

        final SuiteSourceEditorSelectionFixer fixer = new SuiteSourceEditorSelectionFixer(selectionProvider);
        fixer.saveSelection(oldDocument);
        fixer.fixSelection(newDocument);

        verify(selectionProvider).setSelection(new TextSelection(newDocument, 5, 7));
    }

    @Test
    public void selectionIsFixed_whenLineIsDeleted() throws Exception {
        final Document oldDocument = new Document("line1", "line22", "line333");
        final Document newDocument = new Document("line1", "line22");

        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        when(selectionProvider.getSelection()).thenReturn(new TextSelection(oldDocument, 18, 0));

        final SuiteSourceEditorSelectionFixer fixer = new SuiteSourceEditorSelectionFixer(selectionProvider);
        fixer.saveSelection(oldDocument);
        fixer.fixSelection(newDocument);

        verify(selectionProvider).setSelection(new TextSelection(newDocument, 11, 0));
    }
}
