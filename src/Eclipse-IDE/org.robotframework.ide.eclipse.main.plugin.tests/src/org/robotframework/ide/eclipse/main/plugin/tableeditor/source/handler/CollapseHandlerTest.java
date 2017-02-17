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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.CollapseHandler.E4CollapseHandler;
import org.robotframework.red.jface.text.ProjectionViewerWrapper;

public class CollapseHandlerTest {

    private final E4CollapseHandler handler = new E4CollapseHandler();

    @Test
    public void collapseIsNotExecuted_whenViewerSaysItIsImpossible() {
        final ProjectionViewerWrapper projectionViewer = mock(ProjectionViewerWrapper.class);
        when(projectionViewer.canDoOperation(ProjectionViewer.COLLAPSE)).thenReturn(false);
        handler.collapse(projectionViewer);

        verify(projectionViewer).canDoOperation(ProjectionViewer.COLLAPSE);
        verifyNoMoreInteractions(projectionViewer);
    }

    @Test
    public void collapseIsExecuted_whenVieweSaysItIsPossible() {
        final ProjectionViewerWrapper projectionViewer = mock(ProjectionViewerWrapper.class);
        when(projectionViewer.canDoOperation(ProjectionViewer.COLLAPSE)).thenReturn(true);

        handler.collapse(projectionViewer);

        verify(projectionViewer).canDoOperation(ProjectionViewer.COLLAPSE);
        verify(projectionViewer).doOperation(ProjectionViewer.COLLAPSE);
        verifyNoMoreInteractions(projectionViewer);
    }
}
