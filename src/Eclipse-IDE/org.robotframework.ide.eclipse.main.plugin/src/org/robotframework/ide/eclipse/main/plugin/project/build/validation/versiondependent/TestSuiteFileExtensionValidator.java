/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

class TestSuiteFileExtensionValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSuiteFile fileModel;

    private final ValidationReportingStrategy reporter;

    public TestSuiteFileExtensionValidator(final IFile file, final RobotSuiteFile fileModel,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.fileModel = fileModel;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 1));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (fileModel.isSuiteFile()) {
            final String extension = file.getFileExtension();
            if (!"robot".equals(extension)) {
                final RobotProblem problem = RobotProblem
                        .causedBy(SuiteFileProblem.DEPRECATED_TEST_SUITE_FILE_EXTENSION)
                        .formatMessageWith(extension);
                reporter.handleProblem(problem, file, 1);
            }
        }
    }
}
