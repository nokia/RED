/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class LocalProcessCommandLineBuilderTest {

    private static final String PROJECT_NAME = LocalProcessCommandLineBuilderTest.class.getSimpleName();

    private static final IStringVariableManager VARIABLE_MANAGER = VariablesPlugin.getDefault()
            .getStringVariableManager();

    private static final IValueVariable[] CUSTOM_VARIABLES = new IValueVariable[] {
            VARIABLE_MANAGER.newValueVariable("a_var", "a_desc", true, "a_value"),
            VARIABLE_MANAGER.newValueVariable("b_var", "b_desc", true, "b_value"),
            VARIABLE_MANAGER.newValueVariable("c_var", "c_desc", true, "c_value") };

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);
    @Mock
    private RedPreferences preferences;

    @Mock
    private RobotRuntimeEnvironment environment;

    @BeforeClass
    public static void before() throws Exception {
        VARIABLE_MANAGER.addVariables(CUSTOM_VARIABLES);

        projectProvider.createDir(Path.fromPortableString("001__suites_a"));
        projectProvider.createFile(Path.fromPortableString("001__suites_a/s1.robot"), "*** Test Cases ***",
                "001__case1", "  Log  10", "001__case2", "  Log  20");
        projectProvider.createFile(Path.fromPortableString("001__suites_a/s2.robot"), "*** Test Cases ***",
                "001__case3", "  Log  10", "001__case4", "  Log  20");
        projectProvider.createFile("executable_script.bat");
        projectProvider.configure();
    }

    @AfterClass
    public static void after() throws Exception {
        VARIABLE_MANAGER.removeVariables(CUSTOM_VARIABLES);
    }

    @Test
    public void commandLineStartsWithInterpreterPath_whenActiveRuntimeEnvironmentIsUsed() throws Exception {
        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setInterpreterArguments("-a1 -a2");

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getCommandLine()).startsWith("/path/to/executable", "-a1", "-a2", "-m", "robot.run");
    }

    @Test
    public void commandLineStartsWithInterpreterName_whenProjectInterpreterIsNotUsed() throws Exception {
        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setInterpreterArguments("-a1 -a2");
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.PyPy);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getCommandLine()).startsWith(SuiteExecutor.PyPy.executableName(), "-a1", "-a2", "-m",
                "robot.run");
    }

    @Test
    public void commandLineStartsWithDefaultInterpreterName_whenThereIsNoActiveRuntimeEnvironment() throws Exception {
        final RobotProject robotProject = spy(createRobotProject());
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setInterpreterArguments("-a1 -a2");

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getCommandLine()).startsWith(SuiteExecutor.Python.executableName(), "-a1", "-a2", "-m",
                "robot.run");
    }

    @Test
    public void commandLineContainsSuitesToRun() throws Exception {
        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(
                ImmutableMap.of("001__suites_a/s1.robot", newArrayList(), "001__suites_a/s2.robot", newArrayList()));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSequence("-s", PROJECT_NAME + ".Suites_a.S1")
                .containsSequence("-s", PROJECT_NAME + ".Suites_a.S2")
                .endsWith(projectPath)
                .doesNotContain("-t");
    }

    @Test
    public void commandLineContainsTestsToRun() throws Exception {
        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList("001__case1", "001__case2"),
                "001__suites_a/s2.robot", newArrayList("001__case3")));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSequence("-s", PROJECT_NAME + ".Suites_a.S1")
                .containsSequence("-s", PROJECT_NAME + ".Suites_a.S2")
                .containsSequence("-t", PROJECT_NAME + ".Suites_a.S1.001__case1")
                .containsSequence("-t", PROJECT_NAME + ".Suites_a.S1.001__case2")
                .containsSequence("-t", PROJECT_NAME + ".Suites_a.S2.001__case3")
                .endsWith(projectPath);
    }

    @Test
    public void coreExceptionIsThrown_whenResourceDoesNotExist() throws Exception {
        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("not_existig_suite", newArrayList("not_existig_case")));

        final LocalProcessCommandLineBuilder builder = new LocalProcessCommandLineBuilder(robotConfig, robotProject);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> builder.createRunCommandLine(12345, preferences))
                .withMessage("Suite '%s' does not exist in project '%s'", "not_existig_suite", PROJECT_NAME)
                .withNoCause();
    }

    @Test
    public void commandLineTranslatesSuitesNames_whenNamesContainsDoubleUnderscores() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList()));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getArgumentFile().get().generateContent())
                .contains("--suite      " + PROJECT_NAME + ".Suites_a");
    }

    @Test
    public void commandLineDoesNotTranslateTestNames_whenNamesContainsDoubleUnderscores() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList("001__case1")));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String argFileContent = commandLine.getArgumentFile().get().generateContent();
        assertThat(argFileContent).contains("-suite", PROJECT_NAME + ".Suites_a");
        assertThat(argFileContent).contains("-test", PROJECT_NAME + ".Suites_a.001__case1");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_1() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create("folder1");
        final SearchPath searchPath2 = SearchPath.create("folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains(
                "-pythonpath " + projectPath + File.separator + "folder1:" + projectPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_2() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create(PROJECT_NAME + "/folder1");
        final SearchPath searchPath2 = SearchPath.create(PROJECT_NAME + "/folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains(
                "--pythonpath " + projectPath + File.separator + "folder1:" + projectPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsPythonPathsForPythonLibrariesAddedToRedXml() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "PyLib1", PROJECT_NAME + "/folder1"));
        config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "PyLib2", PROJECT_NAME + "/folder2"));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains(
                "--pythonpath " + projectPath + File.separator + "folder1:" + projectPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsClassPathsDefinedInRedXml_1() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create("JavaLib1.jar");
        final SearchPath searchPath2 = SearchPath.create("JavaLib2.jar");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addClassPath(searchPath1);
        config.addClassPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Jython);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSequence("-J-cp", "." + File.pathSeparator + projectPath
                + File.separator + "JavaLib1.jar" + File.pathSeparator + projectPath + File.separator + "JavaLib2.jar");
    }

    @Test
    public void commandLineContainsClassPathsDefinedInRedXml_2() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create(PROJECT_NAME + "/JavaLib1.jar");
        final SearchPath searchPath2 = SearchPath.create(PROJECT_NAME + "/JavaLib2.jar");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addClassPath(searchPath1);
        config.addClassPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Jython);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSequence("-J-cp", "." + File.pathSeparator + projectPath
                + File.separator + "JavaLib1.jar" + File.pathSeparator + projectPath + File.separator + "JavaLib2.jar");
    }

    @Test
    public void commandLineContainsClassPathsForJavaLibrariesAddedToRedXml() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib1", PROJECT_NAME + "/JavaLib1.jar"));
        config.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib2", PROJECT_NAME + "/JavaLib2.jar"));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Jython);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSequence("-J-cp", "." + File.pathSeparator + projectPath
                + File.separator + "JavaLib1.jar" + File.pathSeparator + projectPath + File.separator + "JavaLib2.jar");
    }

    @Test
    public void commandLineContainsPathsForVariableFiles() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedVariableFile(ReferencedVariableFile.create(PROJECT_NAME + "/vars1.py"));
        config.addReferencedVariableFile(ReferencedVariableFile.create(PROJECT_NAME + "/vars2.py", "a", "b", "c"));
        projectProvider.configure(config);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Jython);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).containsSequence(
                "--variablefile " + projectPath + File.separator + "vars1.py",
                "--variablefile " + projectPath + File.separator + "vars2.py:a:b:c");
    }

    @Test
    public void commandLineContainsTags() throws Exception {
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(newArrayList("EX_1", "EX_2"));
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIncludedTags(newArrayList("IN_1", "IN_2"));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getArgumentFile().get().generateContent()).containsSequence("--include    IN_1",
                "--include    IN_2", "--exclude    EX_1", "--exclude    EX_2");
    }

    @Test
    public void commandLineStartsWitExecutableFilePath() throws Exception {
        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final String executablePath = projectProvider.getFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getCommandLine()).startsWith(executablePath);
    }

    @Test
    public void commandLineStartsWithExecutableFilePath_whenPathContainsVariables() throws Exception {
        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath("${workspace_loc:/" + PROJECT_NAME + "/executable_script.bat}");

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getCommandLine())
                .startsWith(projectProvider.getFile("executable_script.bat").getLocation().toOSString());
    }

    @Test
    public void commandLineContainsExecutableFilePathWithArguments() throws Exception {
        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final String executablePath = projectProvider.getFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);
        robotConfig.setExecutableFileArguments("-arg1 abc -arg2 xyz");

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        assertThat(commandLine.getCommandLine()).startsWith(executablePath, "-arg1", "abc", "-arg2", "xyz");
    }

    @Test
    public void coreExceptionIsThrown_whenExecutableFileDoesNotExist() throws Exception {
        final String executablePath = projectProvider.getFile("not_existing.bat").getLocation().toOSString();

        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath(executablePath);

        final LocalProcessCommandLineBuilder builder = new LocalProcessCommandLineBuilder(robotConfig, robotProject);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> builder.createRunCommandLine(12345, preferences))
                .withMessage("Executable file '%s' does not exist", executablePath)
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenExecutableFileDefinedWithVariableDoesNotExist() throws Exception {
        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath("${workspace_loc:/" + PROJECT_NAME + "/not_existing.bat}");

        final LocalProcessCommandLineBuilder builder = new LocalProcessCommandLineBuilder(robotConfig, robotProject);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> builder.createRunCommandLine(12345, preferences))
                .withMessage("Variable references non-existent resource : ${workspace_loc:/" + PROJECT_NAME
                        + "/not_existing.bat}")
                .withNoCause();
    }

    @Test
    public void pathToSuiteIsUsed_whenSingleSuiteIsRunAndPreferenceIsSet() throws Exception {
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList()));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String suitePath = projectProvider.getFile("001__suites_a/s1.robot").getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith(suitePath).doesNotContain("-s", "-t");
    }

    @Test
    public void pathToSuiteIsUsed_whenTestsFromSingleSuiteAreRunAndPreferenceIsSet() throws Exception {
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList("001__case1")));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String suitePath = projectProvider.getFile("001__suites_a/s1.robot").getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith("-t", "001__case1", suitePath).doesNotContain("-s");
    }

    @Test
    public void pathToSuiteIsNotUsed_whenSeveralResourcesAreRunAndPreferenceIsSet() throws Exception {
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(
                ImmutableMap.of("001__suites_a", newArrayList(), "001__suites_a/s1.robot", newArrayList()));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine())
                .endsWith("-s", PROJECT_NAME + ".Suites_a", "-s", PROJECT_NAME + ".Suites_a.S1", projectPath)
                .doesNotContain("-t");
    }

    @Test
    public void pathToSuiteIsNotUsed_whenSingleFolderIsRunAndPreferenceIsSet() throws Exception {
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList()));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith("-s", PROJECT_NAME + ".Suites_a", projectPath)
                .doesNotContain("-t");
    }

    @Test
    public void pathToSuiteIsNotUsed_whenSingleSuiteIsRunAndPreferenceIsNotSet() throws Exception {
        final RobotProject robotProject = createRobotProject(SuiteExecutor.Python);
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a/s1.robot", newArrayList()));

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).endsWith("-s", PROJECT_NAME + ".Suites_a.S1", projectPath)
                .doesNotContain("-t");
    }

    @Test
    public void knownVariablesAreResolvedInAdditionalArguments() throws Exception {
        final RobotProject robotProject = createRobotProject();
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setRobotArguments("a ${a_var} ${a}");
        robotConfig.setInterpreterArguments("${b} b ${b_var}");
        final String executablePath = projectProvider.getFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);
        robotConfig.setExecutableFileArguments("${c_var} ${c} c");

        final RunCommandLine commandLine = new LocalProcessCommandLineBuilder(robotConfig, robotProject)
                .createRunCommandLine(12345, preferences);

        final String projectPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).startsWith(executablePath, "c_value", "${c}", "c")
                .containsSequence("${b}", "b", "b_value", "-m", "robot.run")
                .endsWith("a", "a_value", "${a}", projectPath);
    }

    private RobotProject createRobotProject() {
        return new RobotModel().createRobotProject(projectProvider.getProject());
    }

    private RobotProject createRobotProject(final SuiteExecutor interpreter) {
        final RobotProject robotProject = spy(createRobotProject());
        when(environment.getInterpreter()).thenReturn(interpreter);
        when(environment.getPythonExecutablePath()).thenReturn("/path/to/executable");
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);
        return robotProject;
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

}
