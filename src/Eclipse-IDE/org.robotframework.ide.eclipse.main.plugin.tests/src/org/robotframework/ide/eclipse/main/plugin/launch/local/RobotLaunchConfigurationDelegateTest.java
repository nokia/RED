/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationDelegate.ConsoleData;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RuntimeEnvironmentsMocks;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @BeforeClass
    public static void before() throws Exception {
        projectProvider.createDir(Path.fromPortableString("001__suites_a"));
        projectProvider.createFile(Path.fromPortableString("001__suites_a/s1.robot"), "*** Test Cases ***",
                "001__case1", "  Log  10", "001__case2", "  Log  20");
    }

    @Test
    public void commandLineStartsWithInterpreterPath_whenActiveRuntimeEnvironmentIsUsed() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setInterpreterArguments("-a1 -a2");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).startsWith("some/path/to/python", "-a1", "-a2", "-m", "robot.run");
    }

    @Test
    public void commandLineStartsWithInterpreterName_whenProjectInterpreterIsNotUsed() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setInterpreterArguments("-a1 -a2");
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.PyPy);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).startsWith(SuiteExecutor.PyPy.executableName(), "-a1", "-a2", "-m",
                "robot.run");
    }

    @Test
    public void commandLineStartsWithDefaultInterpreterName_whenThereIsNoActiveRuntimeEnvironment() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setInterpreterArguments("-a1 -a2");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).startsWith(SuiteExecutor.Python.executableName(), "-a1", "-a2", "-m",
                "robot.run");
    }

    @Test
    public void commandLineTranslatesSuitesNames_whenNamesContainsDoubleUnderscores() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList()));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getArgumentFile().get().generateContent())
                .contains("--suite      " + PROJECT_NAME + ".Suites_a");
    }

    @Test
    public void commandLineDoesNotTranslateTestNames_whenNamesContainsDoubleUnderscores() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList("001__case1")));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String argFileContent = commandLine.getArgumentFile().get().generateContent();
        assertThat(argFileContent).contains("-suite", PROJECT_NAME + ".Suites_a");
        assertThat(argFileContent).contains("-test", PROJECT_NAME + ".Suites_a.001__case1");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_1() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create("folder1");
        final SearchPath searchPath2 = SearchPath.create("folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        projectProvider.configure(config);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains("-pythonpath " + projectAbsPath
                + File.separator + "folder1:" + projectAbsPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_2() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create(PROJECT_NAME + "/folder1");
        final SearchPath searchPath2 = SearchPath.create(PROJECT_NAME + "/folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));
        projectProvider.configure(config);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains("--pythonpath " + projectAbsPath
                + File.separator + "folder1:" + projectAbsPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsTags() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(newArrayList("EX_1", "EX_2"));
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIncludedTags(newArrayList("IN_1", "IN_2"));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getArgumentFile().get().generateContent()).containsSequence("--include    IN_1",
                "--include    IN_2", "--exclude    EX_1", "--exclude    EX_2");
    }

    @Test
    public void commandLineStartsWitExecutableFilePath() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final String executablePath = projectProvider.createFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).startsWith(executablePath);
    }

    @Test
    public void commandLineContainsExecutableFilePathWithArguments() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final String executablePath = projectProvider.createFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);
        robotConfig.setExecutableFileArguments("-arg1 abc -arg2 xyz");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).containsSubsequence(executablePath, "-arg1", "abc", "-arg2", "xyz");
    }

    @Test
    public void coreExceptionIsThrown_whenExecutableFileDoesNotExist() throws Exception {
        final String executablePath = projectProvider.getFile("not_existing.bat").getLocation().toOSString();

        thrown.expect(CoreException.class);
        thrown.expectMessage("Executable file '" + executablePath + "' does not exist");

        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath(executablePath);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345, preferences);
    }

    @Test
    public void pathToSuiteIsUsed_whenSingleSuiteIsRunAndPreferenceIsSet() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList()));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String suitePath = projectProvider.createFile("001__suites_a/s1.robot").getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith(suitePath).doesNotContain("-s", "-t");
    }

    @Test
    public void pathToSuiteIsUsed_whenTestsFromSingleSuiteAreRunAndPreferenceIsSet() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList("001__case1")));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String suitePath = projectProvider.createFile("001__suites_a/s1.robot").getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith("-t", "001__case1", suitePath).doesNotContain("-s");
    }

    @Test
    public void pathToSuiteIsNotUsed_whenSeveralResourcesAreRunAndPreferenceIsSet() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(
                ImmutableMap.of("001__suites_a", newArrayList(), "001__suites_a/s1.robot", newArrayList()));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine())
                .endsWith("-s", PROJECT_NAME + ".Suites_a", "-s", PROJECT_NAME + ".Suites_a.S1", projectPath)
                .doesNotContain("-t");
    }

    @Test
    public void pathToSuiteIsNotUsed_whenSingleFolderIsRunAndPreferenceIsSet() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList()));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith("-s", PROJECT_NAME + ".Suites_a", projectPath)
                .doesNotContain("-t");
    }

    @Test
    public void pathToSuiteIsNotUsed_whenSingleSuiteIsRunAndPreferenceIsNotSet() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList()));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith("-s", PROJECT_NAME + ".Suites_a.S1", projectPath)
                .doesNotContain("-t");
    }

    @Test()
    public void pathToExecutableAndUnknownRobotVersionAreUsed_whenPathToExecutableIsSet() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath("some/path/to/script");

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/script");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("<unknown>");
    }

    @Test()
    public void pathToPythonAndKnownRobotVersionAreUsed_whenProjectInterpreterIsNotUsedAndPathToExecutableIsNotSet()
            throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.Python);

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);

        assertThat(consoleData.getProcessLabel()).endsWith(SuiteExecutor.Python.executableName());
        assertThat(consoleData.getSuiteExecutorVersion()).isNotEqualTo("<unknown>");
    }

    @Test()
    public void coreExceptionIsThrown_whenNotExistingInterpreterIsUsedAndPathToExecutableIsNotSet() throws Exception {
        boolean thereIsNoPypy;
        try {
            RobotRuntimeEnvironment.getVersion(SuiteExecutor.PyPy);

            thereIsNoPypy = false;
        } catch (final RobotEnvironmentException e) {
            thereIsNoPypy = true;
        }
        assumeTrue(thereIsNoPypy);

        thrown.expect(CoreException.class);
        thrown.expectMessage(
                "There is no " + SuiteExecutor.PyPy.name() + " interpreter in system PATH environment variable");

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.PyPy);

        RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);
    }

    @Test()
    public void pathToPythonAndKnownRobotVersionAreUsed_whenProjectInterpreterIsUsedAndPathToExecutableIsNotSet()
            throws Exception {
        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/python");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("RF 3");
    }

    @Test()
    public void coreExceptionIsThrown_whenThereIsNoActiveRuntimeEnvironment() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("There is no active runtime environment for project '" + PROJECT_NAME + "'");

        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);
    }

    @Test()
    public void coreExceptionIsThrown__whenActiveRuntimeEnvironmentIsNotValid() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("The runtime environment " + new File("some/path/to/python").getAbsolutePath()
                + " is either not a python installation or it has no Robot installed");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidRobotEnvironment();
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

    @Test
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("This configuration is incompatible with RED version you are currently using."
                + "\nExpected: " + RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION + ", but was: invalid"
                + "\n\nResolution: Delete old configurations manually and create the new ones.");

        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(PROJECT_NAME);

        final ILaunchConfigurationWorkingCopy launchCopy = configuration.getWorkingCopy();
        launchCopy.setAttribute("Version of configuration", "invalid");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        launchDelegate.launch(launchCopy, "run", null, null);
    }
}
