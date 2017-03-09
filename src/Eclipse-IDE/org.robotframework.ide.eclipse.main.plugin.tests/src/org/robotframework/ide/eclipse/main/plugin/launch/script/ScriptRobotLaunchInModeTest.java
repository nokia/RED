/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.junit.ProjectProvider;

public class ScriptRobotLaunchInModeTest {

    private static final String PROJECT_NAME = ScriptRobotLaunchInModeTest.class.getSimpleName();

    private static final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void before() throws Exception {
        projectProvider.createFile("executable_script.bat");
    }

    @Test
    public void commandLineStartsWitExecutableFilePath() throws Exception {
        final ScriptRobotLaunchConfiguration robotConfig = createScriptRobotLaunchConfiguration(PROJECT_NAME);
        final String executablePath = projectProvider.getFile("executable_script.bat").getLocation().toPortableString();
        robotConfig.setScriptPath(executablePath);

        final ScriptRobotLaunchInMode launchMode = createModeUnderTest();
        final RunCommandLine commandLine = launchMode.prepareCommandLine(robotConfig, 12345);

        assertThat(commandLine.getCommandLine()).startsWith(executablePath);
    }

    @Test
    public void commandLineContainsExecutableFilePathWithArguments() throws Exception {
        final ScriptRobotLaunchConfiguration robotConfig = createScriptRobotLaunchConfiguration(PROJECT_NAME);
        final String executablePath = projectProvider.getFile("executable_script.bat").getLocation().toPortableString();
        robotConfig.setScriptPath(executablePath);
        robotConfig.setScriptArguments("-arg1 abc -arg2 xyz");

        final ScriptRobotLaunchInMode launchMode = createModeUnderTest();
        final RunCommandLine commandLine = launchMode.prepareCommandLine(robotConfig, 12345);

        assertThat(commandLine.getCommandLine()).containsSubsequence(executablePath, "-arg1", "abc", "-arg2", "xyz");
    }

    private static ScriptRobotLaunchInMode createModeUnderTest() {
        return new ScriptRobotLaunchInMode() {

            @Override
            protected Process launchAndAttachToProcess(final ScriptRobotLaunchConfiguration robotConfig,
                    final ILaunch launch, final IProgressMonitor monitor) throws CoreException, IOException {
                return null;
            }
        };
    }

    private ScriptRobotLaunchConfiguration createScriptRobotLaunchConfiguration(final String projectName)
            throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = manager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID).newInstance(null, "robot");
        final ScriptRobotLaunchConfiguration robotConfig = new ScriptRobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }
}
