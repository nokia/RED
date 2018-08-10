/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExecutionEnvironment;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.red.junit.ProjectProvider;

public class RobotArtifactsBuilderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotArtifactsBuilderTest.class);

    private RobotModel model;

    private MockReporter reporter;

    private RobotArtifactsBuilder builder;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
    }

    @Before
    public void beforeTest() throws Exception {
        model = new RobotModel();
        reporter = new MockReporter();
        builder = new RobotArtifactsBuilder(projectProvider.getProject(), new BuildLogger());
    }

    @Test
    public void missingEnvironmentProblemIsReported_withoutLocation() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isNull();
        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ProjectConfigurationProblem.ENVIRONMENT_MISSING, new ProblemPosition(1)));
    }

    @Test
    public void missingEnvironmentProblemIsReported_withLocation() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);
        final RobotProjectConfig configuration = new RobotProjectConfig();
        configuration.setExecutionEnvironment(ExecutionEnvironment.create("not_existing", SuiteExecutor.Python));

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isNull();
        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ProjectConfigurationProblem.ENVIRONMENT_MISSING, new ProblemPosition(1)));
    }

    @Test
    public void notPythonProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.isValidPythonInstallation()).thenReturn(false);
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_NOT_A_PYTHON, new ProblemPosition(1)));
    }

    @Test
    public void missingRobotProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.isValidPythonInstallation()).thenReturn(true);
        when(env.hasRobotInstalled()).thenReturn(false);
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_HAS_NO_ROBOT, new ProblemPosition(1)));
    }

    @Test
    public void deprecatedPythonProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.isValidPythonInstallation()).thenReturn(true);
        when(env.hasRobotInstalled()).thenReturn(true);
        when(env.isCompatibleRobotInstallation()).thenReturn(false);
        when(env.getVersion()).thenReturn("Robot Framework 3.0.4 (Python 2.6.6 on win32)");
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_DEPRECATED_PYTHON, new ProblemPosition(1)));
    }

    @Test
    public void noProblemsAreReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.isValidPythonInstallation()).thenReturn(true);
        when(env.hasRobotInstalled()).thenReturn(true);
        when(env.isCompatibleRobotInstallation()).thenReturn(true);
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

}
