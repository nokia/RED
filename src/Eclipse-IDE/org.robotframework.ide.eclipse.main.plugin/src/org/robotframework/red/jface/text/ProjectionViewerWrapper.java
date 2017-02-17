/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.text;

import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;

public class ProjectionViewerWrapper {

    public static ProjectionViewerWrapper from(final RobotFormEditor editor) {
        final SuiteSourceEditor sourceEditor = editor.getSourceEditor();
        return new ProjectionViewerWrapper((ProjectionViewer) sourceEditor.getViewer());
    }

    private final ProjectionViewer viewer;

    public ProjectionViewerWrapper(final ProjectionViewer viewer) {
        this.viewer = viewer;
    }

    public boolean canDoOperation(final int operation) {
        return viewer.canDoOperation(operation);
    }

    public void doOperation(final int operation) {
        viewer.doOperation(operation);
    }
}
