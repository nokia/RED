/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ExpandAllHandler.E4ExpandAllHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.jface.text.ProjectionViewerWrapper;

import com.google.common.annotations.VisibleForTesting;

public class ExpandAllHandler extends DIParameterizedHandler<E4ExpandAllHandler> {

    public ExpandAllHandler() {
        super(E4ExpandAllHandler.class);
    }

    public static class E4ExpandAllHandler {

        @Execute
        public void expandAll(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            expandAll(ProjectionViewerWrapper.from(editor));
        }

        @VisibleForTesting
        void expandAll(final ProjectionViewerWrapper projectionViewer) {
            if (projectionViewer.canDoOperation(ProjectionViewer.EXPAND_ALL)) {
                projectionViewer.doOperation(ProjectionViewer.EXPAND_ALL);
            }
        }
    }
}
