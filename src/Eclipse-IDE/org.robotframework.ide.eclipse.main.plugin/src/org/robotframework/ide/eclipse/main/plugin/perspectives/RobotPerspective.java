/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.perspectives;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.message.MessageLogView;

public class RobotPerspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(final IPageLayout layout) {
        final String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);

        final IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

        final IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.65f, editorArea);
        bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);

        final IFolderLayout bottomLog = layout.createFolder("bottomRight", IPageLayout.RIGHT, 0.50f, "bottom");
        bottomLog.addView(MessageLogView.ID);
        bottomLog.addView(ExecutionView.ID);

        final IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.75f, editorArea);
        topRight.addView(IPageLayout.ID_OUTLINE);

        layout.addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
        layout.addShowViewShortcut(MessageLogView.ID);
        layout.addShowViewShortcut(ExecutionView.ID);
        layout.addShowViewShortcut(DocumentationView.ID);

        layout.addShowInPart("org.eclipse.ui.navigator.ProjectExplorer");
    }
}
