/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.script.ScriptRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RuntimeEnvironmentsMocks;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class LaunchConfigurationsValidatorTest {

    private static final String PROJECT_NAME = LaunchConfigurationsValidatorTest.class.getSimpleName();

    private static final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

    private IProject project;

    private final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Before
    public void setup() throws CoreException {
        removeAllConfigurations();
        project = projectProvider.getProject();
    }

    @AfterClass
    public static void clean() throws CoreException {
        removeAllConfigurations();
    }

    private static void removeAllConfigurations() throws CoreException {
        final ILaunchConfigurationType[] types = manager.getLaunchConfigurationTypes();
        for (final ILaunchConfigurationType type : types) {
            final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);
            for (final ILaunchConfiguration config : launchConfigs) {
                config.delete();
            }
        }
    }

    @Test
    public void whenProjectNameIsEmpty_fatalExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '' does not exist in workspace.");

        validator.validate(createRobotLaunchConfiguration(""));
    }

    @Test
    public void whenProjectDoesNotExist_fatalExceptionIsThrown() throws Exception {
        project.delete(true, null);

        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' does not exist in workspace.");

        validator.validate(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsClosed_fatalExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is currently closed.");
        project.close(null);

        validator.validate(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_1() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment.");

        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        validator.validate(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_2() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment.");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidPythonEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        validator.validate(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_3() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(
                "Project '" + PROJECT_NAME + "' is using invalid Python environment (missing Robot Framework).");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidRobotEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        validator.validate(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenThereAreNoSuitesSpecified_warningExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationException.class);
        thrown.expectMessage("There are no suites specified. All suites in '" + PROJECT_NAME + "' will be executed.");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        validator.validate(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenSystemInterpreterIsUsed_warningExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationException.class);
        thrown.expectMessage(CoreMatchers
                .equalTo("Tests will be launched using 'Python' interpreter as defined in PATH environment variable."));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", "  Log  10");

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator();
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setUsingInterpreterFromProject(false);
        launchConfig.setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList()));
        validator.validate(launchConfig);
    }

    @Test
    public void warningsAreCombinedTogetherInSingleException() throws Exception {
        thrown.expect(LaunchConfigurationValidationException.class);
        thrown.expectMessage(CoreMatchers
                .equalTo("Tests will be launched using 'Python' interpreter as defined in PATH environment variable.\n"
                        + "There are no suites specified. All suites in '" + PROJECT_NAME + "' will be executed."));

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator();
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setUsingInterpreterFromProject(false);
        validator.validate(launchConfig);
    }

    @Test
    public void whenSuitesSpecifiedToRunDoesNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(CoreMatchers
                .<String> either(CoreMatchers.<String> equalTo("Following suites does not exist: /" + PROJECT_NAME
                        + "/file2.robot, /" + PROJECT_NAME + "/suite/dir."))
                .or(CoreMatchers.<String> equalTo("Following suites does not exist: /" + PROJECT_NAME + "/suite/dir, /"
                        + PROJECT_NAME + "/file2.robot.")));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", " Log 10");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList(), "file2.robot",
                newArrayList(), "suite/dir", newArrayList()));
        validator.validate(launchConfig);
    }

    @Test
    public void whenTestsSpecifiedInSuiteDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(CoreMatchers.equalTo("Following tests does not exist: case4, case5."));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20", "case3",
                "  Log  30");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig
                .setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList("case3", "case4", "case5")));
        validator.validate(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithGivenConfiguration() throws Exception, CoreException {
        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20", "case3",
                "  Log  30");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationsValidator validator = new LaunchConfigurationsValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList("case2", "case3")));
        validator.validate(launchConfig);
    }

    @Test
    public void whenRemoteProjectNameIsEmpty_fatalExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '' does not exist in workspace.");

        validator.validate(createRemoteRobotLaunchConfiguration(""));
    }

    @Test
    public void whenHostIsEmpty_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Server IP cannot be empty");

        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setRemoteHostValue("");
        validator.validate(launchConfig);
    }

    @Test
    public void whenPortIsNotCorrect_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Server port 'xyz' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setRemotePortValue("xyz");
        validator.validate(launchConfig);
    }

    @Test
    public void whenTimeoutIsNotCorrect_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Connection timeout 'xyz' must be an Integer between 1 and 3,600");

        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setRemoteTimeoutValue("xyz");
        validator.validate(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithGivenRemoteConfiguration() throws Exception {
        validator.validate(createRemoteRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void projectIsNotValidatedWhenNotDefinedDirectly() throws Exception {
        final ScriptRobotLaunchConfiguration launchConfig = createScriptRobotLaunchConfiguration("");
        validator.validate((IRemoteRobotLaunchConfiguration) launchConfig);
    }

    @Test
    public void whenScriptFilePathIsEmpty_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Executor script file path is not defined.");

        final ScriptRobotLaunchConfiguration launchConfig = createScriptRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setScriptPath("");
        validator.validate(launchConfig);
    }

    @Test
    public void whenScriptFileDoesNotExist_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Executor script file does not exist.");

        final ScriptRobotLaunchConfiguration launchConfig = createScriptRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setScriptPath("/not/existing/path");
        validator.validate(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithGivenScriptConfiguration() throws Exception {
        final IFile scriptFile = projectProvider.createFile("robot_script_file.txt", "run robot command");
        final IFile testFile = projectProvider.createFile("test.robot", "*** Test Cases ***", "case1", "  Log  1");

        final ScriptRobotLaunchConfiguration launchConfig = createScriptRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setScriptPath(scriptFile.getLocation().toOSString());
        launchConfig.setSuitePaths(ImmutableMap.of(testFile.getName(), newArrayList("case1")));
        validator.validate(launchConfig);
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = manager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID).newInstance(null, "robot");
        final RobotLaunchConfiguration launchConfig = new RobotLaunchConfiguration(configuration);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

    private RemoteRobotLaunchConfiguration createRemoteRobotLaunchConfiguration(final String projectName)
            throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = manager
                .getLaunchConfigurationType(RemoteRobotLaunchConfiguration.TYPE_ID).newInstance(null, "remote");
        final RemoteRobotLaunchConfiguration launchConfig = new RemoteRobotLaunchConfiguration(configuration);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

    private ScriptRobotLaunchConfiguration createScriptRobotLaunchConfiguration(final String projectName)
            throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = manager
                .getLaunchConfigurationType(ScriptRobotLaunchConfiguration.TYPE_ID).newInstance(null, "script");
        final ScriptRobotLaunchConfiguration launchConfig = new ScriptRobotLaunchConfiguration(configuration);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

}
