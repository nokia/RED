/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

public class CreateLinkedFolderFixerTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(CreateLinkedFolderFixerTest.class);

    public TemporaryFolder tempFolder = new TemporaryFolder();

    private RobotModel model;

    @Rule
    public TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @Before
    public void before() throws Exception {

        model = new RobotModel();
        projectProvider.configure();
    }

    @Test
    public void linkedFolderIsCreated_whenResourceFileIsImportedFromExternalLocationViaAbsolutePath() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "resource.robot");
        final String path = tmpFile.getCanonicalPath().replaceAll("\\\\", "/");
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path));

        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        executeCreateFolderOperation(path, path, suite, marker);

        final IPath newFolderPath = marker.getResource().getParent().getFullPath().append("external_dir");
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithParentName_whenResourceFileIsImportedFromExternalLocationViaAbsolutePath()
            throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "parent_dir", "external_dir", "resource.robot");
        final String path = tmpFile.getCanonicalPath().replaceAll("\\\\", "/");
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path));
        projectProvider.createDir("external_dir");

        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        executeCreateFolderOperation(path, path, suite, marker);

        final IPath newFolderPath = marker.getResource().getParent().getFullPath().append("parent_dir_external_dir");
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);
        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreated_whenResourceFileIsImportedFromExternalLocationViaRelativePath() throws Exception {
        final String path = "../../../../../../../../../external_dir/resource.robot";
        final String absolutePath = "/external_dir/resource.robot";
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path));

        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        executeCreateFolderOperation(absolutePath, path, suite, marker);

        final IPath newFolderPath = marker.getResource().getParent().getFullPath().append("external_dir");
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithParentName_whenResourceFileIsImportedFromExternalLocationViaRelativePath()
            throws Exception {
        final String path = "../../../../../../../../../parent_dir/external_dir/resource.robot";
        final String absolutePath = "/parent_dir/external_dir/resource.robot";
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path));
        projectProvider.createDir("external_dir");

        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        executeCreateFolderOperation(absolutePath, path, suite, marker);

        final IPath newFolderPath = marker.getResource().getParent().getFullPath().append("parent_dir_external_dir");
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithPrefix_whenResourceFileIsImportedFromExternalLocationViaRelativePath()
            throws Exception {
        final String path = "../../../../../../../../../external_dir/resource.robot";
        final String absolutePath = "/external_dir/resource.robot";
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path));
        projectProvider.createDir("external_dir");

        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        executeCreateFolderOperation(absolutePath, path, suite, marker);

        final IPath newFolderPath = marker.getResource().getParent().getFullPath().append("external_dir(1)");
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);

        assertThat(newFolderHandle.exists()).isTrue();
    }

    @Test
    public void linkedFolderIsCreatedWithIncrementedPrefix_whenResourceFileIsImportedFromExternalLocationViaRelativePath()
            throws Exception {
        final String path = "../../../../../../../../../external_dir/resource.robot";
        final String absolutePath = "/external_dir/resource.robot";
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***", "Resource    " + path));
        projectProvider.createDir("external_dir");
        projectProvider.createDir("external_dir(1)");

        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        executeCreateFolderOperation(absolutePath, path, suite, marker);

        final IPath newFolderPath = marker.getResource().getParent().getFullPath().append("external_dir(2)");
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);

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

    private void executeCreateFolderOperation(final String absolutePath, final String path, final RobotSuiteFile suite,
            final IMarker marker)
            throws Exception {
        final CreateLinkedFolderFixer fixer = new CreateLinkedFolderFixer(absolutePath, path);
        marker.setAttribute(AdditionalMarkerAttributes.PATH, absolutePath);
        marker.setAttribute(AdditionalMarkerAttributes.VALUE, path);
        fixer.executeCreateFolderOperation(new NullProgressMonitor(), marker);
    }
}
