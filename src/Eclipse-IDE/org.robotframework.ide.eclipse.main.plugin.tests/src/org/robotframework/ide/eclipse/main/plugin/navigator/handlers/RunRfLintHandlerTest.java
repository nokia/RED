/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.rflint.RfLintIntegrationServer;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

public class RunRfLintHandlerTest {

    private static final String PROJECT_NAME = RunRfLintHandlerTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    private static IFile suite;

    private static RfLintIntegrationServer server;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        suite = projectProvider.createFile("suite.robot");
        server = mock(RfLintIntegrationServer.class);
        when(server.getHost()).thenReturn("1.2.3.4");
        when(server.getPort()).thenReturn(1234);
    }

    @Test
    public void rfLintAnalysisIsRun_forEmptyConfiguration() throws Exception {
        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        final RobotProject robotProject = createRobotProjectSpy(environment, new RobotProjectConfig());
        final RedPreferences preferences = mock(RedPreferences.class);

        RunRfLintHandler.E4RunRfLintHandler.runRfLint(robotProject, suite, server, preferences);

        verify(environment).runRfLint("1.2.3.4", 1234, projectProvider.getProject().getLocation().toFile(),
                new ArrayList<>(), suite.getLocation().toFile(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
    }

    @Test
    public void rfLintAnalysisIsRun_withExcludedPathsTakenFromProjectConfig() throws Exception {
        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.addExcludedPath("x");
        projectConfig.addExcludedPath("x/y");
        projectConfig.addExcludedPath("x/y/z");
        final RobotProject robotProject = createRobotProjectSpy(environment, projectConfig);
        final RedPreferences preferences = mock(RedPreferences.class);

        RunRfLintHandler.E4RunRfLintHandler.runRfLint(robotProject, suite, server, preferences);

        verify(environment).runRfLint("1.2.3.4", 1234, projectProvider.getProject().getLocation().toFile(),
                newArrayList("x", "x/y", "x/y/z"), suite.getLocation().toFile(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
    }

    @Test
    public void rfLintAnalysisIsRun_withRulesTakenFromPreferences() throws Exception {
        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        final RobotProject robotProject = createRobotProjectSpy(environment, new RobotProjectConfig());
        final RedPreferences preferences = mock(RedPreferences.class);
        final List<RfLintRule> rules = newArrayList(new RfLintRule("x", RfLintViolationSeverity.ERROR, "1"),
                new RfLintRule("y", RfLintViolationSeverity.DEFAULT, "2"),
                new RfLintRule("z", RfLintViolationSeverity.IGNORE, "3"));
        when(preferences.getRfLintRules()).thenReturn(rules);

        RunRfLintHandler.E4RunRfLintHandler.runRfLint(robotProject, suite, server, preferences);

        verify(environment).runRfLint("1.2.3.4", 1234, projectProvider.getProject().getLocation().toFile(),
                new ArrayList<>(), suite.getLocation().toFile(), rules, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    public void rfLintAnalysisIsRun_withRuleFilesTakenFromPreferences() throws Exception {
        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        final RobotProject robotProject = createRobotProjectSpy(environment, new RobotProjectConfig());
        final RedPreferences preferences = mock(RedPreferences.class);
        final List<String> ruleFiles = newArrayList(projectProvider.createFile("rule1.py").getLocation().toOSString(),
                projectProvider.createFile("rule2.py").getLocation().toOSString());
        when(preferences.getRfLintRulesFiles()).thenReturn(ruleFiles);

        RunRfLintHandler.E4RunRfLintHandler.runRfLint(robotProject, suite, server, preferences);

        verify(environment).runRfLint("1.2.3.4", 1234, projectProvider.getProject().getLocation().toFile(),
                new ArrayList<>(), suite.getLocation().toFile(), new ArrayList<>(), ruleFiles, new ArrayList<>());
    }

    @Test
    public void rfLintAnalysisIsRun_withAdditionalArgumentsTakenFromPreferences() throws Exception {
        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        final RobotProject robotProject = createRobotProjectSpy(environment, new RobotProjectConfig());
        final RedPreferences preferences = mock(RedPreferences.class);
        final IFile argFile = projectProvider.createFile("argfile.arg");
        when(preferences.getRfLintAdditionalArguments())
                .thenReturn("arg ${var} -A ${workspace_loc:/" + PROJECT_NAME + "/" + argFile.getName() + "}");

        RunRfLintHandler.E4RunRfLintHandler.runRfLint(robotProject, suite, server, preferences);

        verify(environment).runRfLint("1.2.3.4", 1234, projectProvider.getProject().getLocation().toFile(),
                new ArrayList<>(), suite.getLocation().toFile(), new ArrayList<>(), new ArrayList<>(),
                newArrayList("arg", "${var}", "-A", argFile.getLocation().toOSString()));
    }

    private RobotProject createRobotProjectSpy(final RobotRuntimeEnvironment environment,
            final RobotProjectConfig projectConfig) {
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);
        when(robotProject.getRobotProjectConfig()).thenReturn(projectConfig);
        return robotProject;
    }
}
