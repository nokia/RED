/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.io.File;
import java.net.URI;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class GoToFileHandlerTest {

    @Project
    IProject project;

    @TempDir
    File tempFolder;

    private BiConsumer<IFile, String> caseSelectionConsumer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void beforeTest() {
        caseSelectionConsumer = mock(BiConsumer.class);
    }

    @Test
    public void nothingIsDone_whenNodeIsNull() throws Exception {
        goToFile(null);

        verifyNoInteractions(caseSelectionConsumer);
    }

    @Test
    public void nothingIsDone_whenNodePathIsNull() throws Exception {
        goToFile(ExecutionTreeNode.newSuiteNode(null, "null_path", null));
        goToFile(ExecutionTreeNode.newTestNode(null, "null_path", null));

        verifyNoInteractions(caseSelectionConsumer);
    }

    @Test
    public void nothingIsDone_whenNodePathDoesNotExist() throws Exception {
        goToFile(ExecutionTreeNode.newSuiteNode(null, "not_existing_path", new URI("file:///unknown.robot")));
        goToFile(ExecutionTreeNode.newTestNode(null, "not_existing_path", new URI("file:///unknown.robot")));

        verifyNoInteractions(caseSelectionConsumer);
    }

    @Test
    public void nothingIsDone_whenNodePathIsNotFromWorkspace() throws Exception {
        final File nonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_test.robot");

        goToFile(ExecutionTreeNode.newSuiteNode(null, "not_linked_file", nonWorkspaceFile.toURI()));
        goToFile(ExecutionTreeNode.newTestNode(null, "not_linked_file", nonWorkspaceFile.toURI()));

        verifyNoInteractions(caseSelectionConsumer);
    }

    @Test
    public void caseIsOpened_whenNodePathPointsToProjectFile() throws Exception {
        final IFile suite = createFile(project, "suite.robot");

        goToFile(ExecutionTreeNode.newSuiteNode(null, "project_file", suite.getLocationURI()));

        verify(caseSelectionConsumer).accept(suite, "project_file");
        verifyNoMoreInteractions(caseSelectionConsumer);
    }

    @Test
    public void caseIsOpened_whenNodePathPointsToLinkedFile() throws Exception {
        final File nonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_test.robot");
        final IFile linkedSuite = getFile(project, "linkedSuite.robot");
        linkedSuite.createLink(nonWorkspaceFile.toURI(), IResource.REPLACE, null);

        goToFile(ExecutionTreeNode.newSuiteNode(null, "linked_file", nonWorkspaceFile.toURI()));

        verify(caseSelectionConsumer).accept(linkedSuite, "linked_file");
        verifyNoMoreInteractions(caseSelectionConsumer);
    }

    private void goToFile(final ExecutionTreeNode caseNode) {
        GoToFileHandler.E4GoToFileHandler.openExecutionNodeSourceFile(caseNode, caseSelectionConsumer);
    }
}
