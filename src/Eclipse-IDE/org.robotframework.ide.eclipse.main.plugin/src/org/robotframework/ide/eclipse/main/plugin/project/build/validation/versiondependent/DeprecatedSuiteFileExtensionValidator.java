/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

class DeprecatedSuiteFileExtensionValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSuiteFile fileModel;

    private final Class<? extends RobotSuiteFileSection> suiteSectionClass;

    private final ValidationReportingStrategy reporter;

    public DeprecatedSuiteFileExtensionValidator(final IFile file, final RobotSuiteFile fileModel,
            final Class<? extends RobotSuiteFileSection> suiteSectionClass,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.fileModel = fileModel;
        this.suiteSectionClass = suiteSectionClass;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.closedOpen(new RobotVersion(3, 1), new RobotVersion(3, 2));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final String extension = file.getFileExtension();
        if (!"robot".equalsIgnoreCase(extension)) {
            final Optional<? extends RobotSuiteFileSection> suiteSection = fileModel.findSection(suiteSectionClass);
            if (suiteSection.isPresent()) {
                final RobotSuiteFileSection section = suiteSection.get();
                final ProblemPosition position = ProblemPosition
                        .fromRegion(section.getDefinitionPosition().toFileRegion());

                final RobotProblem problem = createProblem(extension);
                reporter.handleProblem(problem, file, position);
            }
        }
    }

    protected RobotProblem createProblem(final String extension) {
        return RobotProblem.causedBy(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION)
                .formatMessageWith(extension);
    }
}
