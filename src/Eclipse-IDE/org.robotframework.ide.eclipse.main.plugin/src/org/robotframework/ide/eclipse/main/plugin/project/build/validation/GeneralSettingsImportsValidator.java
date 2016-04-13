/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * @author Michal Anglart
 */
abstract class GeneralSettingsImportsValidator implements ModelUnitValidator {

    protected final FileValidationContext validationContext;

    protected final RobotSuiteFile suiteFile;

    private final List<? extends AImported> imports;

    protected final ProblemsReportingStrategy reporter;

    public GeneralSettingsImportsValidator(final FileValidationContext validationContext,
            final RobotSuiteFile suiteFile, final List<? extends AImported> imports,
            final ProblemsReportingStrategy reporter) {
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
            String pathOrName = pathOrNameToken.getText();
            pathOrName = pathOrName.replaceAll(" [\\\\] ", "  ");
            if (RobotExpressions.isParameterized(pathOrName)) {
                final String resolved = suiteFile.getProject().resolve(pathOrName);
                if (RobotExpressions.isParameterized(resolved)) {
                    reportParameterizedImport(pathOrNameToken);
                } else {
                    validateSpecifiedImport(imported, resolved, pathOrNameToken, true, monitor);
                }
            } else {
                validateSpecifiedImport(imported, pathOrName, pathOrNameToken, false, monitor);
            }
        }
    }

    private void validateSpecifiedImport(final AImported imported, final String pathOrName,
            final RobotToken pathOrNameToken, final boolean isParametrized, final IProgressMonitor monitor)
                    throws CoreException {
        if (isPathImport(pathOrName)) {
            validatePathImport(imported, pathOrName, pathOrNameToken, isParametrized, monitor);
        } else {
            validateNameImport(imported, pathOrName, pathOrNameToken, monitor);
        }
    }

    protected abstract boolean isPathImport(String pathOrName);

    @SuppressWarnings("unused")
    protected void validatePathImport(final AImported imported, final String path, final RobotToken pathToken,
            final boolean isParametrized, final IProgressMonitor monitor) throws CoreException {
        final Path resPath = new Path(path);
        final IWorkspaceRoot wsRoot = validationContext.getFile().getWorkspace().getRoot();

        IPath wsRelativePath = null;
        if (resPath.isAbsolute()) {
            if (!isParametrized) {
                reporter.handleProblem(
                        RobotProblem.causedBy(GeneralSettingsProblem.ABSOLUTE_IMPORT_PATH).formatMessageWith(path),
                        validationContext.getFile(), pathToken);
            }
            wsRelativePath = resPath.makeRelativeTo(wsRoot.getLocation());
            if (!wsRoot.getLocation().isPrefixOf(resPath)) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE)
                        .formatMessageWith(path), validationContext.getFile(), pathToken);
                return;
            }
        }
        if (wsRelativePath == null) {
            wsRelativePath = PathsConverter.fromResourceRelativeToWorkspaceRelative(validationContext.getFile(),
                    resPath);
        }
        final IResource resource = wsRoot.findMember(wsRelativePath);
        if (resource == null || !resource.exists()) {
            final Map<String, Object> attributes = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.PATH,
                    wsRelativePath.toPortableString());
            reporter.handleProblem(
                    RobotProblem.causedBy(getCauseForNonExistingResourceImport()).formatMessageWith(path),
                    validationContext.getFile(), pathToken, attributes);
        } else {
            validateExistingResource(resource, path, pathToken);
        }
    }

    @SuppressWarnings("unused")
    protected void validateExistingResource(final IResource resource, final String path, final RobotToken pathToken) {
        // nothing to do; override if needed
    }

    @SuppressWarnings("unused")
    protected void validateNameImport(final AImported imported, final String pathOrName,
            final RobotToken pathOrNameToken, final IProgressMonitor monitor) throws CoreException {
        // nothing to do; override if needed
    }

    private void reportMissingImportArgument(final AImported imported) {
        final RobotToken declarationToken = imported.getDeclaration();
        reporter.handleProblem(RobotProblem.causedBy(getCauseForMissingImportArguments())
                .formatMessageWith(declarationToken.getText()), validationContext.getFile(), declarationToken);
    }

    private void reportParameterizedImport(final RobotToken pathOrNameToken) {
        final String path = pathOrNameToken.getText();
        final Map<String, Object> additional = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME, path);
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.PARAMETERIZED_IMPORT_PATH).formatMessageWith(path),
                validationContext.getFile(), pathOrNameToken, additional);
    }

    protected abstract IProblemCause getCauseForMissingImportArguments();

    protected abstract GeneralSettingsProblem getCauseForNonExistingResourceImport();

    static class LibraryImportValidator extends GeneralSettingsImportsValidator {

        private Optional<LibrariesAutoDiscoverer> librariesAutoDiscoverer;
        
        public LibraryImportValidator(final FileValidationContext validationContext, final RobotSuiteFile suiteFile,
                final List<LibraryImport> imports, final ProblemsReportingStrategy reporter,
                final Optional<LibrariesAutoDiscoverer> librariesAutoDiscoverer) {
            super(validationContext, suiteFile, imports, reporter);
            this.librariesAutoDiscoverer = librariesAutoDiscoverer;
        }

        @Override
        protected IProblemCause getCauseForMissingImportArguments() {
            return GeneralSettingsProblem.MISSING_LIBRARY_NAME;
        }

        @Override
        protected GeneralSettingsProblem getCauseForNonExistingResourceImport() {
            throw new IllegalStateException("This method shouldn't be called for library validators");
        }

        @Override
        protected boolean isPathImport(final String pathOrName) {

            return pathOrName.endsWith("/") || pathOrName.endsWith(".py") || pathOrName.endsWith(".class")
                    || pathOrName.endsWith(".java");
        }

        @Override
        protected void validatePathImport(final AImported imported, final String path, final RobotToken pathToken,
                final boolean isParametrized, final IProgressMonitor monitor) throws CoreException {
            LibrarySpecification specification = null;
            for (final Entry<ReferencedLibrary, LibrarySpecification> entry : validationContext
                    .getReferencedLibrarySpecifications().entrySet()) {
                final IPath entryPath = entry.getKey().getFilepath();
                final IPath libPath1 = PathsConverter.toAbsoluteFromWorkspaceRelativeIfPossible(entryPath);
                final IPath libPath2 = PathsConverter
                        .toAbsoluteFromWorkspaceRelativeIfPossible(entryPath.addFileExtension("py"));
                for (final IPath candidate : PathsResolver.resolveToAbsolutePossiblePaths(suiteFile, path)) {
                    if (candidate.equals(libPath1) || candidate.equals(libPath2)) {
                        specification = entry.getValue();
                    }
                }
            }
            validateWithSpec(imported, specification, path, pathToken, monitor, true);
        }

        @Override
        protected void validateNameImport(final AImported imported, final String name, final RobotToken nameToken,
                final IProgressMonitor monitor) throws CoreException {
            final String libName = createLibName(name, ((LibraryImport) imported).getArguments());
            validateWithSpec(imported, validationContext.getLibrarySpecifications(libName), name, nameToken, monitor,
                    false);
        }

        private String createLibName(final String name, final List<RobotToken> arguments) {
            if ("Remote".equals(name)) {
                // TODO : raise problem when there are no arguments for remote
                return name + " " + (arguments.isEmpty() ? "http://127.0.0.1:8270/RPC2" : arguments.get(0).getText());
            }
            return name;
        }

        private void validateWithSpec(final AImported imported, final LibrarySpecification specification,
                final String pathOrName, final RobotToken pathOrNameToken, final IProgressMonitor monitor,
                final boolean isPath) throws CoreException {
            if (specification != null) {
                final List<RobotToken> arguments = ((LibraryImport) imported).getArguments();
                final ArgumentsDescriptor descriptor = specification.getConstructor() == null
                        ? ArgumentsDescriptor.createDescriptor()
                        : specification.getConstructor().createArgumentsDescriptor();
                new KeywordCallArgumentsValidator(validationContext.getFile(), pathOrNameToken, reporter, descriptor,
                        arguments).validate(monitor);
            } else {
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNKNOWN_LIBRARY)
                        .formatMessageWith(pathOrName);
                final Map<String, Object> additional = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                        pathOrName, AdditionalMarkerAttributes.IS_PATH, isPath);
                reporter.handleProblem(problem, validationContext.getFile(), pathOrNameToken, additional);
                
                if(librariesAutoDiscoverer.isPresent()) {
                    librariesAutoDiscoverer.get().addSuiteFileToDiscovering(suiteFile.getFile());
                }
            }
        }
    }

    static class VariablesImportValidator extends GeneralSettingsImportsValidator {

        public VariablesImportValidator(final FileValidationContext validationContext, final RobotSuiteFile suiteFile,
                final List<VariablesImport> imports, final ProblemsReportingStrategy reporter) {
            super(validationContext, suiteFile, imports, reporter);
        }

        @Override
        protected IProblemCause getCauseForMissingImportArguments() {
            return GeneralSettingsProblem.MISSING_VARIABLES_NAME;
        }

        @Override
        protected GeneralSettingsProblem getCauseForNonExistingResourceImport() {
            return GeneralSettingsProblem.NON_EXISTING_VARIABLES_IMPORT;
        }

        @Override
        protected boolean isPathImport(final String pathOrName) {
            return true;
        }

        @Override
        protected void validateExistingResource(final IResource resource, final String path,
                final RobotToken pathToken) {
            if (resource.getType() != IResource.FILE && resource.getType() != IResource.FOLDER) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_VARIABLES_IMPORT)
                        .formatMessageWith(path, ": given location does not point to a file"),
                        validationContext.getFile(), pathToken);
            }
        }
    }

    static class ResourcesImportValidator extends GeneralSettingsImportsValidator {

        public ResourcesImportValidator(final FileValidationContext validationContext, final RobotSuiteFile suiteFile,
                final List<ResourceImport> imports, final ProblemsReportingStrategy reporter) {
            super(validationContext, suiteFile, imports, reporter);
        }

        @Override
        protected IProblemCause getCauseForMissingImportArguments() {
            return GeneralSettingsProblem.MISSING_RESOURCE_NAME;
        }

        @Override
        protected GeneralSettingsProblem getCauseForNonExistingResourceImport() {
            return GeneralSettingsProblem.NON_EXISTING_RESOURCE_IMPORT;
        }

        @Override
        protected boolean isPathImport(final String pathOrName) {
            return true;
        }

        @Override
        protected void validateExistingResource(final IResource resource, final String path,
                final RobotToken pathToken) {
            if (resource.getType() != IResource.FILE) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                        .formatMessageWith(path, ": given location does not point to a file"),
                        validationContext.getFile(), pathToken);
            } else if (!ASuiteFileDescriber.isResourceFile((IFile) resource)) {
                if (resource.getFileExtension().equalsIgnoreCase("html")) {
                    reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.HTML_RESOURCE_IMPORT),
                            validationContext.getFile(), pathToken);
                } else {
                    reporter.handleProblem(
                            RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                                    .formatMessageWith(path, ": given file is not a Resource file"),
                            validationContext.getFile(), pathToken);
                }
            }
        }
    }
}
