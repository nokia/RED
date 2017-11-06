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
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
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
import org.robotframework.ide.eclipse.main.plugin.project.library.LibraryConstructor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

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
    }

    @Test
    public void markerIsReported_whenImportIsNotSpecified() {
        validateLibraryImport("");

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.MISSING_LIBRARY_NAME, new ProblemPosition(2, Range.closed(17, 24))));
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
        robotProject.setModuleSearchPaths(new ArrayList<File>());

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

        final LibrarySpecification spec = createNewLibrarySpecification(libName, constructor);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport(libName, refLibs);
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

        final LibrarySpecification spec = createNewLibrarySpecification(libPath, constructor);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport("lib.py", refLibs);
        assertThat(reporter.getReportedProblems()).contains(new Problem(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS,
                new ProblemPosition(2, Range.closed(26, 32))));

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByName_1() throws Exception {
        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport(libName, refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByPath_1() throws Exception {
        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport("lib.py", refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByName_2() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir = projectProvider.createDir("directory");
        projectProvider.createFile("directory/lib.py");

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport(libName, refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLocallyExistingLibraryIsImportedByPath_2() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/directory";
        final String libName = "lib";

        final IFolder dir = projectProvider.createDir("directory");
        projectProvider.createFile("directory/lib.py");

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport("directory/lib.py", refLibs);
        assertThat(reporter.getReportedProblems()).isEmpty();

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileExistLocallyButIsImportedUsingAbsolutePath()
            throws Exception {

        final String libPath = projectProvider.getProject().getName();
        final String libName = "lib";

        final IFile libFile = projectProvider.createFile("lib.py");
        final String absPath = libFile.getLocation().toString();

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport(absPath, refLibs);
        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE));

        libFile.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileExistLocallyAsALinkToExternalFile() throws Exception {
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested_lib.py");

        final String libPath = tmpFile.getParent().replaceAll("\\\\", "/");
        final String libName = "external_nested_lib";

        resourceCreator.createLink(tmpFile.toURI(), projectProvider.getFile("link.py"));

        final LibrarySpecification spec = createNewLibrarySpecification(libName);
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, libName, libPath), spec);

        validateLibraryImport(tmpFile.getAbsolutePath().replaceAll("\\\\", "/"), refLibs);
        assertThat(reporter.getReportedProblems()).are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE));
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaSysPathFromWorkspace() throws Exception {
        final String libPath = projectProvider.getProject().getName() + "/dir";

        final IFolder dir = projectProvider.createDir("dir");
        projectProvider.createFile("dir/lib.py");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir.getLocation().toFile()));

        final LibrarySpecification spec = createNewLibrarySpecification("lib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, "lib", libPath), spec);

        validateLibraryImport("lib.py", refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaSysPathFromExternalLocation() {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(newArrayList(dir));

        final LibrarySpecification spec = createNewLibrarySpecification("external_nested_lib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib", dir.getAbsolutePath()), spec);

        validateLibraryImport("external_nested_lib.py", refLibs);
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

        final LibrarySpecification spec = createNewLibrarySpecification("external_nested_lib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib", libPath), spec);

        validateLibraryImport("external_nested_lib.py", refLibs);
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
        robotProject.setModuleSearchPaths(new ArrayList<File>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getLocation().toString()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        final LibrarySpecification spec = createNewLibrarySpecification("lib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, "lib", libPath), spec);

        validateLibraryImport("lib.py", refLibs);
        assertThat(reporter.getReportedProblems())
                .are(onlyCausedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH));

        dir.delete(true, null);
    }

    @Test
    public void noMajorProblemsAreReported_whenLibraryFileIsImportedViaRedXmlPythonPathFromExternalLocation() {
        final File dir = getFile(tempFolder.getRoot(), "external_dir");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setModuleSearchPaths(new ArrayList<File>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getAbsolutePath()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        final LibrarySpecification spec = createNewLibrarySpecification("external_nested_lib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib", dir.getAbsolutePath()), spec);

        validateLibraryImport("external_nested_lib.py", refLibs);
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
        robotProject.setModuleSearchPaths(new ArrayList<File>());

        final List<SearchPath> paths = newArrayList(SearchPath.create(dir.getAbsolutePath()));
        robotProject.getRobotProjectConfig().setPythonPath(paths);

        final LibrarySpecification spec = createNewLibrarySpecification("external_nested_lib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(ReferencedLibrary.create(LibraryType.PYTHON, "external_nested_lib", libPath), spec);

        validateLibraryImport("external_nested_lib.py", refLibs);
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

    private LibrarySpecification createNewLibrarySpecification(final String libName) {
        return createNewLibrarySpecification(libName, null);
    }

    private LibrarySpecification createNewLibrarySpecification(final String libName,
            final LibraryConstructor constructor) {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setName(libName);
        spec.setConstructor(constructor);
        return spec;
    }

    private void validateLibraryImport(final String toImport) {
        validateLibraryImport(toImport, new HashMap<ReferencedLibrary, LibrarySpecification>());
    }

    private void validateLibraryImport(final String toImport,
            final Map<ReferencedLibrary, LibrarySpecification> refLibs) {
        final RobotSuiteFile suiteFile = createLibraryImportingSuite(toImport);
        final LibraryImport libImport = getImport(suiteFile);

        final FileValidationContext context = prepareContext(suiteFile, refLibs);

        final GeneralSettingsLibrariesImportValidator validator = new GeneralSettingsLibrariesImportValidator(context,
                suiteFile, newArrayList(libImport), reporter);
        try {
            validator.validate(null);
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to validate", e);
        }
    }

    private LibraryImport getImport(final RobotSuiteFile suiteFile) {
        return (LibraryImport) suiteFile.findSection(RobotSettingsSection.class).get()
                .getChildren().get(0).getLinkedElement();
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

    private FileValidationContext prepareContext(final RobotSuiteFile suiteFile,
            final Map<ReferencedLibrary, LibrarySpecification> refLibs) {
        final Map<String, LibrarySpecification> specsByName = new HashMap<>();
        refLibs.forEach((refLib, libSpec) -> specsByName.put(refLib.getName(), libSpec));

        final ValidationContext parentContext = new ValidationContext(model, RobotVersion.from("0.0"),
                SuiteExecutor.Python, specsByName, refLibs);
        return new FileValidationContext(parentContext, suiteFile.getFile());
    }

    private static File getFile(final File root, final String... path) {
        if (path == null || path.length == 0) {
            return root;
        } else {
            return getFile(new File(root, path[0]), Arrays.copyOfRange(path, 1, path.length));
        }
    }
}
