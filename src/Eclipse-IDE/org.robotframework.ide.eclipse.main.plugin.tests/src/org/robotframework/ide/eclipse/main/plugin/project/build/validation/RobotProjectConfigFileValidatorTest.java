/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.Libraries;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class RobotProjectConfigFileValidatorTest {

    private static final String PROJECT_NAME = RobotProjectConfigFileValidatorTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private RobotModel model;

    private RobotProjectConfigFileValidator validator;

    private MockReporter reporter;

    @Before
    public void beforeTest() throws Exception {
        reporter = new MockReporter();
        final ValidationContext context = mock(ValidationContext.class);
        model = new RobotModel();
        when(context.getModel()).thenReturn(model);
        final IFile file = mock(IFile.class);
        when(file.getProject()).thenReturn(projectProvider.getProject());
        validator = new RobotProjectConfigFileValidator(context, file, reporter);
    }

    @Test
    public void whenConfigIsNewlyCreated_itHasNoValidationIssues() throws Exception {
        final RobotProjectConfig config = RobotProjectConfig.create();
        final Map<Object, FilePosition> locations = new HashMap<>();
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenConfigHasOldVersionFormat_itHasNoValidationIssues() throws Exception {
        // remove this test after red.xml version change
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.setVersion("1.0");
        final Map<Object, FilePosition> locations = new HashMap<>();
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenConfigHasIncompatibleVersionFormat_InvalidVersionProblemIsReported() throws Exception {
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.setVersion("invalid");

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(config.getVersion(), new FilePosition(3, 0));
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.INVALID_VERSION, new ProblemPosition(3)));
    }

    @Test
    public void whenLibspecForStandardLibraryIsMissing_notGeneratedProblemIsReported() throws Exception {
        final Map<LibraryDescriptor, LibrarySpecification> stdLibs = Libraries.createStdLib("StdLib1");
        stdLibs.put(LibraryDescriptor.ofStandardLibrary("StdLib2"), null);

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(stdLibs);
        
        final RobotProjectConfig config = RobotProjectConfig.create();

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(config, new FilePosition(2, 0));
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(
                        new Problem(ConfigFileProblem.LIBRARY_SPEC_CANNOT_BE_GENERATED, new ProblemPosition(2)));
    }

    @Test
    public void whenLibspecForStandardRemoteLibraryIsMissing_notGeneratedProblemIsReported() throws Exception {
        final String path = "http://127.0.0.1:" + findFreePort() + "/";

        final LibrarySpecification libSpec = new LibrarySpecification();
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create(path));
        libSpec.setDescriptor(descriptor);
        libSpec.setName("Remote");
        final Map<LibraryDescriptor, LibrarySpecification> stdLibs = new HashMap<>();
        stdLibs.put(descriptor, null);

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(stdLibs);
        robotProject.setReferencedLibraries(new HashMap<>());

        final RemoteLocation remoteLocation = RemoteLocation.create(path);
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addRemoteLocation(remoteLocation);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(remoteLocation, new FilePosition(42, 0));
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        // there is also unreachable host problem
        assertThat(reporter.getReportedProblems()).hasSize(2)
                .contains(new Problem(ConfigFileProblem.LIBRARY_SPEC_CANNOT_BE_GENERATED, new ProblemPosition(42)));
    }

    @Test
    public void whenLibspecForReferencedLibraryIsMissing_notGeneratedProblemIsReported() throws CoreException {
        final ReferencedLibrary refLib1 = ReferencedLibrary.create(LibraryType.PYTHON, "ref", "");
        final ReferencedLibrary refLib2 = ReferencedLibrary.create(LibraryType.PYTHON, "missing", "");

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(refLib1), LibrarySpecification.create("ref"));
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(refLib2), null);
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(refLibs);

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addReferencedLibrary(refLib1);
        config.addReferencedLibrary(refLib2);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(refLib1, new FilePosition(1728, 0));
        locations.put(refLib2, new FilePosition(1729, 0));
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(ConfigFileProblem.LIBRARY_SPEC_CANNOT_BE_GENERATED, new ProblemPosition(1729)));
    }

    @Test
    public void whenRemoteLocationHostDoesNotExist_unreachableHostProblemIsReported() throws Exception {
        // opens the socket in order to find port, but the socket gets closed immediately
        final RemoteLocation remoteLocation = RemoteLocation.create("http://127.0.0.1:" + findFreePort() + "/");
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addRemoteLocation(remoteLocation);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(remoteLocation, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.UNREACHABLE_HOST, new ProblemPosition(42)));
    }

    @Test
    public void whenRemoteLocationHostDoesExist_nothingIsReported() throws Exception {
        // opens the socket in order to find port, but the socket remains open till the test end, so
        // no validation error is expected
        try (ServerSocket socket = new ServerSocket(0)) {
            final RemoteLocation remoteLocation = RemoteLocation
                    .create("http://127.0.0.1:" + socket.getLocalPort() + "/");
            final RobotProjectConfig config = RobotProjectConfig.create();
            config.addRemoteLocation(remoteLocation);

            final Map<Object, FilePosition> locations = new HashMap<>();
            locations.put(remoteLocation, new FilePosition(42, 0));

            final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                    new TreeSet<>(), locations);
            validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

            assertThat(reporter.getReportedProblems()).isEmpty();
        } finally {
            // nothing to do, just let the socket close itself
        }
    }

    @Test
    public void whenAbsoluteSearchPathExist_nothingIsReported() throws Exception {
        final File folder = temporaryFolder.newFolder();

        final SearchPath searchPath = SearchPath.create(folder.getAbsolutePath());

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addClassPath(searchPath);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(searchPath, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenAbsoluteSearchPathDoesNotExist_missingLocationProblemIsReported() throws Exception {
        final File folder = temporaryFolder.newFolder();
        final String path = folder.getAbsolutePath();
        folder.delete();

        final SearchPath searchPath = SearchPath.create(path);

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addClassPath(searchPath);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(searchPath, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_SEARCH_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenRelativePathExist_nothingIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("folder"));

        final SearchPath searchPath = SearchPath.create(PROJECT_NAME + "/folder");

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addPythonPath(searchPath);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(searchPath, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenRelativePathDoesNotExist_missingLocationProblemIsReported() throws Exception {
        final SearchPath searchPath = SearchPath.create(PROJECT_NAME + "/folder");

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addPythonPath(searchPath);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(searchPath, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_SEARCH_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenVariableFileExist_nothingIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile.create(PROJECT_NAME + "/a/vars.py");

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(variableFile, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenVariableFileDoesNotExist_missingFileIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars2.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile.create(PROJECT_NAME + "a/vars.py");

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(variableFile, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_VARIABLE_FILE, new ProblemPosition(42)));
    }

    @Test
    public void whenVariableFileExistAndIRelatedWithAbsolutePath_absolutePathWarningIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile
                .create(projectProvider.getProject().getLocation() + "/a/vars.py");

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(variableFile, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.ABSOLUTE_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenVariableFileDoesNotExistAndIsRelatedWithAbsolutePath_missingFileAndAbsolutePathWarningAreReported()
            throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars2.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile
                .create(projectProvider.getProject().getLocation() + "/a/vars.py");

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(variableFile, new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(ConfigFileProblem.MISSING_VARIABLE_FILE, new ProblemPosition(42)),
                new Problem(ConfigFileProblem.ABSOLUTE_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenValidationExcludedPathDoesNotExistInProject_warningIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("directory"));

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addExcludedPath("does/not/exist");

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(config.getExcludedPath().get(0), new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_EXCLUDED_FOLDER, new ProblemPosition(42)));
    }

    @Test
    public void whenValidationExcludedPathExistInProject_nothingIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("directory"));

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addExcludedPath("directory");

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(config.getExcludedPath().get(0), new FilePosition(42, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenValidationExcludedPathExcludesAnotherPath_warningIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("directory"));
        projectProvider.createDir(Path.fromPortableString("directory/nested"));
        projectProvider.createDir(Path.fromPortableString("directory/nested/1"));
        projectProvider.createDir(Path.fromPortableString("directory/nested/1/2"));
        projectProvider.createDir(Path.fromPortableString("directory/nested/1/2/3"));

        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addExcludedPath("directory/nested");
        config.addExcludedPath("directory/nested/1/2");

        final Map<Object, FilePosition> locations = new HashMap<>();
        locations.put(config.getExcludedPath().get(0), new FilePosition(42, 0));
        locations.put(config.getExcludedPath().get(1), new FilePosition(43, 0));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config,
                new TreeSet<>(), locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.USELESS_FOLDER_EXCLUSION, new ProblemPosition(43)));
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            throw new IllegalStateException();
        }
    }
}
