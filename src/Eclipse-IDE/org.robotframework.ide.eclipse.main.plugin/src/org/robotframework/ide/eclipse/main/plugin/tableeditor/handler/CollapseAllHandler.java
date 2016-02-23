/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.CollapseAllHandler.E4CollapseAllHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class CollapseAllHandler extends DIParameterizedHandler<E4CollapseAllHandler> {

    public CollapseAllHandler() {
        super(E4CollapseAllHandler.class);
    }

    public static class E4CollapseAllHandler {

        @Execute
        public Object collapseAll(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final TreeViewer viewer = (TreeViewer) viewerAccessor.getViewer();
            viewer.getTree().setRedraw(false);
            viewer.collapseAll();
            viewer.getTree().setRedraw(true);
            return null;
        }
    }
}
