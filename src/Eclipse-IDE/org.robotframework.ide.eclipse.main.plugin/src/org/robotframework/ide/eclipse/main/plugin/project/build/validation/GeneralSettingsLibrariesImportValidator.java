/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.ImmutableMap;

public class GeneralSettingsLibrariesImportValidator extends GeneralSettingsImportsValidator {

    public GeneralSettingsLibrariesImportValidator(final FileValidationContext validationContext,
            final RobotSuiteFile suiteFile, final List<LibraryImport> imports,
            final ValidationReportingStrategy reporter) {
        super(validationContext, suiteFile, imports, reporter);
    }

    @Override
    protected IProblemCause getCauseForMissingImportArguments() {
        return GeneralSettingsProblem.MISSING_LIBRARY_NAME;
    }

    @Override
    protected IProblemCause getCauseForNonExistingImport() {
        return GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT;
    }

    @Override
    protected boolean isPathImport(final String pathOrName) {
        return pathOrName.endsWith("/") || pathOrName.endsWith(".py") || pathOrName.endsWith(".class")
                || pathOrName.endsWith(".java");
    }

    @Override
    protected void validateResource(final IResource resource, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {
        final IPath candidate = resource.getLocation();

        final LibrarySpecification spec = findSpecification(candidate);
        validateWithSpec(spec, path, pathToken, arguments, true);
    }

    @Override
    protected void validateFile(final File file, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {
        final IPath candidate = new Path(file.getAbsolutePath());

        final LibrarySpecification spec = findSpecification(candidate);
        validateWithSpec(spec, path, pathToken, arguments, true);
    }

    private LibrarySpecification findSpecification(final IPath candidate) {
        final Map<ReferencedLibrary, LibrarySpecification> libs = validationContext
                .getReferencedLibrarySpecifications();
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : libs.entrySet()) {
            final IPath entryPath = new Path(entry.getKey().getFilepath().getPath());
            final IPath libPath1 = RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(entryPath);
            final IPath libPath2 = RedWorkspace.Paths
                    .toAbsoluteFromWorkspaceRelativeIfPossible(entryPath.addFileExtension("py"));
            if (candidate.equals(libPath1) || candidate.equals(libPath2)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    protected void validateNameImport(final String name, final RobotToken nameToken, final List<RobotToken> arguments)
            throws CoreException {
        final String libName = createLibName(name, arguments);
        final LibrarySpecification specification = validationContext.getLibrarySpecifications(libName);
        validateWithSpec(specification, name, nameToken, arguments, false);
    }

    private String createLibName(final String name, final List<RobotToken> arguments) {
        if ("Remote".equals(name)) {
            if (arguments.isEmpty()) {
                return name + " " + "http://127.0.0.1:8270/RPC2";
            } else {
                final String remoteLibName = RemoteLocation.createRemoteName(arguments.get(0).getText());
                return remoteLibName;
            }
        }
        return name;
    }

    private void validateWithSpec(final LibrarySpecification specification, final String pathOrName,
            final RobotToken pathOrNameToken, final List<RobotToken> importArguments, final boolean isPath) {
        if (specification != null) {
            final ArgumentsDescriptor descriptor = specification.getConstructor() == null
                    ? ArgumentsDescriptor.createDescriptor()
                    : specification.getConstructor().createArgumentsDescriptor();
            new GeneralKeywordCallArgumentsValidator(validationContext.getFile(), pathOrNameToken, reporter, descriptor,
                    importArguments).validate(new NullProgressMonitor());
        } else {
            if (!pathOrName.equals("Remote")) {
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT)
                        .formatMessageWith(pathOrName);
                final Map<String, Object> additional = isPath
                        ? ImmutableMap.of(AdditionalMarkerAttributes.PATH, pathOrName)
                        : ImmutableMap.of(AdditionalMarkerAttributes.NAME, pathOrName);
                reporter.handleProblem(problem, validationContext.getFile(), pathOrNameToken, additional);
            } else {
                validateArgumentsForRemoteLibraryImport(pathOrName, pathOrNameToken, importArguments);
            }
        }
    }

    private void validateArgumentsForRemoteLibraryImport(final String name, final RobotToken nameToken,
            final List<RobotToken> arguments) {
        if (arguments.isEmpty()) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.MISSING_ARGUMENT_FOR_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(name);
            reporter.handleProblem(problem, validationContext.getFile(), nameToken);
        } else if (arguments.size() > 1) {
            final String additionalMsg = "To many arguments for '" + name + "' library import";
            final RobotProblem problem = RobotProblem
                    .causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                    .formatMessageWith(additionalMsg);
            reporter.handleProblem(problem, validationContext.getFile(), nameToken);
        } else {
            validateRemoteLocation(name, arguments);
        }
    }

    private void validateRemoteLocation(final String name, final List<RobotToken> arguments) {
        final RobotProjectConfig robotProjectConfig = validationContext.getProjectConfiguration();
        final List<RemoteLocation> remoteLocations = robotProjectConfig.getRemoteLocations();
        final RobotToken adressToken = arguments.get(0);
        final String address = RemoteLocation.createRemoteUri(arguments.get(0).getText());
        final RemoteLocation remoteLibrary = RemoteLocation.create(address);

        if (remoteLocations.contains(remoteLibrary)) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(name, address);
            reporter.handleProblem(problem, validationContext.getFile(), adressToken);
        } else {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML)
                    .formatMessageWith(name, address);
            final Map<String, Object> additional = ImmutableMap.of(AdditionalMarkerAttributes.PATH, address);
            reporter.handleProblem(problem, validationContext.getFile(), adressToken, additional);
        }
    }
}
