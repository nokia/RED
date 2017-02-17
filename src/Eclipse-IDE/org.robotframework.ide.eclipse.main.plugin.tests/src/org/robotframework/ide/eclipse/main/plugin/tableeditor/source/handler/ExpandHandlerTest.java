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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ExpandHandler.E4ExpandHandler;
import org.robotframework.red.jface.text.ProjectionViewerWrapper;

public class ExpandHandlerTest {

    private final E4ExpandHandler handler = new E4ExpandHandler();

    @Test
    public void expandIsNotExecuted_whenViewerSaysItIsImpossible() {
        final ProjectionViewerWrapper projectionViewer = mock(ProjectionViewerWrapper.class);
        when(projectionViewer.canDoOperation(ProjectionViewer.EXPAND)).thenReturn(false);
        handler.expand(projectionViewer);

        verify(projectionViewer).canDoOperation(ProjectionViewer.EXPAND);
        verifyNoMoreInteractions(projectionViewer);
    }

    @Test
    public void expandIsExecuted_whenVieweSaysItIsPossible() {
        final ProjectionViewerWrapper projectionViewer = mock(ProjectionViewerWrapper.class);
        when(projectionViewer.canDoOperation(ProjectionViewer.EXPAND)).thenReturn(true);

        handler.expand(projectionViewer);

        verify(projectionViewer).canDoOperation(ProjectionViewer.EXPAND);
        verify(projectionViewer).doOperation(ProjectionViewer.EXPAND);
        verifyNoMoreInteractions(projectionViewer);
    }

}
