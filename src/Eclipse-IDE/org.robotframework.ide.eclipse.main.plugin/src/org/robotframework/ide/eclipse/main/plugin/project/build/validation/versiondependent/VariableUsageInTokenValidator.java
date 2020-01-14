/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.UnknownVariables;

import com.google.common.collect.Range;


class VariableUsageInTokenValidator extends VersionDependentModelUnitValidator {

    private final FileValidationContext validationContext;

    private final RobotToken token;

    private final ValidationReportingStrategy reporter;

    private final Range<RobotVersion> applicableVersion;

    VariableUsageInTokenValidator(final FileValidationContext validationContext, final Range<RobotVersion> applicableVersion,
            final RobotToken nameToken, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.token = nameToken;
        this.reporter = reporter;
        this.applicableVersion = applicableVersion;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return applicableVersion;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        new UnknownVariables(validationContext, reporter).reportUnknownVars(token);
    }
}
