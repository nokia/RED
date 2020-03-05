/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.libraries.LibraryConstructor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.importer.LibraryImportResolver;
import org.rf.ide.core.testdata.importer.LibraryImportResolver.ImportedLibrary;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.RobotTokenAsArgExtractor;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.annotations.VisibleForTesting;
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
        return new Path(pathOrName).isAbsolute() || pathOrName.endsWith("/") || pathOrName.endsWith(".py")
                || pathOrName.endsWith(".class") || pathOrName.endsWith(".java");
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

    @VisibleForTesting
    protected LibrarySpecification findSpecification(final IPath candidate) {
        return validationContext.getSpecifications()
                .values()
                .stream()
                .filter(spec -> spec != null)
                .filter(spec -> spec.getDescriptor().isReferencedLibrary())
                .filter(spec -> RobotSetting.specPathsMatches(candidate, new Path(spec.getDescriptor().getPath())))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void validateNameImport(final AImported imported, final String name, final RobotToken nameToken,
            final List<RobotToken> arguments) {

        if (name.contains(" ")) {
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.LIBRARY_NAME_WITH_SPACES)
                    .formatMessageWith(name, name.replaceAll("\\s", ""));
            final Map<String, Object> additional = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
            reporter.handleProblem(problem, validationContext.getFile(), nameToken, additional);

        } else {
            final Map<String, String> variableMappings = suiteFile.getRobotProject()
                    .getRobotProjectHolder()
                    .getVariableMappings();
            final LibraryImportResolver importResolver = new LibraryImportResolver(validationContext.getVersion(),
                    variableMappings, (spec, path) -> RobotSetting.isImportedByPath(suiteFile, spec, path),
                    validationContext.getSpecifications());
            final LibrarySpecification spec = importResolver.getImportedLibrary((LibraryImport) imported)
                    .map(ImportedLibrary::getSpecification)
                    .orElse(null);

            validateWithSpec(spec, name, nameToken, arguments, false);
        }
    }

    private void validateWithSpec(final LibrarySpecification specification, final String pathOrName,
            final RobotToken pathOrNameToken, final List<RobotToken> arguments, final boolean isPath) {
        if ("Remote".equals(pathOrName)) {
            validateRemoteLibraryArguments(specification, pathOrNameToken, arguments);

        } else if (specification != null) {
            validateArguments(specification.createArgumentsDescriptor(), arguments, pathOrNameToken);

        } else {
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT)
                    .formatMessageWith(pathOrName);
            final Map<String, Object> additional = isPath ? ImmutableMap.of(AdditionalMarkerAttributes.PATH, pathOrName)
                    : ImmutableMap.of(AdditionalMarkerAttributes.NAME, pathOrName);
            reporter.handleProblem(problem, validationContext.getFile(), pathOrNameToken, additional);
        }
    }

    private void validateRemoteLibraryArguments(final LibrarySpecification specification, final RobotToken nameToken,
            final List<RobotToken> arguments) {
        final ArgumentsDescriptor descriptor = Optional.ofNullable(specification)
                .map(LibrarySpecification::createArgumentsDescriptor)
                .orElseGet(LibraryConstructor.createDefaultForStandardRemote()::createArgumentsDescriptor);
        validateArguments(descriptor, arguments, nameToken);

        if (!CallArgumentsBinder.canBind(suiteFile.getRobotProject().getRobotParserComplianceVersion(), descriptor)) {
            return;
        }

        final Map<String, String> variableMappings = suiteFile.getRobotProject()
                .getRobotProjectHolder()
                .getVariableMappings();
        final CallArgumentsBinder<RobotToken> binder = new CallArgumentsBinder<>(new RobotTokenAsArgExtractor(),
                descriptor);
        binder.bind(arguments);

        if (specification == null) {
            final Argument uriArgument = descriptor.get(0);
            final Optional<String> uri = binder.getLastValueBindedTo(uriArgument)
                    .map(arg -> RobotExpressions.resolve(variableMappings, arg));
            if (uri.isPresent()) {
                final String uriValue = uri.get();
                final RobotToken uriToken = binder.getLastBindedTo(uriArgument).get();
                if (RobotExpressions.isParameterized(uriValue)) {
                    reportUnresolvedParameterizedImport(uriValue, uriToken);
                } else {
                    validateRemoteLocation(uriValue, uriToken);
                }
            } else if (binder.hasBindings()) {
                validateRemoteLocation(RemoteLocation.DEFAULT_ADDRESS, nameToken);
            }
        }

        final Argument timeoutArgument = descriptor.get(1);
        final Optional<String> timeout = binder.getLastValueBindedTo(timeoutArgument)
                .map(arg -> RobotExpressions.resolve(variableMappings, arg));
        if (timeout.isPresent()) {
            final String timeoutValue = timeout.get();
            final RobotToken timeoutToken = binder.getLastBindedTo(timeoutArgument).get();
            if (RobotExpressions.isParameterized(timeoutValue)) {
                reportUnresolvedParameterizedImport(timeoutValue, timeoutToken);
            } else if (!RobotTimeFormat.isValidRobotTimeArgument(timeoutValue)) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                        .formatMessageWith(timeoutValue);
                reporter.handleProblem(problem, validationContext.getFile(), timeoutToken);
            }
        }
    }

    private void validateArguments(final ArgumentsDescriptor descriptor, final List<RobotToken> arguments,
            final RobotToken markerToken) {
        new KeywordCallArgumentsValidator(validationContext, markerToken, reporter, descriptor, arguments).validate();
    }

    private void validateRemoteLocation(final String uri, final RobotToken markerToken) {
        final RemoteLocation location;
        try {
            location = RemoteLocation.create(uri);
        } catch (final IllegalArgumentException e) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(e.getCause().getMessage());
            reporter.handleProblem(problem, validationContext.getFile(), markerToken);
            return;
        }

        if (location.getUri().isAbsolute() && !"http".equalsIgnoreCase(location.getUri().getScheme())
                && !"https".equalsIgnoreCase(location.getUri().getScheme())) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(location.getUri().getScheme());
            reporter.handleProblem(problem, validationContext.getFile(), markerToken);
            return;
        }

        final boolean isInRemoteLocations = validationContext.getProjectConfiguration()
                .getRemoteLocations()
                .stream()
                .anyMatch(locationFromSpec -> RemoteLocation.unify(location.getUri())
                        .equals(RemoteLocation.unify(locationFromSpec.getUri())));
        if (isInRemoteLocations) {
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNREACHABLE_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(location.getUri());
            reporter.handleProblem(problem, validationContext.getFile(), markerToken);
        } else {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(location.getRemoteName());
            final Map<String, Object> additional = ImmutableMap.of(AdditionalMarkerAttributes.PATH,
                    location.getUri().toString());
            reporter.handleProblem(problem, validationContext.getFile(), markerToken, additional);
        }
    }
}
