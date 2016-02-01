/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;

import com.google.common.collect.Range;

public class SuitePreconditionDeclarationSettingValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSettingsSection section;

    private final ProblemsReportingStrategy reporter;

    public SuitePreconditionDeclarationSettingValidator(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub

    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.all();
    }

}
