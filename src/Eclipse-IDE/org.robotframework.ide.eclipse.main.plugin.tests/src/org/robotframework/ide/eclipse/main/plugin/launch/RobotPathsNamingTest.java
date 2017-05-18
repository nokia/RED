/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class RobotPathsNamingTest {

    private static final String PROJECT_NAME = RobotPathsNamingTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Test
    public void testDifferentPathsToSuiteNamesConversions() {
        assertThat(suiteNameFor("")).isEqualTo(PROJECT_NAME);
        assertThat(suiteNameFor("a")).isEqualTo(PROJECT_NAME + ".A");
        assertThat(suiteNameFor("a/b/c")).isEqualTo(PROJECT_NAME + ".A.B.C");
        assertThat(suiteNameFor("abc")).isEqualTo(PROJECT_NAME + ".Abc");
        assertThat(suiteNameFor("some path/to suite")).isEqualTo(PROJECT_NAME + ".Some Path.To Suite");
        assertThat(suiteNameFor("a/001__b/c")).isEqualTo(PROJECT_NAME + ".A.B.C");
    }

    @Test
    public void testSuiteNameConversionForResource() throws Exception {
        final IFile suite = projectProvider.createFile("some test suite.robot");
        assertThat(RobotPathsNaming.createSuiteName(suite)).isEqualTo(PROJECT_NAME + ".Some Test Suite");
    }

    @Test
    public void testTestNameConversion() throws Exception {
        final IProject project = projectProvider.getProject();
        assertThat(RobotPathsNaming.createTestName(project, new Path("suite.robot"), "test case"))
                .isEqualTo(PROJECT_NAME + ".Suite.test case");
    }

    private static String suiteNameFor(final String path) {
        final IProject project = projectProvider.getProject();
        return RobotPathsNaming.createSuiteName(project, new Path(path));
    }
}
