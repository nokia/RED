/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.testdata.ValuesEscapes;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths.MarkedPath;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths.PathRelativityPoint;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

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

    protected abstract IProblemCause getCauseForNonExistingImport();

    protected abstract IProblemCause getCauseForMissingImportArguments();

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final AImported imported : imports) {
            validateImport(imported);
        }
    }

    private void validateImport(final AImported imported) throws CoreException {
        final RobotToken pathOrNameToken = imported.getPathOrName();
        if (pathOrNameToken == null) {
            reportMissingImportArgument(imported.getDeclaration());
        } else {
            final String pathOrName = ValuesEscapes.unescapeSpaces(pathOrNameToken.getText());
            if (RobotExpressions.isParameterized(pathOrName)) {
                final String resolved = suiteFile.getProject().resolve(pathOrName);
                if (RobotExpressions.isParameterized(resolved)) {
                    // still parameterized after resolving
                    reportUnresolvedParameterizedImport(pathOrNameToken);
                } else {
                    validateSpecifiedImport(imported, resolved, pathOrNameToken, true);
                }
            } else {
                validateSpecifiedImport(imported, pathOrName, pathOrNameToken, false);
            }
        }
    }

    private void reportMissingImportArgument(final RobotToken declarationToken) {
        reporter.handleProblem(RobotProblem.causedBy(getCauseForMissingImportArguments())
                .formatMessageWith(declarationToken.getText()), validationContext.getFile(), declarationToken);
    }

    private void reportUnresolvedParameterizedImport(final RobotToken pathOrNameToken) {
        final String path = pathOrNameToken.getText();
        final Map<String, Object> additional = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME, path);
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_PARAMETERIZED).formatMessageWith(path),
                validationContext.getFile(), pathOrNameToken, additional);
    }

    private void validateSpecifiedImport(final AImported imported, final String pathOrName,
            final RobotToken pathOrNameToken, final boolean isParametrized)
                    throws CoreException {
        if (isPathImport(pathOrName)) {
            validatePathImport(pathOrName, pathOrNameToken, isParametrized, imported.getArguments());
        } else {
            validateNameImport(pathOrName, pathOrNameToken, imported.getArguments());
        }
    }

    protected abstract boolean isPathImport(String pathOrName);

    protected void validatePathImport(final String path, final RobotToken pathToken, final boolean isParametrized,
            final List<RobotToken> arguments) throws CoreException {

        if (PathsResolver.hasNotEscapedWindowsPathSeparator(pathToken.getText())) {
            reportWindowsPathImport(pathToken);
            return;
        }

        final IPath importPath = new Path(path);
        if (!isParametrized && importPath.isAbsolute()) {
            reportAbsolutePathImport(pathToken, path);
        }
        
        final Optional<MarkedPath> absoluteMarkedPath = calculateAbsolutePath(importPath);
        if (!absoluteMarkedPath.isPresent()) {
            reportMissingImportPath(path, pathToken, importPath);
            return;

        }

        if (isRelativeToModuleSearchPath(absoluteMarkedPath)) {
            reportModuleSearchPathRelativeness(path, pathToken, importPath, absoluteMarkedPath);
        }

        final IPath absoluteImportPath = absoluteMarkedPath.get().getPath();
        final IWorkspaceRoot wsRoot = validationContext.getFile().getWorkspace().getRoot();

        if (destinationIsInWorkspace(wsRoot, absoluteImportPath)) {

            final IResource resource = getResourceFor(wsRoot, absoluteImportPath);
            if (resource == null) {
                reportNonExistingResource(path, pathToken, null);
            } else if (!resource.exists()) {
                reportNonExistingResource(path, pathToken, resource.getFullPath().toPortableString());
            } else {
                validateResource(resource, path, pathToken, arguments);
            }

        } else {
            reportFileOutsideOfWorkspace(path, pathToken);

            final File importAsFile = absoluteImportPath.toFile();
            if (!importAsFile.exists()) {
                reportNonExistingResource(path, pathToken, null);
            } else {
                validateFile(importAsFile, path, pathToken, arguments);
            }
        }
    }

    private void reportFileOutsideOfWorkspace(final String path, final RobotToken pathToken) {
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_OUTSIDE_WORKSPACE).formatMessageWith(path),
                validationContext.getFile(), pathToken);
    }

    private void reportNonExistingResource(final String path, final RobotToken pathToken,
            final String workspaceRelativePath) {
        final Map<String, Object> attributes = workspaceRelativePath != null
                ? ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.PATH, workspaceRelativePath)
                : ImmutableMap.<String, Object> of();
        reporter.handleProblem(RobotProblem.causedBy(getCauseForNonExistingImport()).formatMessageWith(path),
                validationContext.getFile(), pathToken, attributes);
    }

    private IResource getResourceFor(final IWorkspaceRoot root, final IPath absoluteImportPath) {
        if (root.getLocation().isPrefixOf(absoluteImportPath)) {
            final IPath wsRelativePath = absoluteImportPath.makeRelativeTo(root.getLocation());
            return root.findMember(wsRelativePath);
        } else {
            return root.findFilesForLocationURI(absoluteImportPath.toFile().toURI())[0];
        }
    }

    private boolean isRelativeToModuleSearchPath(final Optional<MarkedPath> absoluteMarkedPath) {
        return EnumSet.of(PathRelativityPoint.MODULE_SEARCH_PATH, PathRelativityPoint.PROJECT_CONFIG_PATH)
                .contains(absoluteMarkedPath.get().getRelativity());
    }

    private void reportModuleSearchPathRelativeness(final String path, final RobotToken pathToken,
            final IPath importPath, final Optional<MarkedPath> absoluteMarkedPath) {
        final Map<String, Object> attributes = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.PATH,
                importPath.toPortableString());
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH)
                        .formatMessageWith(path, absoluteMarkedPath.get().getPath().toString()),
                validationContext.getFile(), pathToken, attributes);
    }

    private Optional<MarkedPath> calculateAbsolutePath(final IPath importPath) {
        final Optional<MarkedPath> absoluteMarkedPath;
        if (importPath.isAbsolute()) {
            absoluteMarkedPath = Optional.of(new MarkedPath(importPath, PathRelativityPoint.NONE));
        } else {
            absoluteMarkedPath = new ImportSearchPaths(suiteFile.getProject())
                    .getAbsolutePath(suiteFile, importPath);
        }
        return absoluteMarkedPath;
    }

    protected void reportMissingImportPath(final String path, final RobotToken pathToken, final IPath importPath) {
        final Map<String, Object> attributes = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.PATH,
                importPath.toPortableString());
        reporter.handleProblem(RobotProblem.causedBy(getCauseForNonExistingImport()).formatMessageWith(path),
                validationContext.getFile(), pathToken, attributes);
    }

    private void reportAbsolutePathImport(final RobotToken pathToken, final String path) {
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.IMPORT_PATH_ABSOLUTE).formatMessageWith(path),
                validationContext.getFile(), pathToken);
    }

    private void reportWindowsPathImport(final RobotToken pathToken) {
        final RobotProblem problem = RobotProblem
                .causedBy(GeneralSettingsProblem.IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS);
        reporter.handleProblem(problem, validationContext.getFile(), pathToken);
    }

    private boolean destinationIsInWorkspace(final IWorkspaceRoot root, final IPath destinationAbsolutePath) {
        return root.getLocation().isPrefixOf(destinationAbsolutePath)
                || root.findFilesForLocationURI(destinationAbsolutePath.toFile().toURI()).length > 0;
    }

    @SuppressWarnings("unused")
    protected void validateNameImport(final String name, final RobotToken nameToken, final List<RobotToken> arguments)
            throws CoreException {
        // nothing to do; override if needed
    }

    @SuppressWarnings("unused")
    protected void validateResource(final IResource resource, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {
        // nothing to do; override if needed
    }

    @SuppressWarnings("unused")
    protected void validateFile(final File file, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {
        // nothing to do; override if needed
    }
}
