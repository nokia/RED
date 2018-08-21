/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

class DeprecatedGeneralSettingsTableHeaderValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final SettingTable table;

    private final ValidationReportingStrategy reporter;

    DeprecatedGeneralSettingsTableHeaderValidator(final IFile file, final SettingTable table,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.table = table;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.closedOpen(new RobotVersion(3, 0), new RobotVersion(3, 1));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        reportOutdated("*** Settings ***");
    }

    private void reportOutdated(final String correctRepresentation) {
        for (final AModelElement<?> th : table.getHeaders()) {
            final RobotToken declarationToken = th.getDeclaration();
            final String text = declarationToken.getText();

            final String canonicalText = text.replaceAll("\\s", "").toLowerCase();
            if (canonicalText.contains("metadata")) {
                final RobotProblem problem = RobotProblem.causedBy(SuiteFileProblem.DEPRECATED_TABLE_HEADER)
                        .formatMessageWith(text, correctRepresentation);
                final Map<String, Object> additionalAttributes = ImmutableMap.of(AdditionalMarkerAttributes.VALUE,
                        correctRepresentation);
                reporter.handleProblem(problem, file, declarationToken, additionalAttributes);
            }
        }
    }
}
