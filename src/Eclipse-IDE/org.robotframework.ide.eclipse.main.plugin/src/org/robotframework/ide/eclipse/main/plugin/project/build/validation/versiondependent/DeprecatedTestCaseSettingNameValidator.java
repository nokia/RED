/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

class DeprecatedTestCaseSettingNameValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final TestCase testCase;

    private final ValidationReportingStrategy reporter;

    DeprecatedTestCaseSettingNameValidator(final IFile file, final TestCase testCase,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.testCase = testCase;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.closedOpen(new RobotVersion(3, 0), new RobotVersion(3, 1));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final RobotVersion version = new RobotVersion(3, 0);

        reportOutdated(testCase.getDocumentation(),
                RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
        reportOutdated(testCase.getSetups(),
                RobotTokenType.TEST_CASE_SETTING_SETUP.getTheMostCorrectOneRepresentation(version).getRepresentation());
        reportOutdated(testCase.getTeardowns(),
                RobotTokenType.TEST_CASE_SETTING_TEARDOWN.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
    }

    private void reportOutdated(final List<? extends AModelElement<?>> settings, final String correctRepresentation) {
        for (final AModelElement<?> setting : settings) {
            final RobotToken declarationToken = setting.getDeclaration();
            final String text = declarationToken.getText();

            final String canonicalText = text.replaceAll("\\s", "").toLowerCase();
            final String canonicalCorrectRepresentation = correctRepresentation.replaceAll("\\s", "").toLowerCase();

            if (!canonicalText.contains(canonicalCorrectRepresentation)) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.DEPRECATED_CASE_SETTING_NAME)
                        .formatMessageWith(text, correctRepresentation);
                final Map<String, Object> additionalAttributes = ImmutableMap.of(AdditionalMarkerAttributes.VALUE,
                        correctRepresentation);
                reporter.handleProblem(problem, file, declarationToken, additionalAttributes);
            }
        }
    }
}
