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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.red.junit.ProjectProvider;

public class RobotProjectConfigFileValidatorTest {

    private static final String PROJECT_NAME = RobotProjectConfigFileValidatorTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private RobotProjectConfigFileValidator validator;

    private MockReporter reporter;

    @Before
    public void beforeTest() throws Exception {
        reporter = new MockReporter();
        final ValidationContext context = mock(ValidationContext.class);
        when(context.getModel()).thenReturn(new RobotModel());
        final IFile file = mock(IFile.class);
        when(file.getProject()).thenReturn(projectProvider.getProject());
        validator = new RobotProjectConfigFileValidator(context, file, reporter);
    }

    @Test
    public void whenConfigIsNewlyCreated_itHasNoValidationIssues() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final Map<Object, ProblemPosition> locations = new HashMap<>();
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);

        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenRemoteLocationHostDoesNotExist_unreachableHostProblemIsReported() throws Exception {
        // opens the socket in order to find port, but the socket gets closed immediately
        final RemoteLocation remoteLocation = RemoteLocation.create("http://127.0.0.1:" + findFreePort() + "/");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addRemoteLocation(remoteLocation);
        
        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(remoteLocation, new ProblemPosition(42));
        
        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);

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
            final RobotProjectConfig config = new RobotProjectConfig();
            config.addRemoteLocation(remoteLocation);

            final Map<Object, ProblemPosition> locations = new HashMap<>();
            locations.put(remoteLocation, new ProblemPosition(42));

            final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
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

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addClassPath(searchPath);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(searchPath, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenAbsoluteSearchPathDoesNotExist_missingLocationProblemIsReported() throws Exception {
        final File folder = temporaryFolder.newFolder();
        final String path = folder.getAbsolutePath();
        folder.delete();

        final SearchPath searchPath = SearchPath.create(path);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addClassPath(searchPath);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(searchPath, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_SEARCH_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenRelativePathExist_nothingIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("folder"));

        final SearchPath searchPath = SearchPath.create(PROJECT_NAME + "/folder");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(searchPath, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenRelativePathDoesNotExist_missingLocationProblemIsReported() throws Exception {
        final SearchPath searchPath = SearchPath.create(PROJECT_NAME + "/folder");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(searchPath, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_SEARCH_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenVariableFileExist_nothingIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile.create(PROJECT_NAME + "/a/vars.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(variableFile, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void whenVariableFileDoesntExist_missingFileIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars2.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile.create(PROJECT_NAME + "a/vars.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(variableFile, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
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

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(variableFile, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.ABSOLUTE_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenVariableFileDoesntExistAndIsRelatedWithAbsolutePath_missingFileAndAbsolutePathWarningAreReported()
            throws Exception {
        projectProvider.createDir(Path.fromPortableString("a"));
        projectProvider.createFile(Path.fromPortableString("a/vars2.py"), "VAR = 100");

        final ReferencedVariableFile variableFile = ReferencedVariableFile
                .create(projectProvider.getProject().getLocation() + "/a/vars.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedVariableFile(variableFile);

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(variableFile, new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(ConfigFileProblem.MISSING_VARIABLE_FILE, new ProblemPosition(42)),
                new Problem(ConfigFileProblem.ABSOLUTE_PATH, new ProblemPosition(42)));
    }

    @Test
    public void whenValidationExcludedPathDoesNotExistInProject_warningIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("directory"));

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath(Path.fromPortableString("does/not/exist"));

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(config.getExcludedPath().get(0), new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
        validator.validate(new NullProgressMonitor(), linesAugmentedConfig);

        assertThat(reporter.getReportedProblems())
                .containsExactly(new Problem(ConfigFileProblem.MISSING_EXCLUDED_FOLDER, new ProblemPosition(42)));
    }

    @Test
    public void whenValidationExcludedPathExistInProject_nothingIsReported() throws Exception {
        projectProvider.createDir(Path.fromPortableString("directory"));

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath(Path.fromPortableString("directory"));

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(config.getExcludedPath().get(0), new ProblemPosition(42));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
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

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath(Path.fromPortableString("directory/nested"));
        config.addExcludedPath(Path.fromPortableString("directory/nested/1/2"));

        final Map<Object, ProblemPosition> locations = new HashMap<>();
        locations.put(config.getExcludedPath().get(0), new ProblemPosition(42));
        locations.put(config.getExcludedPath().get(1), new ProblemPosition(43));

        final RobotProjectConfigWithLines linesAugmentedConfig = new RobotProjectConfigWithLines(config, locations);
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
