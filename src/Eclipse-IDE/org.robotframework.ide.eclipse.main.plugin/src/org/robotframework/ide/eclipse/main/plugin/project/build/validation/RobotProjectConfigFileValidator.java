/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;

import com.google.common.collect.ImmutableMap;

public class RobotProjectConfigFileValidator implements ModelUnitValidator {

    private final ValidationContext context;

    private final IFile configFile;

    private final ProblemsReportingStrategy reporter;

    public RobotProjectConfigFileValidator(final ValidationContext context, final IFile configFile,
            final ProblemsReportingStrategy reporter) {
        this.context = context;
        this.configFile = configFile;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        RobotProjectConfigWithLines config = null;
        try {
            config = new RobotProjectConfigReader().readConfigurationWithLines(configFile);
        } catch (final CannotReadProjectConfigurationException e) {
            // this problem is handled by RobotLibraries builder
            return;
        }

        final RobotProjectConfig model = config.getConfigurationModel();
        final Map<Object, ProblemPosition> linesMapping = config.getLinesMapping();
        for (final RemoteLocation location : model.getRemoteLocations()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateRemoteLocation(location, linesMapping, reporter);
        }

        int index = 0;
        for (final ReferencedLibrary library : model.getLibraries()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateReferencedLibrary(library, index, linesMapping, reporter);
            index++;
        }

        for (final ReferencedVariableFile variableFile : model.getReferencedVariableFiles()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateReferencedVariableFile(variableFile, linesMapping, reporter);
        }

        for (final ExcludedFolderPath excludedPath : model.getExcludedPath()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateExcludedPath(excludedPath, model.getExcludedPath(), linesMapping, reporter);
        }
    }

    private void validateRemoteLocation(final RemoteLocation location, final Map<Object, ProblemPosition> linesMapping,
            final ProblemsReportingStrategy reporter) throws CoreException {
        final URI uriAddress = location.getUriAddress();
        @SuppressWarnings("resource")
        final Socket s = new Socket();
        try {
            final SocketAddress sockaddr = new InetSocketAddress(uriAddress.getHost(), uriAddress.getPort());
            s.connect(sockaddr, 5000);
        } catch (final IOException | IllegalArgumentException ex) {
            final RobotProblem unreachableHostProblem = RobotProblem.causedBy(ConfigFileProblem.UNREACHABLE_HOST)
                    .formatMessageWith(uriAddress);
            reporter.handleProblem(unreachableHostProblem, configFile, linesMapping.get(location));
        } finally {
            try {
                s.close();
            } catch (final IOException e) {
                // fine
            }
        }
    }

    private void validateReferencedLibrary(final ReferencedLibrary library, final int index,
            final Map<Object, ProblemPosition> linesMapping, final ProblemsReportingStrategy reporter) {
        final LibraryType libType = library.provideType();
        final IPath libraryPath = Path.fromPortableString(library.getPath());
        final ProblemPosition position = linesMapping.get(library);

        final Map<String, Object> additional = ImmutableMap.<String, Object> of(ConfigFileProblem.LIBRARY_INDEX, index);
        List<RobotProblem> libProblems;
        switch (libType) {
            case JAVA:
                libProblems = findJavaLibaryProblem(libraryPath, library.getName());
                break;
            case PYTHON:
                libProblems = findPythonLibraryProblem(libraryPath, library.getName());
                break;
            case VIRTUAL:
                libProblems = findVirtualLibaryProblem(libraryPath);
                break;
            default:
                libProblems = newArrayList();
                break;
        }

        for (final RobotProblem problem : libProblems) {
            reporter.handleProblem(problem, configFile, position, additional);
        }
    }

    private List<RobotProblem> findJavaLibaryProblem(final IPath libraryPath, final String libName) {
        final List<RobotProblem> javaLibProblems = newArrayList();

        if (!"jar".equals(libraryPath.getFileExtension())) {
            javaLibProblems.add(
                    RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_NOT_A_JAR_FILE).formatMessageWith(libraryPath));
        }
        javaLibProblems.addAll(validateLibraryPath(libraryPath, ConfigFileProblem.MISSING_LIBRARY_FILE));

        final IPath absolutePath = PathsConverter.toAbsoluteFromWorkspaceRelativeIfPossible(libraryPath);
        final RobotProject robotProject = context.getModel().createRobotProject(configFile.getProject());
        boolean containsClass = false;
        for (final JarClass jarClass : new JarStructureBuilder(robotProject.getRuntimeEnvironment(),
                robotProject.getRobotProjectConfig()).provideEntriesFromFile(absolutePath.toFile())) {
            if (jarClass.getQualifiedName().equals(libName)) {
                containsClass = true;
                break;
            }
        }
        if (!containsClass) {
            javaLibProblems.add(RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_MISSING_CLASS)
                    .formatMessageWith(libraryPath, libName));
        }

        if (context.getExecutorInUse() != SuiteExecutor.Jython) {
            javaLibProblems.add(RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_IN_NON_JAVA_ENV)
                    .formatMessageWith(libraryPath, context.getExecutorInUse()));
        }
        return javaLibProblems;
    }

    private List<RobotProblem> findPythonLibraryProblem(final IPath libraryPath, final String libName) {
        final List<RobotProblem> pyLibProblems = newArrayList();
        pyLibProblems.addAll(validateLibraryPath(libraryPath, ConfigFileProblem.MISSING_LIBRARY_FILE));
        // TODO validate classes
        return pyLibProblems;
    }

    private List<RobotProblem> findVirtualLibaryProblem(final IPath libraryPath) {
        return newArrayList(validateLibraryPath(libraryPath, ConfigFileProblem.MISSING_LIBRARY_FILE));
    }

    private List<RobotProblem> validateLibraryPath(final IPath libraryPath,
            final ConfigFileProblem missingFileProblem) {
        final List<RobotProblem> problems = newArrayList();
        if (libraryPath.isAbsolute()) {
            problems.add(RobotProblem.causedBy(ConfigFileProblem.ABSOLUTE_PATH).formatMessageWith(libraryPath));
            if (!libraryPath.toFile().exists()) {
                problems.add(RobotProblem.causedBy(missingFileProblem).formatMessageWith(libraryPath));
            }
        } else {
            final IResource libspec = ResourcesPlugin.getWorkspace().getRoot().findMember(libraryPath);
            if (libspec == null || !libspec.exists()) {
                problems.add(RobotProblem.causedBy(missingFileProblem).formatMessageWith(libraryPath));
            }
        }
        return problems;
    }

    private void validateReferencedVariableFile(final ReferencedVariableFile variableFile,
            final Map<Object, ProblemPosition> linesMapping, final ProblemsReportingStrategy reporter) {

        final IPath libraryPath = Path.fromPortableString(variableFile.getPath());
        final List<RobotProblem> pathProblems = validateLibraryPath(libraryPath,
                ConfigFileProblem.MISSING_VARIABLE_FILE);
        for (final RobotProblem pathProblem : pathProblems) {
            reporter.handleProblem(pathProblem, configFile, linesMapping.get(variableFile));
        }
    }

    private void validateExcludedPath(final ExcludedFolderPath excludedPath, final List<ExcludedFolderPath> allExcluded,
            final Map<Object, ProblemPosition> linesMapping, final ProblemsReportingStrategy reporter) {
        final IProject project = configFile.getProject();
        final IPath asExcludedPath = excludedPath.asPath();
        final Path projectPath = new Path(project.getName());
        if (!project.exists(asExcludedPath)) {
            final RobotProblem problem = RobotProblem.causedBy(ConfigFileProblem.MISSING_EXCLUDED_FOLDER)
                    .formatMessageWith(projectPath.append(asExcludedPath));
            reporter.handleProblem(problem, configFile, linesMapping.get(excludedPath));
        }

        for (final ExcludedFolderPath otherPath : allExcluded) {
            if (otherPath != excludedPath) {
                final IPath otherAsPath = otherPath.asPath();
                if (otherAsPath.isPrefixOf(asExcludedPath)) {
                    final RobotProblem problem = RobotProblem.causedBy(ConfigFileProblem.USELESS_FOLDER_EXCLUSION)
                            .formatMessageWith(projectPath.append(asExcludedPath), projectPath.append(otherAsPath));
                    reporter.handleProblem(problem, configFile, linesMapping.get(excludedPath));
                }
            }
        }

    }
}
