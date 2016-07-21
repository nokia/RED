/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.perspectives;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.console.IConsoleConstants;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;
import org.robotframework.ide.eclipse.main.plugin.views.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.MessageLogView;

import com.google.common.collect.Lists;

public class RobotPerspectiveTest {

    @Test
    public void editorAreaShouldBeVisible() {
        final IPageLayout layout = createMockPageLayout(Lists.<IFolderLayout> newArrayList());

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(layout).setEditorAreaVisible(true);
    }

    @Test
    public void thereIsADebugPerspectiveShortcutCreated() {
        final IPageLayout layout = createMockPageLayout(Lists.<IFolderLayout> newArrayList());

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(layout).addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
    }

    @Test
    public void thereAreShortcutsToMessageLogViewAndExecutionView() {
        final IPageLayout layout = createMockPageLayout(Lists.<IFolderLayout> newArrayList());

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(layout).addShowViewShortcut(MessageLogView.ID);
        verify(layout).addShowViewShortcut(ExecutionView.ID);
    }

    @Test
    public void thereIsAProjectExplorerShortcutInShowIn() {
        final IPageLayout layout = createMockPageLayout(Lists.<IFolderLayout> newArrayList());

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(layout).addShowInPart("org.eclipse.ui.navigator.ProjectExplorer");
    }

    @Test
    public void thereAreFourFoldersCreated() {
        final ArrayList<IFolderLayout> folders = Lists.<IFolderLayout> newArrayList();
        final IPageLayout layout = createMockPageLayout(folders);

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        assertThat(folders).hasSize(4);
    }

    @Test
    public void firstFolderContainsOnlyProjectExplorer() {
        final ArrayList<IFolderLayout> folders = Lists.<IFolderLayout> newArrayList();
        final IPageLayout layout = createMockPageLayout(folders);

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(folders.get(0), times(1)).addView(anyString());
        verify(folders.get(0)).addView(IPageLayout.ID_PROJECT_EXPLORER);
    }

    @Test
    public void secondFolderContainsConsoleAndProblemViews() {
        final ArrayList<IFolderLayout> folders = Lists.<IFolderLayout> newArrayList();
        final IPageLayout layout = createMockPageLayout(folders);

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(folders.get(1), times(2)).addView(anyString());
        verify(folders.get(1)).addView(IConsoleConstants.ID_CONSOLE_VIEW);
        verify(folders.get(1)).addView(IPageLayout.ID_PROBLEM_VIEW);
    }

    @Test
    public void thirdFolderContainsOnlyOutlineView() {
        final ArrayList<IFolderLayout> folders = Lists.<IFolderLayout> newArrayList();
        final IPageLayout layout = createMockPageLayout(folders);

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(folders.get(3), times(1)).addView(anyString());
        verify(folders.get(3)).addView(IPageLayout.ID_OUTLINE);
    }

    @Test
    public void fourthFolderContainsConsoleAndProblemViews() {
        final ArrayList<IFolderLayout> folders = Lists.<IFolderLayout> newArrayList();
        final IPageLayout layout = createMockPageLayout(folders);

        final RobotPerspective perspective = new RobotPerspective();
        perspective.createInitialLayout(layout);

        verify(folders.get(2), times(3)).addView(anyString());
        verify(folders.get(2)).addView(MessageLogView.ID);
        verify(folders.get(2)).addView(ExecutionView.ID);
        verify(folders.get(2)).addView(DocumentationView.ID);
    }

    protected IPageLayout createMockPageLayout(final List<IFolderLayout> folderLayouts) {
        final IPageLayout layout = mock(IPageLayout.class);
        final IFolderLayout folderLayout1 = mock(IFolderLayout.class);
        final IFolderLayout folderLayout2 = mock(IFolderLayout.class);
        final IFolderLayout folderLayout3 = mock(IFolderLayout.class);
        final IFolderLayout folderLayout4 = mock(IFolderLayout.class);
        when(layout.createFolder(anyString(), anyInt(), anyFloat(), anyString())).thenReturn(folderLayout1,
                folderLayout2, folderLayout3, folderLayout4);
        
        folderLayouts.addAll(newArrayList(folderLayout1, folderLayout2, folderLayout3, folderLayout4));

        return layout;
    }

}
