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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Range;

public class GeneralSettingsVariablesImportValidatorTest {

    public static ProjectProvider projectProvider = new ProjectProvider(
            GeneralSettingsVariablesImportValidatorTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    private RobotModel model;

    private MockReporter reporter;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final File root = tempFolder.getRoot();

        getFile(root, "external.py").createNewFile();
        getFile(root, "external_dir").mkdir();
        getFile(root, "external_dir", "external_nested.py").createNewFile();
        getFile(root, "external_dir", "external_nested.txt").createNewFile();

        projectProvider.configure();
    }

    @Before
    public void beforeTest() {
        model = new RobotModel();
        reporter = new MockReporter();
    }

    @Test
    public void markerIsReported_whenImportIsNotSpecified() {
        validateVariablesImport("");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.MISSING_VARIABLES_NAME, new ProblemPosition(2, Range.closed(17, 26))));
    }

    @Test
    public void markerIsReported_whenImportingUnknownThing() {
        validateVariablesImport("something");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NON_EXISTING_VARIABLES_IMPORT, new ProblemPosition(2, Range.closed(28, 37))));
    }

    @Test
    public void markerIsReported_whenImportContainsUnknownVariables() {
        validateVariablesImport("${unknown}/file.py");

        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_PARAMETERIZED, new ProblemPosition(2, Range.closed(28, 46))));
    }

    @Test
    public void markerIsReported_whenUsingUnescapedWindowsPaths_1() {
        validateVariablesImport("d:\\folder\\res.py");

        assertThat(reporter.getReportedProblems()).containsOnly(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS,
                new ProblemPosition(2, Range.closed(28, 44))));
    }

    @Test
    public void markerIsReported_whenUsingUnescapedWindowsPaths_2() {
        validateVariablesImport("..\\..\\res.py");

        assertThat(reporter.getReportedProblems()).containsOnly(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS,
                new ProblemPosition(2, Range.closed(28, 40))));
    }

    @Test
    public void markerIsReported_whenUsingUnescapedWindowsPaths_3() {
        validateVariablesImport("..\\../res.py");

        assertThat(reporter.getReportedProblems()).containsOnly(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS,
                new ProblemPosition(2, Range.closed(28, 40))));
    }

    @Test
    public void markerIsReported_whenUsingAbsolutePathImport() throws Exception {
        final File tmpFile = tempFolder.newFile("file.py");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateVariablesImport(absPath);

        assertThat(reporter.getReportedProblems()).contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE,
                new ProblemPosition(2, Range.closed(28, 28 + absPath.length()))));
    }

    @Test
    public void markerIsReported_whenFileIsImportedRelativelyViaSysPathInsteadOfLocally() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(tmpFile.getParentFile()));

        validateVariablesImport("external_nested.py");

        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        new ProblemPosition(2, Range.closed(28, 46))));
    }

    @Test
    public void markerIsReported_whenFileIsImportedRelativelyViaRedXmlPythonPathInsteadOfLocally() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(tmpFile.getParent()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        validateVariablesImport("external_nested.py");

        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        new ProblemPosition(2, Range.closed(28, 46))));

        robotProject.getRobotProjectConfig().setPythonPath(null);
    }

    @Test
    public void markerIsReported_whenFileIsImportedFromOutsideOfWorkspace_1() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(tmpFile.getParentFile()));

        validateVariablesImport("external_nested.py");

        assertThat(reporter.getReportedProblems()).contains(new Problem(
                GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE, new ProblemPosition(2, Range.closed(28, 46))));
    }

    @Test
    public void markerIsReported_whenFileIsImportedFromOutsideOfWorkspace_2() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested.py");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateVariablesImport(absPath);

        assertThat(reporter.getReportedProblems())
                .contains(new Problem(GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE,
                        new ProblemPosition(2, Range.closed(28, 28 + absPath.length()))));
    }

    @Test
    public void markerIsReported_whenExternalFileDoesNotExist() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "non_existing.py");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateVariablesImport(absPath);

        assertThat(reporter.getReportedProblems()).contains(
                new Problem(GeneralSettingsProblem.NON_EXISTING_VARIABLES_IMPORT,
                        new ProblemPosition(2, Range.closed(28, 28 + absPath.length()))));
    }

    @Test
    public void markerIsReported_whenFileInWorkspaceDoesNotExist() {
        validateVariablesImport("non_existing.py");

        assertThat(reporter.getReportedProblems()).contains(new Problem(
                GeneralSettingsProblem.NON_EXISTING_VARIABLES_IMPORT, new ProblemPosition(2, Range.closed(28, 43))));
    }

    @Test
    public void markerIsReported_whenImportedVariablesLiesInDifferentDirectory() throws Exception {
        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/res.py");

        validateVariablesImport("res.py");
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                GeneralSettingsProblem.NON_EXISTING_VARIABLES_IMPORT, new ProblemPosition(2, Range.closed(28, 34))));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileExistLocally_1() throws Exception {
        final IFile file = projectProvider.createFile("res.robot");

        validateVariablesImport("res.robot");
        assertThat(reporter.getReportedProblems()).isEmpty();

        file.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileExistLocally_2() throws Exception {
        final IFolder dir = projectProvider.createDir("directory");
        projectProvider.createFile("directory/res.robot");

        validateVariablesImport("directory/res.robot");
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenWorkspaceImportIsNotAFile() throws Exception {
        final IFolder dir = projectProvider.createDir("directory");

        validateVariablesImport("directory");
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenExternalImportIsNotAFile() {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir");

        final String absPath = tmpFile.getAbsolutePath().replaceAll("\\\\", "/");
        validateVariablesImport(absPath);

        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE,
                GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE));
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileExistLocallyButIsImportedUsingAbsolutePath()
            throws Exception {
        final IFile resFile = projectProvider.createFile("res.robot");

        final String absPath = resFile.getLocation().toString();
        validateVariablesImport(absPath);

        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE));

        resFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileExistLocallyAsALinkToExternalFile() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested.py");

        resourceCreator.createLink(tmpFile.toURI(), projectProvider.getFile("link.py"));

        validateVariablesImport(tmpFile.getAbsolutePath().replaceAll("\\\\", "/"));
        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE));
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileIsImportedViaSysPathFromWorkspace() throws Exception {
        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/res.robot");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir.getLocation().toFile()));

        validateVariablesImport("res.robot");
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileIsImportedViaSysPathFromExternalLocation() {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir));

        validateVariablesImport("external_nested.py");
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE));
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileIsImportedViaSysPathFromExternalLocationWhichIsLinkedInWorkspace()
            throws Exception {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        resourceCreator.createLink(dir.toURI(), projectProvider.getProject().getFolder("linking_dir"));

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir));

        validateVariablesImport("external_nested.py");
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileIsImportedViaRedXmlPythonPathFromWorkspace()
            throws Exception {
        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/res.robot");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getLocation().toString()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        validateVariablesImport("res.robot");
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileIsImportedViaRedXmlPythonPathFromExternalLocation() {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getAbsolutePath()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        validateVariablesImport("external_nested.py");

        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH,
                        GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE));
    }

    @Test
    public void noMajorProblemsAreReported_whenVariablesFileIsImportedViaRedXmlPythonPathFromExternalLocationWhichIsLinkedInWorkspace()
            throws Exception {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        resourceCreator.createLink(dir.toURI(), projectProvider.getProject().getFolder("linking_dir"));

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getAbsolutePath()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        validateVariablesImport("external_nested.py");
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

    private void validateVariablesImport(final String toImport) {
        final RobotSuiteFile suiteFile = createVariablesImportingSuite(toImport);
        final VariablesImport varsImport = getImport(suiteFile);

        final FileValidationContext context = prepareContext(suiteFile);

        final GeneralSettingsVariablesImportValidator validator = new GeneralSettingsVariablesImportValidator(context,
                suiteFile, newArrayList(varsImport), reporter);
        try {
            validator.validate(null);
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to validate", e);
        }
    }

    private RobotSuiteFile createVariablesImportingSuite(final String toImport) {
        try {
            final IFile file = projectProvider.createFile("suite.robot",
                    "*** Settings ***",
                    "Variables  " + toImport);
            final RobotSuiteFile suite = model.createSuiteFile(file);
            suite.dispose();
            return suite;
        } catch (IOException | CoreException e) {
            throw new IllegalStateException("Cannot create file", e);
        }
    }

    private FileValidationContext prepareContext(final RobotSuiteFile suiteFile) {
        final ValidationContext parentContext = new ValidationContext(suiteFile.getProject().getRobotProjectConfig(),
                model, RobotVersion.from("0.0"), SuiteExecutor.Python, ArrayListMultimap.create(), new HashMap<>());
        return new FileValidationContext(parentContext, suiteFile.getFile());
    }

    private VariablesImport getImport(final RobotSuiteFile suiteFile) {
        return (VariablesImport) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getLinkedElement();
    }

    private static File getFile(final File root, final String... path) {
        if (path == null || path.length == 0) {
            return root;
        } else {
            return getFile(new File(root, path[0]), Arrays.copyOfRange(path, 1, path.length));
        }
    }
}
