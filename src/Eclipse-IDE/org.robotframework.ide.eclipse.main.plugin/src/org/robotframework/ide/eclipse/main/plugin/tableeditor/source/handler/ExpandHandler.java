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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ExpandHandler.E4ExpandHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ExpandHandler extends DIParameterizedHandler<E4ExpandHandler> {

    public ExpandHandler() {
        super(E4ExpandHandler.class);
    }

    public static class E4ExpandHandler {

        @Execute
        public void expand(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {

            final SuiteSourceEditor sourceEditor = editor.getSourceEditor();
            final ProjectionViewer viewer = (ProjectionViewer) sourceEditor.getViewer();

            if (viewer.canDoOperation(ProjectionViewer.EXPAND)) {
                viewer.doOperation(ProjectionViewer.EXPAND);
            }
        }
    }
}
