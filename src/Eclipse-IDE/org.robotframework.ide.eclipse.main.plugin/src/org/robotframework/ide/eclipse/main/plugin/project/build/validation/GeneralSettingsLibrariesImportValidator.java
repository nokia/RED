/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class GeneralSettingsLibrariesImportValidator extends GeneralSettingsImportsValidator {

    private final Optional<LibrariesAutoDiscoverer> librariesAutoDiscoverer;
    
    public GeneralSettingsLibrariesImportValidator(final FileValidationContext validationContext,
            final RobotSuiteFile suiteFile, final List<LibraryImport> imports, final ProblemsReportingStrategy reporter,
            final Optional<LibrariesAutoDiscoverer> librariesAutoDiscoverer) {
        super(validationContext, suiteFile, imports, reporter);
        this.librariesAutoDiscoverer = librariesAutoDiscoverer;
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
        final Map<ReferencedLibrary, LibrarySpecification> libs = validationContext.getReferencedLibrarySpecifications();
        for (final ReferencedLibrary refLib : libs.keySet()) {
            final IPath entryPath = refLib.getFilepath();
            final IPath libPath1 = PathsConverter.toAbsoluteFromWorkspaceRelativeIfPossible(entryPath);
            final IPath libPath2 = PathsConverter
                    .toAbsoluteFromWorkspaceRelativeIfPossible(entryPath.addFileExtension("py"));
            if (candidate.equals(libPath1) || candidate.equals(libPath2)) {
                return libs.get(refLib);
            }
        }
        return null;
    }

    @Override
    protected void validateNameImport(final String name, final RobotToken nameToken,
            final List<RobotToken> arguments) throws CoreException {
        final String libName = createLibName(name, arguments);
        final LibrarySpecification specification = validationContext.getLibrarySpecifications(libName);
        validateWithSpec(specification, name, nameToken, arguments, false);
    }

    private String createLibName(final String name, final List<RobotToken> arguments) {
        if ("Remote".equals(name)) {
            // TODO : raise problem when there are no arguments for remote
            return name + " " + (arguments.isEmpty() ? "http://127.0.0.1:8270/RPC2" : arguments.get(0).getText());
        }
        return name;
    }

    private void validateWithSpec(final LibrarySpecification specification, final String pathOrName,
            final RobotToken pathOrNameToken, final List<RobotToken> importArguments, final boolean isPath) {
        if (specification != null) {
            final ArgumentsDescriptor descriptor = specification.getConstructor() == null
                    ? ArgumentsDescriptor.createDescriptor()
                    : specification.getConstructor().createArgumentsDescriptor();
            new KeywordCallArgumentsValidator(validationContext.getFile(), pathOrNameToken, reporter, descriptor,
                    importArguments).validate(new NullProgressMonitor());
        } else {
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT)
                    .formatMessageWith(pathOrName);
            final Map<String, Object> additional = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                    pathOrName, AdditionalMarkerAttributes.IS_PATH, isPath);
            reporter.handleProblem(problem, validationContext.getFile(), pathOrNameToken, additional);

            // FIXME : what it does here?
            if (librariesAutoDiscoverer.isPresent()) {
                librariesAutoDiscoverer.get().addSuiteFileToDiscovering(suiteFile.getFile());
            }
        }
    }

    @Override
    protected void reportMissingImportPath(final String path, final RobotToken pathToken, final IPath importPath) {
        // FIXME : remove this
        super.reportMissingImportPath(path, pathToken, importPath);
        if (librariesAutoDiscoverer.isPresent()) {
            librariesAutoDiscoverer.get().addSuiteFileToDiscovering(suiteFile.getFile());
        }
    }
}