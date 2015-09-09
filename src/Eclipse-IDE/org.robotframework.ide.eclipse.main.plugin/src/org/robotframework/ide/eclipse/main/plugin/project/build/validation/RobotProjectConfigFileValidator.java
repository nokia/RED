/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.Location;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder.JarClass;

public class RobotProjectConfigFileValidator implements ModelUnitValidator {

    private final ValidationContext validationContext;

    private final IFile configFile;

    public RobotProjectConfigFileValidator(final ValidationContext validationContext, final IFile configFile) {
        this.validationContext = validationContext;
        this.configFile = configFile;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();
        RobotProjectConfigWithLines config = null;
        try {
            config = new RobotProjectConfigReader().readConfigurationWithLines(configFile);
        } catch (final CannotReadProjectConfigurationException e) {
            // this problem is handled by RobotLibraries builder
            return;
        }

        final RobotProjectConfig model = config.getConfigurationModel();
        final Map<Object, Location> linesMapping = config.getLinesMapping();
        for (final RemoteLocation location : model.getRemoteLocations()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateRemoteLocation(location, configFile, linesMapping, reporter);
        }

        int index = 0;
        for (final ReferencedLibrary library : model.getLibraries()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateReferencedLibrary(library, index, configFile, linesMapping, reporter);
            index++;
        }
    }

    private void validateRemoteLocation(final RemoteLocation location, final IFile configFile,
            final Map<Object, Location> linesMapping, final ProblemsReportingStrategy reporter) throws CoreException {
        final URI uriAddress = location.getUriAddress();
        try (Socket s = new Socket(uriAddress.getHost(), uriAddress.getPort())) {
            // that's fine
        } catch (final IOException ex) {
            final RobotProblem unreachableHostProblem = RobotProblem.causedBy(ConfigFileProblem.UNREACHABLE_HOST)
                    .formatMessageWith(uriAddress);
            reporter.handleProblem(unreachableHostProblem, configFile, linesMapping.get(location).getLineNumber());
        }
    }

    private void validateReferencedLibrary(final ReferencedLibrary library, final int index, final IFile configFile,
            final Map<Object, Location> linesMapping, final ProblemsReportingStrategy reporter) {
        final LibraryType libType = library.provideType();
        final IPath libraryPath = Path.fromPortableString(library.getPath());
        final int lineNumber = linesMapping.get(library).getLineNumber();

        final Map<String, Object> additional = new HashMap<>();
        additional.put(ConfigFileProblem.LIBRARY_INDEX, index);

        switch (libType) {
            case JAVA:
            reporter.handleProblem(findJavaLibaryProblem(libraryPath, library.getName()),
                    configFile, lineNumber, additional);
                break;
            case VIRTUAL:
                reporter.handleProblem(findVirtualLibaryProblem(libraryPath), configFile, lineNumber, additional);
                break;
            default:
                break;
        }

    }

    private RobotProblem findJavaLibaryProblem(final IPath libraryPath, final String libName) {
        final File filePath = libraryPath.toFile();
        if (!"jar".equals(libraryPath.getFileExtension())) {
            return RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_NOT_A_JAR_FILE)
                    .formatMessageWith(libraryPath);
        }
        if (!filePath.exists()) {
            return RobotProblem.causedBy(ConfigFileProblem.MISSING_JAR_FILE).formatMessageWith(
                    libraryPath);
        } else {
            boolean containsClass = false;
            for (final JarClass jarClass : new JarStructureBuilder().provideEntriesFromJarFile(filePath.toString())) {
                if (jarClass.getQualifiedName().equals(libName)) {
                    containsClass = true;
                    break;
                }
            }
            if (!containsClass) {
                return RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_MISSING_CLASS)
                        .formatMessageWith(libraryPath, libName);
            }
        }
        return null;
    }

    private RobotProblem findVirtualLibaryProblem(final IPath libraryPath) {
        if (libraryPath.isAbsolute()) {
            return RobotProblem.causedBy(ConfigFileProblem.ABSOLUTE_PATH).formatMessageWith(
                    libraryPath);
        }
        final IResource libspec = ResourcesPlugin.getWorkspace().getRoot().findMember(libraryPath);
        if (libspec == null || !libspec.exists()) {
            return RobotProblem.causedBy(ConfigFileProblem.MISSING_LIBSPEC_FILE)
                    .formatMessageWith(libraryPath);
        }
        return null;
    }
}
