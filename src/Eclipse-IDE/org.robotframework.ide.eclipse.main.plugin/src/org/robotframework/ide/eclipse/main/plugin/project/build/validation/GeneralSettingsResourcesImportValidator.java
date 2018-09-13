/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.RedURI;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

class GeneralSettingsResourcesImportValidator extends GeneralSettingsImportsValidator {

    final RobotSuiteFile suiteFile;

    GeneralSettingsResourcesImportValidator(final FileValidationContext validationContext,
            final RobotSuiteFile suiteFile, final List<ResourceImport> imports,
            final ValidationReportingStrategy reporter) {
        super(validationContext, suiteFile, imports, reporter);
        this.suiteFile = suiteFile;
    }

    @Override
    protected IProblemCause getCauseForMissingImportArguments() {
        return GeneralSettingsProblem.MISSING_RESOURCE_NAME;
    }

    @Override
    protected IProblemCause getCauseForNonExistingImport() {
        return GeneralSettingsProblem.NON_EXISTING_RESOURCE_IMPORT;
    }

    @Override
    protected boolean isPathImport(final String pathOrName) {
        return true;
    }

    @Override
    protected void validateResource(final IResource resource, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {

        if (resource.getType() != IResource.FILE) {
            reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                    .formatMessageWith(path, ": given location does not point to a file"), validationContext.getFile(),
                    pathToken);
        } else if (!ASuiteFileDescriber.isResourceFile((IFile) resource)) {
            if ("html".equalsIgnoreCase(resource.getFileExtension())) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.HTML_RESOURCE_IMPORT),
                        validationContext.getFile(), pathToken);
            } else {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                        .formatMessageWith(path, ": given file is not a Resource file"), validationContext.getFile(),
                        pathToken);
            }
        }
    }

    @Override
    @VisibleForTesting
    protected void validateFile(final File file, final String path, final RobotToken pathToken,
            final List<RobotToken> arguments) {

        if (!file.isFile()) {
            reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                    .formatMessageWith(path, ": given location does not point to a file"), validationContext.getFile(),
                    pathToken);
        } else {
            final String fileExtension = new Path(file.getAbsolutePath()).getFileExtension();
            final String extension = fileExtension == null ? null : fileExtension.toLowerCase();
            if ("html".equals(extension)) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.HTML_RESOURCE_IMPORT),
                        validationContext.getFile(), pathToken);
            } else if (!newHashSet("txt", "tsv", "robot").contains(extension)) {
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.INVALID_RESOURCE_IMPORT)
                        .formatMessageWith(path, ": given file is not a Resource file"), validationContext.getFile(),
                        pathToken);
            } else {
                final String parentContainerName = new File(file.getParent()).getName();
                final Optional<MarkedUri> absoluteMarkedPath = calculateAbsoluteUri(path);
                final String absoluteImportString = absoluteMarkedPath.isPresent() ? RedURI.reverseUriSpecialCharsEscapes(
                        new File(absoluteMarkedPath.get().getPath()).getAbsolutePath().replaceAll("\\\\", "/")) : "";
                // When a relative path is too long to be properly resolved we get an absolute
                // path with '..' (e.g. 'D:/../res.robot'). If such file exists the third
                // condition avoids the problems caused by incorrect relative constructions.
                if (parentContainerName.isEmpty() || absoluteImportString.isEmpty() || absoluteImportString.contains("..")) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(GeneralSettingsProblem.NON_WORKSPACE_UNLINKABLE_RESOURCE_IMPORT),
                            validationContext.getFile(), pathToken);
                } else {
                    final Map<String, Object> additionalAttributes = ImmutableMap.of(AdditionalMarkerAttributes.PATH,
                            absoluteImportString, AdditionalMarkerAttributes.VALUE, pathToken.getText());
                    reporter.handleProblem(
                            RobotProblem.causedBy(GeneralSettingsProblem.NON_WORKSPACE_LINKABLE_RESOURCE_IMPORT),
                            validationContext.getFile(), pathToken, additionalAttributes);
                }
            }
        }
    }
}
