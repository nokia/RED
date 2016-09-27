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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.CollapseHandler.E4CollapseHandler;

public class CollapseHandlerTest {

    private final E4CollapseHandler handler = new E4CollapseHandler();

    @Test
    public void collapseIsNotExecuted_whenViewerSaysItIsImpossible() {
        final ProjectionViewer viewer = mock(ProjectionViewer.class);
        when(viewer.canDoOperation(ProjectionViewer.COLLAPSE)).thenReturn(false);

        final SuiteSourceEditor sourceEditor = mock(SuiteSourceEditor.class);
        when(sourceEditor.getViewer()).thenReturn(viewer);

        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSourceEditor()).thenReturn(sourceEditor);

        handler.collapse(editor);

        verify(viewer).canDoOperation(ProjectionViewer.COLLAPSE);
        verifyNoMoreInteractions(viewer);
    }

    @Test
    public void collapseIsExecuted_whenVieweSaysItIsPossible() {
        final ProjectionViewer viewer = mock(ProjectionViewer.class);
        when(viewer.canDoOperation(ProjectionViewer.COLLAPSE)).thenReturn(true);

        final SuiteSourceEditor sourceEditor = mock(SuiteSourceEditor.class);
        when(sourceEditor.getViewer()).thenReturn(viewer);

        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSourceEditor()).thenReturn(sourceEditor);

        handler.collapse(editor);

        verify(viewer).canDoOperation(ProjectionViewer.COLLAPSE);
        verify(viewer).doOperation(ProjectionViewer.COLLAPSE);
        verifyNoMoreInteractions(viewer);
    }

}
