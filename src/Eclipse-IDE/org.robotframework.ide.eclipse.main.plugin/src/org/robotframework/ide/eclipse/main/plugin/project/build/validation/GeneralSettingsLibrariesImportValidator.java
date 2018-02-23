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
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
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
        validateArgumentsForRemoteLibraryImport(name, nameToken, arguments);
    }

    private String createLibName(final String name, final List<RobotToken> arguments) {
        if ("Remote".equals(name)) {
            if (arguments.isEmpty()) {
                return name + " " + "http://127.0.0.1:8270/RPC2";
            } else {
                final String libName = name + " " + arguments.get(0).getText();
                return findLibraryInAccessibleLibraries(libName);
            }
        }
        return name;
    }

    private String findLibraryInAccessibleLibraries(final String libName) {
        final Map<String, LibrarySpecification> accessibleLibraries = validationContext.getAccessibleLibraries();

        for (final String accessibleLibraryName : accessibleLibraries.keySet()) {
            if (stripLastSlashIfNecessary(accessibleLibraryName).equals(stripLastSlashIfNecessary(libName))) {
                return accessibleLibraryName;
            }
        }
        return libName;
    }

    private static String stripLastSlashIfNecessary(final String string) {
        return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
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
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT)
                    .formatMessageWith(pathOrName);
            final Map<String, Object> additional = isPath ? ImmutableMap.of(AdditionalMarkerAttributes.PATH, pathOrName)
                    : ImmutableMap.of(AdditionalMarkerAttributes.NAME, pathOrName);
            reporter.handleProblem(problem, validationContext.getFile(), pathOrNameToken, additional);
        }
    }

    private void validateArgumentsForRemoteLibraryImport(final String name, final RobotToken nameToken,
            final List<RobotToken> arguments) {
        if ("Remote".equals(name)) {
            if (arguments.isEmpty()) {
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.IMPORT_REMOTE_LIBRARY_WITHOUT_ARGUMENTS)
                        .formatMessageWith(name);
                reporter.handleProblem(problem, validationContext.getFile(), nameToken);
            }
        }
    }
}
