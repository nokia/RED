/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class RobotSuitesNamingTest {

    private static final String PROJECT_NAME = RobotSuitesNamingTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Test
    public void testDifferentPathsToNamesConversions() {
        assertThat(suiteNameFor("")).isEqualTo(PROJECT_NAME);
        assertThat(suiteNameFor("a")).isEqualTo(PROJECT_NAME + ".A");
        assertThat(suiteNameFor("a/b/c")).isEqualTo(PROJECT_NAME + ".A.B.C");
        assertThat(suiteNameFor("abc")).isEqualTo(PROJECT_NAME + ".Abc");
        assertThat(suiteNameFor("some path/to suite")).isEqualTo(PROJECT_NAME + ".Some Path.To Suite");
        assertThat(suiteNameFor("a/001__b/c")).isEqualTo(PROJECT_NAME + ".A.B.C");
    }

    private static String suiteNameFor(final String path) {
        final IProject project = projectProvider.getProject();
        return RobotSuitesNaming.createSuiteName(project, new Path(path));
    }
}
