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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ExpandHandler.E4ExpandHandler;

public class ExpandHandlerTest {

    private final E4ExpandHandler handler = new E4ExpandHandler();

    @Test
    public void expandIsNotExecuted_whenViewerSaysItIsImpossible() {
        final ProjectionViewer viewer = mock(ProjectionViewer.class);
        when(viewer.canDoOperation(ProjectionViewer.EXPAND)).thenReturn(false);

        final SuiteSourceEditor sourceEditor = mock(SuiteSourceEditor.class);
        when(sourceEditor.getViewer()).thenReturn(viewer);

        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSourceEditor()).thenReturn(sourceEditor);

        handler.expand(editor);

        verify(viewer).canDoOperation(ProjectionViewer.EXPAND);
        verifyNoMoreInteractions(viewer);
    }

    @Test
    public void expandIsExecuted_whenVieweSaysItIsPossible() {
        final ProjectionViewer viewer = mock(ProjectionViewer.class);
        when(viewer.canDoOperation(ProjectionViewer.EXPAND)).thenReturn(true);

        final SuiteSourceEditor sourceEditor = mock(SuiteSourceEditor.class);
        when(sourceEditor.getViewer()).thenReturn(viewer);

        final RobotFormEditor editor = mock(RobotFormEditor.class);
        when(editor.getSourceEditor()).thenReturn(sourceEditor);

        handler.expand(editor);

        verify(viewer).canDoOperation(ProjectionViewer.EXPAND);
        verify(viewer).doOperation(ProjectionViewer.EXPAND);
        verifyNoMoreInteractions(viewer);
    }

}
