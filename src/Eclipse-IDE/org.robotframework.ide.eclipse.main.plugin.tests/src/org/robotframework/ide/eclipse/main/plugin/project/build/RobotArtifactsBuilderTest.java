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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.project.NullRobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExecutionEnvironment;
import org.rf.ide.core.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
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
        final IRuntimeEnvironment env = new NullRuntimeEnvironment();
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ProjectConfigurationProblem.ENVIRONMENT_MISSING, new ProblemPosition(1)));
    }

    @Test
    public void missingEnvironmentProblemIsReported_withLocation() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new NullRuntimeEnvironment();
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();
        configuration.setExecutionEnvironment(ExecutionEnvironment.create("not_existing", SuiteExecutor.Python));

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ProjectConfigurationProblem.ENVIRONMENT_MISSING, new ProblemPosition(1)));
    }

    @Test
    public void notPythonProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new InvalidPythonRuntimeEnvironment(new File("path"));
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_NOT_A_PYTHON, new ProblemPosition(1)));
    }

    @Test
    public void missingRobotProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new MissingRobotRuntimeEnvironment(null);
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();

        assertThat(builder.provideRuntimeEnvironment(robotProject, configuration, reporter)).isSameAs(env);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_HAS_NO_ROBOT, new ProblemPosition(1)));
    }

    @Test
    public void deprecatedRobotProblemIsReported_whenPythonInstallationIsNotDeprecated() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new RobotRuntimeEnvironment(null,
                "Robot Framework 2.8.2 (Python 2.7.1 on win32)");
        builder.checkRuntimeEnvironment(env, robotProject, reporter);

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_DEPRECATED_ROBOT, new ProblemPosition(1)));
    }

    @Test
    public void deprecatedRobotProblemIsReported_whenPythonInstallationIsDeprecated() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new RobotRuntimeEnvironment(null,
                "Robot Framework 2.8.2 (Python 2.6.1 on win32)");
        builder.checkRuntimeEnvironment(env, robotProject, reporter);

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_DEPRECATED_ROBOT, new ProblemPosition(1)));
    }

    @Test
    public void noProblemsAreReported_whenRobotInstallationIsNotDeprecated() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new RobotRuntimeEnvironment(null,
                "Robot Framework 2.9 (Python 2.7.1 on win32)");
        builder.checkRuntimeEnvironment(env, robotProject, reporter);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void deprecatedPythonProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new RobotRuntimeEnvironment(null,
                "Robot Framework 3.0.4 (Python 2.6.6 on win32)");
        builder.checkRuntimeEnvironment(env, robotProject, reporter);

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.ENVIRONMENT_DEPRECATED_PYTHON, new ProblemPosition(1)));
    }

    @Test
    public void noProblemsAreReported_whenEnvironmentCanBeProvided() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        final IRuntimeEnvironment env = new RobotRuntimeEnvironment(null,
                "Robot Framework 3.0.4 (Python 3.7.1 on win32)");
        when(robotProject.getRuntimeEnvironment()).thenReturn(env);
        final RobotProjectConfig configuration = new RobotProjectConfig();
        builder.provideRuntimeEnvironment(robotProject, configuration, reporter);
        builder.checkRuntimeEnvironment(env, robotProject, reporter);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void missingConfigProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        when(robotProject.getConfigurationFile()).thenReturn(mock(IFile.class));

        assertThat(builder.provideConfiguration(robotProject, reporter))
                .isExactlyInstanceOf(NullRobotProjectConfig.class);
        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ProjectConfigurationProblem.CONFIG_FILE_MISSING, new ProblemPosition(1)));
    }

    @Test
    public void readingConfigProblemIsReported() throws Exception {
        final RobotProject robotProject = spy(model.createRobotProject(projectProvider.getProject()));
        when(robotProject.readRobotProjectConfig()).thenThrow(CannotReadProjectConfigurationException.class);

        assertThat(builder.provideConfiguration(robotProject, reporter))
                .isExactlyInstanceOf(NullRobotProjectConfig.class);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ProjectConfigurationProblem.CONFIG_FILE_READING_PROBLEM, new ProblemPosition(1)));
    }

    @Test
    public void noProblemsAreReported_whenConfigCanBeProvided() throws Exception {
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());

        assertThat(builder.provideConfiguration(robotProject, reporter)).isExactlyInstanceOf(RobotProjectConfig.class);
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

}
