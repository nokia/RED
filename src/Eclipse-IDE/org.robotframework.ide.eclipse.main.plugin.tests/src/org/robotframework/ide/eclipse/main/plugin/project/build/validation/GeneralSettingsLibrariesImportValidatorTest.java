/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.libraries.LibraryConstructor;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

public class GeneralSettingsLibrariesImportValidatorTest {

    public static ProjectProvider projectProvider = new ProjectProvider(
            GeneralSettingsLibrariesImportValidatorTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    private RobotModel model;

    private MockReporter reporter;

    private RobotProject robotProject;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final File root = tempFolder.getRoot();

        getFile(root, "external_lib.py").createNewFile();
        getFile(root, "external_dir").mkdir();
        getFile(root, "external_dir", "external_nested_lib.py").createNewFile();

        projectProvider.configure();
    }

    @Before
    public void beforeTest() {
        model = new RobotModel();
        reporter = new MockReporter();
        robotProject = model.createRobotProject(projectProvider.getProject());
    }

    @Test
    public void markerIsReported_whenImportIsNotSpecified() {
        validateLibraryImport("");

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.MISSING_LIBRARY_NAME, new ProblemPosition(2, Range.closed(17, 24))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithoutArgumentsAndDefaultLocationIsNotInConfig() {
        validateLibraryImport("Remote");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(26, 32))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithoutArgumentsAndDefaultLocationIsInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:8270/RPC2");

        validateLibraryImport("Remote");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(26, 32))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithTimeoutAndDefaultLocationIsInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:8270/RPC2");

        validateLibraryImport("Remote  timeout=60");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(26, 32))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithToManyNamedArguments() {
        validateLibraryImport("Remote  uri=http://127.0.0.1:9000/  timeout=60  timeout=30");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.OVERRIDDEN_NAMED_ARGUMENT, new ProblemPosition(2, Range.closed(62, 72))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithToManyPositionalArguments() {
        validateLibraryImport("Remote  http://127.0.0.1:9000/  60  30");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS, new ProblemPosition(2, Range.closed(26, 32))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithPositionalUriNotInConfig() {
        validateLibraryImport("Remote  http://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(34, 56))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithPositionalUriWithoutProtocolNotInConfig() {
        validateLibraryImport("Remote  127.0.0.1:9000");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(34, 48))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNamedUriNotInConfig() {
        validateLibraryImport("Remote  uri=http://127.0.0.1:9000");

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(34, 59))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNamedUriWithoutProtocolNotInConfig() {
        validateLibraryImport("Remote  uri=127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(34, 53))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithPositionalTimeoutAndUriNotInConfig() {
        validateLibraryImport("Remote    http://127.0.0.1:9000/    30");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(36, 58))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNamedTimeoutAndUriNotInConfig() {
        validateLibraryImport("Remote    uri=http://127.0.0.1:9000    timeout=30");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(36, 61))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvertedNamedTimeoutAndUriNotInConfig() {
        validateLibraryImport("Remote    timeout=30    uri=http://127.0.0.1:9000");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML,
                        new ProblemPosition(2, Range.closed(50, 75))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithPositionalUriInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000/");

        validateLibraryImport("Remote  http://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 56))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithPositionalUriWithoutProtocolInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000/");

        validateLibraryImport("Remote  127.0.0.1:9000");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 48))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithNamedUriInConfigWithoutLibSpec() {
        addRemoteUriToConfig("https://127.0.0.1:9000/");

        validateLibraryImport("Remote  uri=https://127.0.0.1:9000");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 60))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithNamedUriWithoutProtocolInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000");

        validateLibraryImport("Remote  uri=127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 53))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithPositionalTimeoutAndUriInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000/");

        validateLibraryImport("Remote   http://127.0.0.1:9000/  30");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(35, 57))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithPositionalTimeoutAndUriWithoutProtocolInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000/");

        validateLibraryImport("Remote  127.0.0.1:9000  30");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 48))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithNamedTimeoutAndUriInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000/");

        validateLibraryImport("Remote  uri=http://127.0.0.1:9000  timeout=30");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 59))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryWithInvertedNamedTimeoutAndUriInConfigWithoutLibSpec() {
        addRemoteUriToConfig("http://127.0.0.1:9000");

        validateLibraryImport("Remote  timeout=30  uri=127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(46, 65))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUri1() {
        validateLibraryImport("Remote  urrri=http://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 62))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUri2() {
        validateLibraryImport("Remote  1://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 53))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUri3() {
        validateLibraryImport("Remote  %urihttp://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 60))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUri4() {
        validateLibraryImport("Remote  http://127.0.0.1:9000/%");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 57))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUriWithoutScheme() {
        validateLibraryImport("Remote  ://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 52))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUriWithoutAddress() {
        validateLibraryImport("Remote  http://");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 41))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidPositionalUriAndTimeout() {
        validateLibraryImport("Remote  urrri[http://127.0.0.1:9000/  60");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 62))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUri1() {
        validateLibraryImport("Remote  uri=urrri]http://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 66))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUri2() {
        validateLibraryImport("Remote  uri=:://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 57))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUri3() {
        validateLibraryImport("Remote  uri=}urihttp://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 64))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUri4() {
        validateLibraryImport("Remote  uri=uri=http://127.0.0.1:9000/%");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 65))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUriWithoutScheme() {
        validateLibraryImport("Remote  uri=://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 56))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUriWithoutAddress() {
        validateLibraryImport("Remote  uri=http://");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 45))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvalidNamedUriAndTimeout() {
        validateLibraryImport("Remote  uri=urrri!http://127.0.0.1:9000/  timeout=60");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 66))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithInvertedInvalidNamedUriAndTimeout() {
        validateLibraryImport("Remote  timeout=60  uri=urrri|http://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(46, 78))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNotSupportedPositionalUriProtocol() {
        validateLibraryImport("Remote  ftp://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 55))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNotSupportedNamedUriProtocol() {
        validateLibraryImport("Remote  uri=ftp://127.0.0.1:9000/");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 59))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNotSupportedPositionalUriProtocolAndTimeout() {
        validateLibraryImport("Remote  ftp://127.0.0.1:9000/  60");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 55))));
    }

    @Test
    public void markerIsReported_whenRemoteLibraryIsImportedWithNotSupportedNamedUriProtocolAndTimeout() {
        validateLibraryImport("Remote  uri=ftp://127.0.0.1:9000/  timeout=60");

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(GeneralSettingsProblem.NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(34, 59))));
    }

    @Test
    public void markerIsReported_whenImportingUnknownLibraryByName() {
        validateLibraryImport("ExampleLibrary");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT, new ProblemPosition(2, Range.closed(26, 40))));
    }

    @Test
    public void markerIsReported_whenImportContainsUnknownVariables() {
        validateLibraryImport("${unknown}/file.robot");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_PARAMETERIZED, new ProblemPosition(2, Range.closed(26, 47))));
    }

    @Test
    public void markerIsReported_whenUsingUnescapedWindowsPaths_1() {
        validateLibraryImport("C:\\test\\ExampleLibrary.py");

        assertThat(reporter.getReportedProblems())
                .containsOnly(new Problem(GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS,
                        new ProblemPosition(2, Range.closed(26, 51))));
    }

    @Test
    public void markerIsReported_whenUsingUnescapedWindowsPaths_2() {
        validateLibraryImport("..\\..\\ExampleLibrary.py");

        assertThat(reporter.getReportedProblems())
                .containsOnly(new Problem(GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS,
                        new ProblemPosition(2, Range.closed(26, 49))));
    }

    @Test
    public void markerIsReported_whenUsingUnescapedWindowsPaths_3() {
        validateLibraryImport("../..\\ExampleLibrary.py");

        assertThat(reporter.getReportedProblems())
                .containsOnly(new Problem(GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS,
                        new ProblemPosition(2, Range.closed(26, 49))));
    }

    @Test
    public void markerIsReported_whenUsingAbsolutePathImport() throws Exception {
        final File tmpFile = tempFolder.newFile("library.py");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateLibraryImport(absPath);

        assertThat(reporter.getReportedProblems()).contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE,
                new ProblemPosition(2, Range.closed(26, 26 + absPath.length()))));
    }

    @Test
    public void markerIsReported_whenFileIsImportedRelativelyViaSysPathInsteadOfLocally() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested_lib.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(tmpFile.getParentFile()));

        validateLibraryImport("external_nested_lib.py");
        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        new ProblemPosition(2, Range.closed(26, 48))));
    }

    @Test
    public void markerIsReported_whenFileIsImportedRelativelyViaRedXmlPythonPathInsteadOfLocally() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested_lib.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(tmpFile.getParent()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        validateLibraryImport("external_nested_lib.py");
        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        new ProblemPosition(2, Range.closed(26, 48))));

        robotProject.getRobotProjectConfig().setPythonPath(null);
    }

    @Test
    public void markerIsReported_whenFileIsImportedFromOutsideOfWorkspace_1() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested_lib.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(tmpFile.getParentFile()));

        validateLibraryImport("external_nested_lib.py");

        assertThat(reporter.getReportedProblems()).contains(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE, new ProblemPosition(2, Range.closed(26, 48))));
    }

    @Test
    public void markerIsReported_whenFileIsImportedFromOutsideOfWorkspace_2() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested_lib.py");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateLibraryImport(absPath);

        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE,
                        new ProblemPosition(2, Range.closed(26, 26 + absPath.length()))));
    }

    @Test
    public void markerIsReported_whenExternalFileDoesNotExist() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "non_existing.py");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateLibraryImport(absPath);

        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT,
                        new ProblemPosition(2, Range.closed(26, 26 + absPath.length()))));
    }

    @Test
    public void markerIsReported_whenFileInWorkspaceDoesNotExist() {
        validateLibraryImport("non_existing.py");

        assertThat(reporter.getReportedProblems()).contains(new Problem(
                GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT, new ProblemPosition(2, Range.closed(26, 41))));
    }

    @Test
    public void markerIsReported_whenImportedResourceLiesInDifferentDirectory() throws Exception {
        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/lib.py");

        validateLibraryImport("lib.py");
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT, new ProblemPosition(2, Range.closed(26, 32))));

        dir.delete(true, null);
    }

    @Test
    public void markerIsReported_whenThereIsProblemWithLibraryArgumentsWhenImportedByName() throws Exception {
        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile(libPath);

        final LibraryConstructor constructor = new LibraryConstructor();
        constructor.setArguments(newArrayList("x", "y", "*ls"));

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor, constructor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport(libName, new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).contains(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(2, Range.closed(26, 29))));

        libFile.delete(true, null);
    }

    @Test
    public void markerIsReported_whenThereIsProblemWithLibraryArgumentsWhenImportedByPath() throws Exception {
        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");

        final LibraryConstructor constructor = new LibraryConstructor();
        constructor.setArguments(newArrayList("x", "y", "*ls"));

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor, constructor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).contains(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(2, Range.closed(26, 32))));

        libFile.delete(true, null);
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithoutArgumentsAndDefaultLocationIsInConfigWithLibSpec()
            throws Exception {
        final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary("http://127.0.0.1:8270/RPC2");

        validateLibraryImport("Remote", libs, new HashMap<>());

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithTimeoutAndDefaultLocationIsInConfigWithLibSpec()
            throws Exception {
        final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary("http://127.0.0.1:8270/RPC2");

        validateLibraryImport("Remote   timeout=30", libs, new HashMap<>());

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithPositionalUriInConfigWithLibSpec() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  http://127.0.0.1:" + socket.getLocalPort() + "/", new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithPositionalUriWithoutProtocolInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  127.0.0.1:" + socket.getLocalPort(), new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithNamedUriInConfigWithLibSpec() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  uri=http://127.0.0.1:" + socket.getLocalPort(), new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithNamedUriWithoutProtocolInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  uri=127.0.0.1:" + socket.getLocalPort(), new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithUriWithHttpProtocolInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  https://127.0.0.1:" + socket.getLocalPort(), new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithUriWithHttpsProtocolInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "https://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  http://127.0.0.1:" + socket.getLocalPort(), new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithPositionalTimeoutAndUriInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  http://127.0.0.1:" + socket.getLocalPort() + "/  30", new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithPositionalTimeoutAndUriWithoutProtocolInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote  127.0.0.1:" + socket.getLocalPort() + "  30", new HashMap<>(), libs);
            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithNamedTimeoutAndUriInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";
            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote   uri=http://127.0.0.1:" + socket.getLocalPort() + "  timeout=30",
                    new HashMap<>(), libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMarkerIsReported_whenRemoteLibraryIsImportedWithInvertedNamedTimeoutAndUriInConfigWithLibSpec()
            throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            final String location = "http://127.0.0.1:" + socket.getLocalPort() + "/";

            final Map<LibraryDescriptor, LibrarySpecification> libs = createLibSpecForLibrary(location);

            validateLibraryImport("Remote   timeout=30  uri=127.0.0.1:" + socket.getLocalPort() + "/", new HashMap<>(),
                    libs);

            assertThat(reporter.getReportedProblems()).isEmpty();
        }
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByName_1() throws Exception {
        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport(libName, new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByName_2() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir = projectProvider.createDir("directory");
        projectProvider.createFile("directory/lib.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport(libName, new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByName_3() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir1 = projectProvider.createDir("directory");
        final IFolder dir2 = projectProvider.createDir("directory/lib");
        projectProvider.createFile("directory/lib/__init__.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport(libName, new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir1.delete(true, null);
        dir2.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByPath_1() throws Exception {
        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByPath_2() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir = projectProvider.createDir("directory");
        projectProvider.createFile("directory/lib.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("directory/lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByPath_3() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir1 = projectProvider.createDir("directory");
        final IFolder dir2 = projectProvider.createDir("directory/lib");
        projectProvider.createFile("directory/lib/__init__.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("directory/lib/", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir1.delete(true, null);
        dir2.delete(true, null);
    }

    @Test
    public void markerIsReported_whenLocallyExistingLibraryIsImportedByRelativePathWithoutTrailingSeparator()
            throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir1 = projectProvider.createDir("directory");
        final IFolder dir2 = projectProvider.createDir("directory/lib");
        projectProvider.createFile("directory/lib/__init__.py");

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("directory/lib", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).contains(new Problem(
                GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT, new ProblemPosition(2, Range.closed(26, 39))));

        dir1.delete(true, null);
        dir2.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileExistLocallyButIsImportedUsingAbsolutePath()
            throws Exception {

        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");
        final String absPath = libFile.getLocation().toString();

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport(absPath, new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE));

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileExistLocallyAsALinkToExternalFile() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested_lib.py");

        final String libPath = tmpFile.getParent().replaceAll("\\\\", "/");
        final String libName = "external_nested_lib";

        resourceCreator.createLink(tmpFile.toURI(), projectProvider.getFile("link.py"));

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport(tmpFile.getAbsolutePath().replaceAll("\\\\", "/"), new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE));
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaSysPathFromWorkspace() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/dir";

        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/lib.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir.getLocation().toFile()));

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, "lib", libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaSysPathFromExternalLocation() {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir));

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib",
                dir.getAbsolutePath());
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("external_nested_lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE));
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaSysPathFromExternalLocationWhichIsLinkedInWorkspace()
            throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/linking_dir";
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        resourceCreator.createLink(dir.toURI(), projectProvider.getProject().getFolder("linking_dir"));

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir));

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib", libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("external_nested_lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaRedXmlPythonPathFromWorkspace()
            throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/dir";
        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/lib.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getLocation().toString()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, "lib", libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaRedXmlPythonPathFromExternalLocation() {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getAbsolutePath()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib",
                dir.getAbsolutePath());
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("external_nested_lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE));
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaRedXmlPythonPathFromExternalLocationWhichIsLinkedInWorkspace()
            throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/linking_dir";
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        resourceCreator.createLink(dir.toURI(), projectProvider.getProject().getFolder("linking_dir"));

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getAbsolutePath()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        final ReferencedLibrary refLib = ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib", libPath);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(refLib);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = ImmutableMap.of(descriptor, spec);

        validateLibraryImport("external_nested_lib.py", new HashMap<>(), refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));
    }

    private Condition<Problem> onlyCausedBy(final GeneralSettingsProblem... causes) {
        final Set<GeneralSettingsProblem> causesSet = newHashSet(causes);
        return new Condition<MockReporter.Problem>() {

            @Override
            public boolean matches(final Problem problem) {
                return causesSet.contains(problem.getCause());
            }
        };
    }

    private LibrarySpecification createNewLibrarySpecification(final LibraryDescriptor descriptor) {
        return createNewLibrarySpecification(descriptor, null);
    }

    private LibrarySpecification createNewLibrarySpecification(final LibraryDescriptor descriptor,
            final LibraryConstructor constructor) {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setName(descriptor.getName());
        spec.setConstructor(constructor);
        spec.setDescriptor(descriptor);
        return spec;
    }

    private void validateLibraryImport(final String toImport) {
        validateLibraryImport(toImport, new HashMap<>(), new HashMap<>());
    }

    private void validateLibraryImport(final String toImport,
            final Map<LibraryDescriptor, LibrarySpecification> stdLibs,
            final Map<LibraryDescriptor, LibrarySpecification> refLibs) {

        final RobotSuiteFile suiteFile = createLibraryImportingSuite(toImport);
        final LibraryImport libImport = getImport(suiteFile);

        final FileValidationContext context = prepareContext(robotProject.getRobotProjectConfig(), suiteFile, stdLibs,
                refLibs);

        final GeneralSettingsLibrariesImportValidator validator = new GeneralSettingsLibrariesImportValidator(context,
                suiteFile, newArrayList(libImport), reporter);
        try {
            validator.validate(null);
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to validate", e);
        }
    }

    private LibraryImport getImport(final RobotSuiteFile suiteFile) {
        return (LibraryImport) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
    }

    private RobotSuiteFile createLibraryImportingSuite(final String toImport) {
        try {
            final IFile file = projectProvider.createFile("suite.robot",
                    "*** Settings ***",
                    "Library  " + toImport);
            final RobotSuiteFile suite = model.createSuiteFile(file);
            suite.dispose();
            return suite;
        } catch (IOException | CoreException e) {
            throw new IllegalStateException("Cannot create file", e);
        }
    }

    private FileValidationContext prepareContext(final RobotProjectConfig config, final RobotSuiteFile suiteFile,
            final Map<LibraryDescriptor, LibrarySpecification> stdLibs,
            final Map<LibraryDescriptor, LibrarySpecification> refLibs) {

        final Multimap<String, LibrarySpecification> stdSpecsByName = Multimaps.index(stdLibs.values(),
                LibrarySpecification::getName);
        final Multimap<String, LibrarySpecification> refSpecsByName = Multimaps.index(refLibs.values(),
                LibrarySpecification::getName);

        final Multimap<String, LibrarySpecification> allLibsByNames = ArrayListMultimap.create();
        allLibsByNames.putAll(stdSpecsByName);
        allLibsByNames.putAll(refSpecsByName);

        final ValidationContext parentContext = new ValidationContext(config, model, RobotVersion.from("0.0"),
                SuiteExecutor.Python, allLibsByNames, refLibs);
        return new FileValidationContext(parentContext, suiteFile.getFile());
    }

    private static File getFile(final File root, final String... path) {
        if (path == null || path.length == 0) {
            return root;
        } else {
            return getFile(new File(root, path[0]), Arrays.copyOfRange(path, 1, path.length));
        }
    }

    private void addRemoteUriToConfig(final String address) {
        final RobotProjectConfig robotProjectConfig = robotProject.getRobotProjectConfig();
        final RemoteLocation remoteLibrary = RemoteLocation.create(address);
        robotProjectConfig.addRemoteLocation(remoteLibrary);

    }

    private Map<LibraryDescriptor, LibrarySpecification> createLibSpecForLibrary(final String address) {
        final RemoteLocation remoteLibrary = RemoteLocation.create(address);
        final LibraryConstructor constructor = new LibraryConstructor();
        constructor.setArguments(newArrayList("uri=http://127.0.0.1:8270", "timeout=None"));
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardRemoteLibrary(remoteLibrary);
        final LibrarySpecification spec = createNewLibrarySpecification(descriptor, constructor);
        final Map<LibraryDescriptor, LibrarySpecification> libs = ImmutableMap.of(descriptor, spec);

        final RobotProjectConfig robotProjectConfig = robotProject.getRobotProjectConfig();
        robotProjectConfig.addRemoteLocation(remoteLibrary);

        return libs;
    }
}
