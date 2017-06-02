/*
 * Copyright 2017 Nokia Solutions and Networks
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

public class ResourceImportsPathsResolverTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ResourceImportsPathsResolverTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
        projectProvider.createDir("a");
        projectProvider.createDir("a/b");
        projectProvider.createDir("a/b/c");
        projectProvider.createFile("res1.robot");
        projectProvider.createFile("a/res2.robot");
        projectProvider.createFile("a/b/res3.robot");
        projectProvider.createFile("a/b/c/res4.robot");
    }

    @Test
    public void nothingIsReturned_whenNoImportExists() throws Exception {
        final String[] importSection = createResourceImportSection();
        final RobotSuiteFile suiteFile = createSuiteFile("test.robot", importSection);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).isEmpty();
    }

    @Test
    public void onlyExistingResourceFilePathsAreSkipped() throws Exception {
        final String[] importSection = createResourceImportSection("res1.robot", "not_existing.robot", "a/res2.robot",
                "xyz.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("test.robot", importSection);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res1.robot").getFullPath(),
                projectProvider.getFile("a/res2.robot").getFullPath());
    }

    @Test
    public void resourceFilePathAreRetrievedInImportOrder() throws Exception {
        final String[] importSection = createResourceImportSection("a/res2.robot", "res1.robot", "a/b/res3.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("test.robot", importSection);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("a/res2.robot").getFullPath(),
                projectProvider.getFile("res1.robot").getFullPath(),
                projectProvider.getFile("a/b/res3.robot").getFullPath());
    }

    @Test
    public void resourceFilePathIsRetrieved_whenVariableIsUsedInImportPath() throws Exception {
        final String[] importSection = createResourceImportSection("${execdir}/res1.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("test.robot", importSection);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res1.robot").getFullPath());
    }

    @Test
    public void resourceFilePathIsRetrieved_whenAbsoluteImportPathIsUsed() throws Exception {
        final String[] importSection = createResourceImportSection(
                projectProvider.getProject().getLocation().toString() + "/a/res2.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("test.robot", importSection);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("a/res2.robot").getFullPath());
    }

    @Test
    public void resourceFilePathIsRetrieved_whenRelativeImportPathIsUsed() throws Exception {
        final String[] importSection = createResourceImportSection("../../res1.robot", "../res2.robot", "./res3.robot",
                "c/res4.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("a/b/test.robot", importSection);

        final List<IPath> filesPaths = ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suiteFile);

        assertThat(filesPaths).containsExactly(projectProvider.getFile("res1.robot").getFullPath(),
                projectProvider.getFile("a/res2.robot").getFullPath(),
                projectProvider.getFile("a/b/res3.robot").getFullPath(),
                projectProvider.getFile("a/b/c/res4.robot").getFullPath());
    }

    private static String[] createResourceImportSection(final String... resourcePaths) {
        final String[] result = new String[resourcePaths.length + 1];
        result[0] = "*** Settings ***";
        for (int i = 0; i < resourcePaths.length; i++) {
            result[i + 1] = "Resource  " + resourcePaths[i];
        }
        return result;
    }

    private static RobotSuiteFile createSuiteFile(final String filePath, final String... lines)
            throws IOException, CoreException {
        final IFile sourceFile = projectProvider.createFile(filePath, lines);
        return new RobotModel().createSuiteFile(sourceFile);
    }

}
