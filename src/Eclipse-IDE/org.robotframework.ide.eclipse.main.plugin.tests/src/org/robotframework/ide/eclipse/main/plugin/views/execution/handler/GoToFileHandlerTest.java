/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.net.URI;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

@RunWith(MockitoJUnitRunner.class)
public class GoToFileHandlerTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(GoToFileHandlerTest.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @Mock
    private BiConsumer<IFile, String> caseSelectionConsumer;

    @Test
    public void nothingIsDone_whenNodeIsNull() throws Exception {
        goToFile(null);

        verifyZeroInteractions(caseSelectionConsumer);
    }

    @Test
    public void nothingIsDone_whenNodePathIsNull() throws Exception {
        goToFile(new ExecutionTreeNode(null, null, "null_path", null, null));

        verifyZeroInteractions(caseSelectionConsumer);
    }

    @Test
    public void nothingIsDone_whenNodePathDoesNotExist() throws Exception {
        goToFile(new ExecutionTreeNode(null, null, "not_existing_path", new URI("file:///unknown.robot"), null));

        verifyZeroInteractions(caseSelectionConsumer);
    }

    @Test
    public void nothingIsDone_whenNodePathIsNotFromWorkspace() throws Exception {
        final File nonWorkspaceFile = tempFolder.newFile("non_workspace_test.robot");

        goToFile(new ExecutionTreeNode(null, null, "not_linked_file", nonWorkspaceFile.toURI(), null));

        verifyZeroInteractions(caseSelectionConsumer);
    }

    @Test
    public void caseIsOpened_whenNodePathPointsToProjectFile() throws Exception {
        final IFile suite = projectProvider.createFile("suite.robot");

        goToFile(new ExecutionTreeNode(null, null, "project_file", suite.getLocationURI(), null));

        verify(caseSelectionConsumer).accept(suite, "project_file");
        verifyNoMoreInteractions(caseSelectionConsumer);
    }

    @Test
    public void caseIsOpened_whenNodePathPointsToLinkedFile() throws Exception {
        final File nonWorkspaceFile = tempFolder.newFile("non_workspace_test.robot");
        final IFile linkedSuite = projectProvider.getFile("linkedSuite.robot");
        resourceCreator.createLink(nonWorkspaceFile.toURI(), linkedSuite);

        goToFile(new ExecutionTreeNode(null, null, "linked_file", nonWorkspaceFile.toURI(), null));

        verify(caseSelectionConsumer).accept(linkedSuite, "linked_file");
        verifyNoMoreInteractions(caseSelectionConsumer);
    }

    private void goToFile(final ExecutionTreeNode caseNode) {
        GoToFileHandler.E4GoToFileHandler.openExecutionNodeSourceFile(caseNode, caseSelectionConsumer);
    }
}
