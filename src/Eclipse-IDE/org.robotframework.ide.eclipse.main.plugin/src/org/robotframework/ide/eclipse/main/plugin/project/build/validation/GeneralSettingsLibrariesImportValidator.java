/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
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
            final RobotToken pathOrNameToken, final List<RobotToken> importArguments, final boolean isPath) {
        if (specification != null) {
            final ArgumentsDescriptor descriptor = specification.getConstructor() == null
                    ? ArgumentsDescriptor.createDescriptor()
                    : specification.getConstructor().createArgumentsDescriptor();
            new KeywordCallArgumentsValidator(validationContext, pathOrNameToken, reporter, descriptor, importArguments)
                    .validate(new NullProgressMonitor());

        } else if (pathOrName.equalsIgnoreCase("remote")) {
            reportProblemOnRemoteLibraryArguments(pathOrNameToken, importArguments);

        } else {
            final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.NON_EXISTING_LIBRARY_IMPORT)
                    .formatMessageWith(pathOrName);
            final Map<String, Object> additional = isPath ? ImmutableMap.of(AdditionalMarkerAttributes.PATH, pathOrName)
                    : ImmutableMap.of(AdditionalMarkerAttributes.NAME, pathOrName);
            reporter.handleProblem(problem, validationContext.getFile(), pathOrNameToken, additional);
        }
    }

    private void reportProblemOnRemoteLibraryArguments(final RobotToken nameToken, final List<RobotToken> arguments) {
        final RobotVersion parserVersion = suiteFile.getRobotProject().getRobotParserComplianceVersion();
        final Map<String, String> variableMappings = suiteFile.getRobotProject().getRobotProjectHolder()
                .getVariableMappings();
        final ArgumentsDescriptor remoteLibConsDescriptor = ArgumentsDescriptor
                .createDescriptor("uri=" + RemoteLocation.DEFAULT_ADDRESS, "timeout=30");
        final CallArgumentsBinder<RobotToken> importBinder = new CallArgumentsBinder<>(new RobotTokenAsArgExtractor(),
                remoteLibConsDescriptor);

        if (CallArgumentsBinder.canBind(parserVersion, remoteLibConsDescriptor)) {
            importBinder.bind(arguments);
        }

        final Optional<RobotToken> addressToken = importBinder.getLastBindedTo(remoteLibConsDescriptor.get(0));
        final Optional<String> address = importBinder.getLastValueBindedTo(remoteLibConsDescriptor.get(0))
                .map(RemoteLocation::addProtocolIfNecessary)
                .map(arg -> RobotExpressions.resolve(variableMappings, arg));

        final Optional<RobotToken> timeoutToken = importBinder.getLastBindedTo(remoteLibConsDescriptor.get(1));
        final Optional<String> timeout = importBinder.getLastValueBindedTo(remoteLibConsDescriptor.get(1))
                .map(arg -> RobotExpressions.resolve(variableMappings, arg));

        if (address.isPresent() && RobotExpressions.isParameterized(address.get())) {
            reportUnresolvedParameterizedImport(address.get(), addressToken.get());

        } else if (address.isPresent()) {
            reportProblemOnRemoteLocation(addressToken.get(), address.get());

        } else {
            reportProblemOnRemoteLocation(nameToken, RemoteLocation.DEFAULT_ADDRESS);
            new KeywordCallArgumentsValidator(validationContext, nameToken, reporter, remoteLibConsDescriptor,
                    arguments).validate(new NullProgressMonitor());
        }

        if (timeout.isPresent() && !RobotTimeFormat.isValidRobotTimeArgument(timeout.get())) {
            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                    .formatMessageWith(timeout.get());
            reporter.handleProblem(problem, validationContext.getFile(), timeoutToken.get());
        }
    }

    private void reportProblemOnRemoteLocation(final RobotToken markerToken, final String address) {
        try {
            final String uriScheme = URI.create(address.toLowerCase()).getScheme();
            if (uriScheme != null && !uriScheme.equals("http") && !uriScheme.equals("https")) {
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT)
                        .formatMessageWith(uriScheme);
                reporter.handleProblem(problem, validationContext.getFile(), markerToken);
                return;
            }
        } catch (final IllegalArgumentException e) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.INVALID_URI_IN_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(e.getCause().getMessage());
            reporter.handleProblem(problem, validationContext.getFile(), markerToken);
            return;
        }

        if (isInRemoteLocations(address)) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.NON_REACHABLE_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(address);
            reporter.handleProblem(problem, validationContext.getFile(), markerToken);
        } else {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.NON_EXISTING_REMOTE_LIBRARY_IMPORT)
                    .formatMessageWith(address);
            final Map<String, Object> additional = ImmutableMap.of(AdditionalMarkerAttributes.PATH, address);
            reporter.handleProblem(problem, validationContext.getFile(), markerToken, additional);
        }
    }

    private boolean isInRemoteLocations(final String address) {
        final RobotProjectConfig robotProjectConfig = validationContext.getProjectConfiguration();
        final List<RemoteLocation> remoteLocations = robotProjectConfig.getRemoteLocations();

        return remoteLocations.stream().anyMatch(location -> RemoteLocation.areEqual(address, location.getUri()));
    }
}
