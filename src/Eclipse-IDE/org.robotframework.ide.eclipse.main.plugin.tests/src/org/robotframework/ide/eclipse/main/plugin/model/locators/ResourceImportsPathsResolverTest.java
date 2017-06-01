/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ObjectArrays;

public class ResourceImportsPathsResolverTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ResourceImportsPathsResolverTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
        projectProvider.createFile("res1.robot");
        projectProvider.createFile("res2.robot");
    }

    @Test
    public void nothingIsReturned_whenNoImportExists() throws Exception {
        final RobotSuiteFile suiteFile = createSuiteFile("*** Settings ***");

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).isEmpty();
    }

    @Test
    public void onlyExistingResourceFilePathsAreSkipped() throws Exception {
        final String[] resourceImportSettingSections = ObjectArrays.concat(
                createResourceImportSettingsSection("res1.robot"),
                createResourceImportSettingsSection("not_existing.robot"), String.class);
        final RobotSuiteFile suiteFile = createSuiteFile(resourceImportSettingSections);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res1.robot").getFullPath());
    }

    @Test
    public void resourceFilePathAreRetrievedInImportOrder() throws Exception {
        final String[] resourceImportSettingSections = ObjectArrays.concat(
                createResourceImportSettingsSection("res2.robot"), createResourceImportSettingsSection("res1.robot"),
                String.class);
        final RobotSuiteFile suiteFile = createSuiteFile(resourceImportSettingSections);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res2.robot").getFullPath(),
                projectProvider.getFile("res1.robot").getFullPath());
    }

    @Test
    public void resourceFilePathIsRetrieved_whenVariableIsUsedInImportPath() throws Exception {
        final String[] resourceImportSettingSections = createResourceImportSettingsSection("${execdir}/res1.robot");
        final RobotSuiteFile suiteFile = createSuiteFile(resourceImportSettingSections);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res1.robot").getFullPath());
    }

    @Test
    public void resourceFilePathIsRetrieved_whenAbsoluteImportPathIsUsed() throws Exception {
        final String[] resourceImportSettingSections = createResourceImportSettingsSection(
                projectProvider.getProject().getLocation().toString() + "/res1.robot");
        final RobotSuiteFile suiteFile = createSuiteFile(resourceImportSettingSections);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res1.robot").getFullPath());
    }

    private static String[] createResourceImportSettingsSection(final String resourcePath) {
        return new String[] { "*** Settings ***", "Resource  " + resourcePath };
    }

    private static RobotSuiteFile createSuiteFile(final String... lines) throws IOException, CoreException {
        final IFile sourceFile = projectProvider.createFile("test.robot", lines);
        return new RobotModel().createSuiteFile(sourceFile);
    }

}
