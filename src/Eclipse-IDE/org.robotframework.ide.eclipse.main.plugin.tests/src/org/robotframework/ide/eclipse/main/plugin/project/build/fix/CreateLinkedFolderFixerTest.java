/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robotframework.red.junit.ProjectProvider;

public class CreateLinkedFolderFixerTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(CreateLinkedFolderFixerTest.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void linkedFolderIsCreated_whenResourceFileIsImportedFromExternalLocationViaAbsolutePath() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "resource.robot");
        final String path = tmpFile.getCanonicalPath().replaceAll("\\\\", "/");
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path);

        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(path, path);
        fixer.executeCreateFolderOperation(file, null);

        final IPath newFolderPath = file.getParent().getFullPath().append("external_dir");
        final IFolder newFolderHandle = file.getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithParentName_whenResourceFileIsImportedFromExternalLocationViaAbsolutePath()
            throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "parent_dir", "external_dir", "resource.robot");
        final String path = tmpFile.getCanonicalPath().replaceAll("\\\\", "/");
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path);
        projectProvider.createDir("external_dir");

        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(path, path);
        fixer.executeCreateFolderOperation(file, null);

        final IPath newFolderPath = file.getParent().getFullPath().append("parent_dir_external_dir");
        final IFolder newFolderHandle = file.getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreated_whenResourceFileIsImportedFromExternalLocationViaRelativePath() throws Exception {
        final String path = "../../../../../../../../../external_dir/resource.robot";
        final String absolutePath = "/external_dir/resource.robot";
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path);

        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(absolutePath, path);
        fixer.executeCreateFolderOperation(file, null);

        final IPath newFolderPath = file.getParent().getFullPath().append("external_dir");
        final IFolder newFolderHandle = file.getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithParentName_whenResourceFileIsImportedFromExternalLocationViaRelativePath()
            throws Exception {
        final String path = "../../../../../../../../../parent_dir/external_dir/resource.robot";
        final String absolutePath = "/parent_dir/external_dir/resource.robot";
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path);
        projectProvider.createDir("external_dir");

        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(absolutePath, path);
        fixer.executeCreateFolderOperation(file, null);

        final IPath newFolderPath = file.getParent().getFullPath().append("parent_dir_external_dir");
        final IFolder newFolderHandle = file.getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithPrefix_whenResourceFileIsImportedFromExternalLocationViaRelativePath()
            throws Exception {
        final String path = "../../../../../../../../../external_dir/resource.robot";
        final String absolutePath = "/external_dir/resource.robot";
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path);
        projectProvider.createDir("external_dir");

        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(absolutePath, path);
        fixer.executeCreateFolderOperation(file, null);

        final IPath newFolderPath = file.getParent().getFullPath().append("external_dir(1)");
        final IFolder newFolderHandle = file.getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithIncrementedPrefix_whenResourceFileIsImportedFromExternalLocationViaRelativePath()
            throws Exception {
        final String path = "../../../../../../../../../external_dir/resource.robot";
        final String absolutePath = "/external_dir/resource.robot";
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path);
        projectProvider.createDir("external_dir");
        projectProvider.createDir("external_dir(1)");

        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(absolutePath, path);
        fixer.executeCreateFolderOperation(file, null);

        final IPath newFolderPath = file.getParent().getFullPath().append("external_dir(2)");
        final IFolder newFolderHandle = file.getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void fixerExistInFixers_whenResourceIsLocatedOutsideWorkspace() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "non_existing.robot");
        final String path = tmpFile.getCanonicalPath().replaceAll("\\\\", "/");
        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(path, path);

        assertThat(fixer.getLabel().equals("Create Linked Folder for '" + path + "' resource"));
    }

    private static File getFile(final File root, final String... path) {
        if (path == null || path.length == 0) {
            return root;
        } else {
            return getFile(new File(root, path[0]), Arrays.copyOfRange(path, 1, path.length));
        }
    }
}
