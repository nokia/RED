/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.CollapseAllHandler.E4CollapseAllHandler;

public class CollapseAllHandlerTest {

    private final E4CollapseAllHandler handler = new E4CollapseAllHandler();

    @Test
    public void collapseIsNotExecuted_whenViewerSaysItIsImpossible() {
        final ProjectionViewer viewer = mock(ProjectionViewer.class);
        when(viewer.canDoOperation(ProjectionViewer.COLLAPSE_ALL)).thenReturn(false);

        final SuiteSourceEditor sourceEditor = mock(SuiteSourceEditor.class);
        when(sourceEditor.getViewer()).thenReturn(viewer);

        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSourceEditor()).thenReturn(sourceEditor);

        handler.collapseAll(editor);

        verify(viewer).canDoOperation(ProjectionViewer.COLLAPSE_ALL);
        verifyNoMoreInteractions(viewer);
    }

    @Test
    public void collapseIsExecuted_whenVieweSaysItIsPossible() {
        final ProjectionViewer viewer = mock(ProjectionViewer.class);
        when(viewer.canDoOperation(ProjectionViewer.COLLAPSE_ALL)).thenReturn(true);

        final SuiteSourceEditor sourceEditor = mock(SuiteSourceEditor.class);
        when(sourceEditor.getViewer()).thenReturn(viewer);

        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSourceEditor()).thenReturn(sourceEditor);

        handler.collapseAll(editor);

        verify(viewer).canDoOperation(ProjectionViewer.COLLAPSE_ALL);
        verify(viewer).doOperation(ProjectionViewer.COLLAPSE_ALL);
        verifyNoMoreInteractions(viewer);
    }

}
