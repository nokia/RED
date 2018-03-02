/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

class GeneralSettingsResourcesImportValidator extends GeneralSettingsImportsValidator {

    GeneralSettingsResourcesImportValidator(final FileValidationContext validationContext,
            final RobotSuiteFile suiteFile, final List<ResourceImport> imports,
            final ValidationReportingStrategy reporter) {
        super(validationContext, suiteFile, imports, reporter);
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
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.NON_WORKSPACE_RESOURCE_IMPORT),
                        validationContext.getFile(), pathToken);
            }
        }
    }
}
