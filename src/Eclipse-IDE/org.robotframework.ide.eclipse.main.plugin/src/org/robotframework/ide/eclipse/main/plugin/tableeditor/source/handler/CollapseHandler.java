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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.CollapseHandler.E4CollapseHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class CollapseHandler extends DIParameterizedHandler<E4CollapseHandler> {

    public CollapseHandler() {
        super(E4CollapseHandler.class);
    }

    public static class E4CollapseHandler {

        @Execute
        public void collapse(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {

            final SuiteSourceEditor sourceEditor = editor.getSourceEditor();
            final ProjectionViewer viewer = (ProjectionViewer) sourceEditor.getViewer();

            if (viewer.canDoOperation(ProjectionViewer.COLLAPSE)) {
                viewer.doOperation(ProjectionViewer.COLLAPSE);
            }
        }
    }
}
