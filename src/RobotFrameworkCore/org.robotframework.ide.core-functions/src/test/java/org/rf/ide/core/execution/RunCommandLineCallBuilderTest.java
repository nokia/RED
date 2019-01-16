/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;
import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.execution.RunCommandLineCallBuilder.RunCommandLine;

public class RunCommandLineCallBuilderTest {

    @Test
    public void testSimpleCall_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                        RedTemporaryDirectory.getTemporaryFile("TestRunnerAgent.py") + ":12345", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testSimpleCall_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(5)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener",
                        RedTemporaryDirectory.getTemporaryFile("TestRunnerAgent.py") + ":12345");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithDataSourcePaths_argsFile() throws IOException {
        final File f1 = new File("a");
        final File f2 = new File("b");
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .withDataSources(newArrayList(f1, f2))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile",
                        f1.getAbsolutePath(), f2.getAbsolutePath());
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallWithDataSourcePaths_argsInline() throws IOException {
        final File f1 = new File("a");
        final File f2 = new File("b");
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .withDataSources(newArrayList(f1, f2))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", f1.getAbsolutePath(),
                        f2.getAbsolutePath());
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutable_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .withExecutableFile(new File("exec"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(8)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutable_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .withExecutableFile(new File("exec"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(6)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutableAndArguments_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .withExecutableFile(new File("exec"))
                .addUserArgumentsForExecutableFile(newArrayList("args"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("args", "/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutableAndArguments_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .withExecutableFile(new File("exec"))
                .addUserArgumentsForExecutableFile(newArrayList("args"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("args", "/x/y/z/python", "-m", "robot.run", "--listener");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallNotWrappedWithExecutableButWithArgumentsHaveNothing_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .addUserArgumentsForExecutableFile(newArrayList("args"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallNotWrappedWithExecutableButWithArgumentsHaveNothing_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .addUserArgumentsForExecutableFile(newArrayList("args"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(5)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutableAndArguments_withSequenceRobotCommandArg() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .withExecutableFile(new File("exec"))
                .addUserArgumentsForExecutableFile(newArrayList("args"))
                .useSingleRobotCommandLineArg(false)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("args", "/x/y/z/python", "-m", "robot.run", "--listener");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutableAndArguments_withSingleRobotCommandArg() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .withExecutableFile(new File("exec"))
                .addUserArgumentsForExecutableFile(newArrayList("args"))
                .useSingleRobotCommandLineArg(true)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(3).containsSubsequence("args");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getCommandLine()[2]).startsWith("/x/y/z/python -m robot.run --listener");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWrappedWithOtherExecutableAndSeveralArguments_withSequenceRobotCommandArg() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .withExecutableFile(new File("exec"))
                .addUserArgumentsForExecutableFile(newArrayList("a1", "-a2", "a3 a4", "-a5", "a6", "a7"))
                .useSingleRobotCommandLineArg(false)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(12)
                .containsSubsequence("a1", "-a2", "a3 a4", "-a5", "a6", "a7", "/x/y/z/python", "-m", "robot.run",
                        "--listener");
        assertThat(cmdLine.getCommandLine()[0]).endsWith("exec");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithInterpreterArgumentsAdded_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .addUserArgumentsForInterpreter(newArrayList("args"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(8)
                .containsSubsequence("/x/y/z/python", "args", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallWithInterpreterArgumentsAdded_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .addUserArgumentsForInterpreter(newArrayList("args"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(6)
                .containsSubsequence("/x/y/z/python", "args", "-m", "robot.run", "--listener");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithSeveralInterpreterArgumentsAdded_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .addUserArgumentsForInterpreter(newArrayList("a1", "-a2", "a3 a4", "-a5", "a6", "a7"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(11)
                .containsSubsequence("/x/y/z/python", "a1", "-a2", "a3 a4", "-a5", "a6", "a7", "-m", "robot.run",
                        "--listener");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithClassPathForJython_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Jython, "/x/y/z/jython", 12345)
                .useArgumentFile(true)
                .addLocationsToClassPath(newArrayList("cp/path", "other"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/jython", "-J-cp", "cp/path" + File.pathSeparator + "other", "-m",
                        "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallWithClassPathForJython_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Jython, "/x/y/z/jython", 12345)
                .useArgumentFile(false)
                .addLocationsToClassPath(newArrayList("cp/path", "other"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/jython", "-J-cp", "cp/path" + File.pathSeparator + "other", "-m",
                        "robot.run", "--listener");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithAdditionalPythonPathForJython_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder
                .create(SuiteExecutor.Jython, "/path/to/bin/jython", 12345)
                .useArgumentFile(true)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(8)
                .containsSubsequence("/path/to/bin/jython",
                        "-J-Dpython.path=" + Paths.get("/path/to", "Lib", "site-packages"), "-m", "robot.run",
                        "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).isPresent();
    }

    @Test
    public void testCallWithAdditionalPythonPathForJython_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder
                .create(SuiteExecutor.Jython, "/path/to/bin/jython", 12345)
                .useArgumentFile(false)
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(6)
                .containsSubsequence("/path/to/bin/jython",
                        "-J-Dpython.path=" + Paths.get("/path/to", "Lib", "site-packages"), "-m", "robot.run",
                        "--listener");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithSuitesInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .suitesToRun(newArrayList("s1", "s2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).hasValueSatisfying(
                (argumentFile -> assertThat(argumentFile.generateContent()).contains("--suite s1", "--suite s2")));
    }

    @Test
    public void testCallWithSuitesInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .suitesToRun(newArrayList("s1", "s2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "-s", "s1", "-s", "s2");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithTestsInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .testsToRun(newArrayList("t1", "t2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).hasValueSatisfying(
                (argumentFile -> assertThat(argumentFile.generateContent()).contains("--test t1", "--test t2")));
    }

    @Test
    public void testCallWithTestsInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .testsToRun(newArrayList("t1", "t2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "-t", "t1", "-t", "t2");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithIncludedTagsInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .includeTags(newArrayList("tag1", "tag2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile())
                .hasValueSatisfying((argumentFile -> assertThat(argumentFile.generateContent())
                        .contains("--include tag1", "--include tag2")));
    }

    @Test
    public void testCallWithIncludedTagsInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .includeTags(newArrayList("tag1", "tag2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "-i", "tag1", "-i", "tag2");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithExcludedTagsInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .excludeTags(newArrayList("tag1", "tag2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile())
                .hasValueSatisfying((argumentFile -> assertThat(argumentFile.generateContent())
                        .contains("--exclude tag1", "--exclude tag2")));
    }

    @Test
    public void testCallWithExcludedTagsInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .excludeTags(newArrayList("tag1", "tag2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "-e", "tag1", "-e", "tag2");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithVarFilesInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .addVariableFiles(newArrayList("var1.py", "var2.py"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile())
                .hasValueSatisfying((argumentFile -> assertThat(argumentFile.generateContent())
                        .contains("--variablefile var1.py", "--variablefile var2.py")));
    }

    @Test
    public void testCallWithVarFilesInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .addVariableFiles(newArrayList("var1.py", "var2.py"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(9)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "-V", "var1.py", "-V",
                        "var2.py");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithPythonpathInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .addLocationsToPythonPath(newArrayList("path1", "path2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile()).hasValueSatisfying(
                (argumentFile -> assertThat(argumentFile.generateContent()).contains("--pythonpath path1:path2")));
    }

    @Test
    public void testCallWithPythonpathInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .addLocationsToPythonPath(newArrayList("path1", "path2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "-P", "path1:path2");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

    @Test
    public void testCallWithUserArgumentsInArgumentFile_argsFile() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(true)
                .addUserArgumentsForRobot(newArrayList("--arg", "val1", "-X", "val 2", "--other", "--other2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(7)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--argumentfile");
        assertThat(cmdLine.getArgumentFile())
                .hasValueSatisfying((argumentFile -> assertThat(argumentFile.generateContent())
                        .contains("--arg    val1", "-X       val 2", "--other", "--other2")));
    }

    @Test
    public void testCallWithUserArgumentsInArgumentFile_argsInline() throws IOException {
        final RunCommandLine cmdLine = RunCommandLineCallBuilder.create(SuiteExecutor.Python, "/x/y/z/python", 12345)
                .useArgumentFile(false)
                .addUserArgumentsForRobot(newArrayList("--arg", "val1", "-X", "val 2", "--other", "--other2"))
                .build();

        assertThat(cmdLine.getCommandLine()).hasSize(11)
                .containsSubsequence("/x/y/z/python", "-m", "robot.run", "--listener", "--arg", "val1", "-X", "val 2",
                        "--other", "--other2");
        assertThat(cmdLine.getArgumentFile()).isNotPresent();
    }

}
