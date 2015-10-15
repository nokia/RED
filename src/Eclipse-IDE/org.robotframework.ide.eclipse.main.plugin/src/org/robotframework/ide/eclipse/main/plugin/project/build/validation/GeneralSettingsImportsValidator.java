/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibraryConstructor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
abstract class GeneralSettingsImportsValidator implements ModelUnitValidator {

    protected final ValidationContext validationContext;

    protected final RobotSuiteFile suiteFile;

    private final List<? extends AImported> imports;

    protected final ProblemsReportingStrategy reporter;


    public GeneralSettingsImportsValidator(final ValidationContext validationContext, final RobotSuiteFile suiteFile,
            final List<? extends AImported> imports, final ProblemsReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.suiteFile = suiteFile;
        this.imports = imports;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final AImported imported : imports) {
            validateImport(imported, monitor);
        }
    }

    private void validateImport(final AImported imported, final IProgressMonitor monitor) throws CoreException {
        final RobotToken pathOrNameToken = imported.getPathOrName();
        if (pathOrNameToken == null) {
            reportMissingImportArgument(imported);
        } else {
            final String pathOrName = pathOrNameToken.getText().toString();

            if (isParameterized(pathOrName)) {
                reportParameterizedImport(pathOrNameToken);
            } else {
                validateSpecifiedImport(imported, pathOrNameToken, monitor);
            }
        }
    }

    private void validateSpecifiedImport(final AImported imported, final RobotToken pathOrNameToken,
            final IProgressMonitor monitor) throws CoreException {
        final String pathOrName = pathOrNameToken.getText().toString();
        if (isPathImport(pathOrName)) {
            validatePathImport(imported, pathOrNameToken, monitor);
        } else {
            validateNameImport(imported, pathOrNameToken, monitor);
        }
    }

    protected abstract boolean isPathImport(String pathOrName);

    @SuppressWarnings("unused")
    protected void validatePathImport(final AImported imported, final RobotToken pathToken,
            final IProgressMonitor monitor) throws CoreException {
        final String path = pathToken.getText().toString();
        final Path resPath = new Path(path);
        final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();

        IPath wsRelativePath = null;
        if (resPath.isAbsolute()) {
            reporter.handleProblem(
                    RobotProblem.causedBy(GeneralSettingsProblem.ABSOLUTE_IMPORT_PATH).formatMessageWith(path),
                    suiteFile.getFile(), pathToken);
            wsRelativePath = resPath.makeRelativeTo(wsRoot.getLocation());
            if (!wsRoot.getLocation().isPrefixOf(resPath)) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE)
                        .formatMessageWith(path), suiteFile.getFile(), pathToken);
                return;
            }
        }
        if (wsRelativePath == null) {
            wsRelativePath = PathsConverter.fromResourceRelativeToWorkspaceRelative(suiteFile.getFile(), resPath);
        }
        final IResource resource = wsRoot.findMember(wsRelativePath);
        if (resource == null || !resource.exists()) {
            reporter.handleProblem(RobotProblem.causedBy(getCauseForInvalidPathImport())
                    .formatMessageWith(path, ": file does not exist"), suiteFile.getFile(), pathToken);
        } else {
            validateExistingResource(resource, pathToken);
        }
    }

    @SuppressWarnings("unused")
    protected void validateExistingResource(final IResource resource, final RobotToken pathToken) {
        // nothing to do; override if needed
    }

    @SuppressWarnings("unused")
    protected void validateNameImport(final AImported imported, final RobotToken pathOrNameToken,
            final IProgressMonitor monitor) throws CoreException {
        // nothing to do; override if needed
    }

    private void reportMissingImportArgument(final AImported imported) {
        final RobotToken declarationToken = imported.getDeclaration();
        reporter.handleProblem(RobotProblem.causedBy(getCauseForMissingImportArguments())
                .formatMessageWith(declarationToken.getText().toString()), suiteFile.getFile(), declarationToken);
    }

    private void reportParameterizedImport(final RobotToken pathOrNameToken) {
        reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.PARAMETERIZED_IMPORT_PATH)
                .formatMessageWith(pathOrNameToken.getText().toString()), suiteFile.getFile(), pathOrNameToken);
    }

    protected abstract IProblemCause getCauseForMissingImportArguments();

    protected abstract GeneralSettingsProblem getCauseForInvalidPathImport();

    private boolean isParameterized(final String pathOrName) {
        return Pattern.compile("[@$&%]\\{[^\\}]+\\}").matcher(pathOrName).find();
    }


    static class LibraryImportValidator extends GeneralSettingsImportsValidator {

        public LibraryImportValidator(final ValidationContext validationContext, final RobotSuiteFile suiteFile,
                final List<LibraryImport> imports, final ProblemsReportingStrategy reporter) {
            super(validationContext, suiteFile, imports, reporter);
        }

        @Override
        protected IProblemCause getCauseForMissingImportArguments() {
            return GeneralSettingsProblem.MISSING_LIBRARY_NAME;
        }

        @Override
        protected GeneralSettingsProblem getCauseForInvalidPathImport() {
            throw new IllegalStateException("This method shouldn't be called for library validators");
        }

        @Override
        protected boolean isPathImport(final String pathOrName) {

            return pathOrName.endsWith("/") || pathOrName.endsWith(".py") || pathOrName.endsWith(".class")
                    || pathOrName.endsWith(".java");
        }

        @Override
        protected void validatePathImport(final AImported imported, final RobotToken path,
                final IProgressMonitor monitor) throws CoreException {
            LibrarySpecification specification = null;
            for (final Entry<ReferencedLibrary, LibrarySpecification> entry : validationContext
                    .getReferencedLibrarySpecifications().entrySet()) {
                for (final IPath p : PathsResolver.resolveToAbsolutePath(suiteFile, path.getText().toString())) {
                    if (p.equals(PathsConverter
                            .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(entry.getKey().getPath())))) {
                        specification = entry.getValue();
                    }
                }
            }
            validateWithSpec(imported, specification, path, monitor);
        }

        @Override
        protected void validateNameImport(final AImported imported, final RobotToken name,
                final IProgressMonitor monitor) throws CoreException {
            validateWithSpec(imported, validationContext.getLibrarySpecificationsAsMap().get(name.getText().toString()),
                    name, monitor);
        }

        private void validateWithSpec(final AImported imported, final LibrarySpecification specification,
                final RobotToken pathOrNameToken, final IProgressMonitor monitor) throws CoreException {
            if (specification != null) {
                final List<RobotToken> arguments = ((LibraryImport) imported).getArguments();
                final LibraryConstructor constructor = specification.getConstructor();
                final Optional<ArgumentsDescriptor> descriptor = constructor == null
                        ? Optional.<ArgumentsDescriptor> absent()
                        : Optional.of(constructor.createArgumentsDescriptor());
                new KeywordCallArgumentsValidator(suiteFile.getFile(), pathOrNameToken, reporter, descriptor, arguments)
                        .validate(monitor);
            } else {
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNKNOWN_LIBRARY)
                        .formatMessageWith(pathOrNameToken.getText().toString());
                reporter.handleProblem(problem, suiteFile.getFile(), pathOrNameToken);
            }
        }
    }

    static class VariablesImportValidator extends GeneralSettingsImportsValidator {

        public VariablesImportValidator(final ValidationContext validationContext, final RobotSuiteFile suiteFile,
                final List<VariablesImport> imports, final ProblemsReportingStrategy reporter) {
            super(validationContext, suiteFile, imports, reporter);
        }

        @Override
        protected IProblemCause getCauseForMissingImportArguments() {
            return GeneralSettingsProblem.MISSING_VARIABLES_NAME;
        }

        @Override
        protected GeneralSettingsProblem getCauseForInvalidPathImport() {
            return GeneralSettingsProblem.INVALID_VARIABLES_IMPORT;
        }

        @Override
        protected boolean isPathImport(final String pathOrName) {
            return true;
        }

        @Override
        protected void validateExistingResource(final IResource resource, final RobotToken pathToken) {
            final String path = pathToken.getText().toString();
            if (resource.getType() != IResource.FILE) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_VARIABLES_IMPORT)
                        .formatMessageWith(path, ": given location does not point to a file"), suiteFile.getFile(),
                        pathToken);
            } else {
                boolean isDefinedInProject = false;
                for (final String p : suiteFile.getProject().getVariableFilePaths()) {
                    final Path projectDefinedFilePath = new Path(p);
                    if (projectDefinedFilePath.equals(resource.getFullPath())
                            || projectDefinedFilePath.equals(resource.getLocation())) {
                        isDefinedInProject = true;
                    }
                }
                if (!isDefinedInProject) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(GeneralSettingsProblem.INVALID_VARIABLES_IMPORT)
                                    .formatMessageWith(path, ": Variable files should be defined in red.xml file"),
                            suiteFile.getFile(), pathToken);
                }
            }
        }
    }

    static class ResourcesImportValidator extends GeneralSettingsImportsValidator {

        public ResourcesImportValidator(final ValidationContext validationContext, final RobotSuiteFile suiteFile,
                final List<ResourceImport> imports, final ProblemsReportingStrategy reporter) {
            super(validationContext, suiteFile, imports, reporter);
        }

        @Override
        protected IProblemCause getCauseForMissingImportArguments() {
            return GeneralSettingsProblem.MISSING_RESOURCE_NAME;
        }

        @Override
        protected GeneralSettingsProblem getCauseForInvalidPathImport() {
            return GeneralSettingsProblem.INVALID_RESOURCE_IMPORT;
        }

        @Override
        protected boolean isPathImport(final String pathOrName) {
            return true;
        }

        @Override
        protected void validateExistingResource(final IResource resource, final RobotToken pathToken) {
            final String path = pathToken.getText().toString();
            if (resource.getType() != IResource.FILE) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                        .formatMessageWith(path, ": given location does not point to a file"), suiteFile.getFile(),
                        pathToken);
            } else {
                try {
                    final String id = ((IFile) resource).getContentDescription().getContentType().getId();
                    if (!RobotSuiteFileDescriber.RESOURCE_FILE_CONTENT_ID.equals(id)) {
                        reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                                .formatMessageWith(path, ": given file is not a Resource file"), suiteFile.getFile(),
                                pathToken);
                    }
                } catch (final CoreException e) {
                    // this shouldn't happen
                }
            }
        }
    }
}
