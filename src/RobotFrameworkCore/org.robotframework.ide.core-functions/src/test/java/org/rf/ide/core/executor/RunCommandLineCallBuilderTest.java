/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;

public class RunCommandLineCallBuilderTest {

    @Test
    public void testSimpleCall_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345).build();
        assertThat(cmdLine.getCommandLine()).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run",
                "--listener", "--argumentfile");
    }

    @Test
    public void testCallWithProject_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .withProject(new File("project"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(8).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(commandLine[commandLine.length - 1]).endsWith("project");
    }

    @Test
    public void testCallWithAdditionalDataPaths_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .withAdditionalProjectsLocations(newArrayList("a", "b"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(9).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile", "a", "b");
    }

    @Test
    public void testCallWrappedWithOtherExecutable_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .withExecutableFile("exec")
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(8).containsSubsequence("exec", "/x/y/z/python", "-m", "robot.run",
                "--listener",
                "--argumentfile");
    }

    @Test
    public void testCallWrappedWithOtherExecutableAndArguments_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .withExecutableFile("exec")
                .addUserArgumentsForExecutableFile("args")
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(9).containsSubsequence("exec", "args", "/x/y/z/python", "-m", "robot.run",
                "--listener", "--argumentfile");
    }

    @Test
    public void testCallNotWrappedWithExecutableButWithArgumentsHaveNothing_withRuntimeEnvironement()
            throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .addUserArgumentsForExecutableFile("args")
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run",
                "--listener", "--argumentfile");
    }

    @Test
    public void testCallWithInterpreterArgumentsAdded_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .addUserArgumentsForInterpreter("args")
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(8).containsSubsequence("/x/y/z/python", "args", "-m", "robot.run", "--listener",
                "--argumentfile");
    }

    @Test
    public void testCallWithClassPathForJython_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Jython, "/x/y/z/jython");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .addLocationsToClassPath(newArrayList("cp/path"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(9).containsSubsequence("/x/y/z/jython", "-J-cp", "cp/path", "-m", "robot.run",
                "--listener", "--argumentfile");
    }

    @Test
    public void testCallWithSuitesInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .suitesToRun(newArrayList("s1", "s2"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run",
                "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--suite s1");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--suite s2");
    }

    @Test
    public void testCallWithTestsInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .testsToRun(newArrayList("t1", "t2"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--test t1");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--test t2");
    }

    @Test
    public void testCallWithIncludedTagsInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .includeTags(newArrayList("tag1", "tag2"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--include tag1");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--include tag2");
    }

    @Test
    public void testCallWithExcludedTagsInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .excludeTags(newArrayList("tag1", "tag2"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--exclude tag1");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--exclude tag2");
    }

    @Test
    public void testCallWithVarFilesInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .addVariableFiles(newArrayList("var1.py", "var2.py"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--variablefile var1.py");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--variablefile var2.py");
    }

    @Test
    public void testCallWithPythonpathInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .addLocationsToPythonPath(newArrayList("path1", "path2"))
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--pythonpath path1:path2");
    }

    @Test
    public void testCallWithUserArgumentsInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .addUserArgumentsForRobot("--arg val1 -X val2 --other --other2")
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--arg    val1");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("-X       val2");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--other");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--other2");
    }

    @Test
    public void testCallForDryrunInArgumentFile_withRuntimeEnvironement() throws IOException {
        final RobotRuntimeEnvironment env = prepareEnvironment(SuiteExecutor.Python, "/x/y/z/python");

        final RunCommandLine cmdLine = RunCommandLineCallBuilder.forEnvironment(env, 12345)
                .enableDryRun()
                .build();
        final String[] commandLine = cmdLine.getCommandLine();
        assertThat(commandLine).hasSize(7).containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                "--argumentfile");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--prerunmodifier");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--runemptysuite");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--dryrun");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--output         NONE");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--report         NONE");
        assertThat(cmdLine.getArgumentFile().generateContent()).contains("--log            NONE");
    }

    private static RobotRuntimeEnvironment prepareEnvironment(final SuiteExecutor executor,
            final String interpreterPath) {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(executor);
        when(env.getPythonExecutablePath()).thenReturn(interpreterPath);
        return env;
    }

}
