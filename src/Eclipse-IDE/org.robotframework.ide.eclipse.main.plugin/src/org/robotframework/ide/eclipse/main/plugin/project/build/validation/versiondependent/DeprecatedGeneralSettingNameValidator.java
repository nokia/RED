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
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

class DeprecatedGeneralSettingNameValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final SettingTable table;

    private final ValidationReportingStrategy reporter;

    DeprecatedGeneralSettingNameValidator(final IFile file, final SettingTable table,
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
        final RobotVersion version = new RobotVersion(3, 0);

        reportOutdated(table.getDocumentation(),
                RobotTokenType.SETTING_DOCUMENTATION_DECLARATION.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
        reportOutdated(table.getSuiteSetups(),
                RobotTokenType.SETTING_SUITE_SETUP_DECLARATION.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
        reportOutdated(table.getSuiteTeardowns(),
                RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
        reportOutdated(table.getTestSetups(),
                RobotTokenType.SETTING_TEST_SETUP_DECLARATION.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
        reportOutdated(table.getTestTeardowns(),
                RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION.getTheMostCorrectOneRepresentation(version)
                        .getRepresentation());
    }

    private void reportOutdated(final List<? extends AModelElement<?>> settings, final String correctRepresentation) {
        for (final AModelElement<?> setting : settings) {
            final RobotToken declarationToken = setting.getDeclaration();
            final String text = declarationToken.getText();

            final String canonicalText = text.replaceAll("\\s", "").toLowerCase();
            final String canonicalCorrectRepresentation = correctRepresentation.replaceAll("\\s", "").toLowerCase();

            if (!canonicalText.contains(canonicalCorrectRepresentation)) {
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.DEPRECATED_SETTING_NAME)
                        .formatMessageWith(text, correctRepresentation);
                final Map<String, Object> additionalAttributes = ImmutableMap.of(AdditionalMarkerAttributes.VALUE,
                        correctRepresentation);
                reporter.handleProblem(problem, file, declarationToken, additionalAttributes);
            }
        }
    }
}
