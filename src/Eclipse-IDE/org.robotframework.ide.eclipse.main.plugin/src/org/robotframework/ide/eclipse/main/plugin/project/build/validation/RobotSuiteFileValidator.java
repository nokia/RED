/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

public class RobotSuiteFileValidator extends RobotFileValidator {

    public RobotSuiteFileValidator(final ValidationContext context, final IFile file,
            final ValidationReportingStrategy reporter) {
        super(context, file, reporter);
    }

    @Override
    public void validate(final RobotSuiteFile fileModel, final FileValidationContext validationContext)
            throws CoreException {
        super.validate(fileModel, validationContext);

        validateIfThereAreNoForbiddenSections(fileModel);
    }

    @Override
    void reportVersionSpecificProblems(final FileValidationContext validationContext, final RobotSuiteFile fileModel)
            throws CoreException {
        new VersionDependentValidators(validationContext, reporter)
                .getFileValidators(fileModel, RobotCasesSection.class)
                .forEach(ModelUnitValidator::validate);
    }

    private void validateIfThereAreNoForbiddenSections(final RobotSuiteFile fileModel) {
        fileModel.findSection(RobotTasksSection.class)
                .map(RobotTasksSection::getLinkedElement)
                .map(ARobotSectionTable::getHeaders)
                .map(headers -> headers.get(0))
                .map(TableHeader::getDeclaration)
                .ifPresent(headerToken -> reporter.handleProblem(
                        RobotProblem.causedBy(SuiteFileProblem.SUITE_FILE_CONTAINS_TASKS), file, headerToken));
    }
}
