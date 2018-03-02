/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.Range;

public class RobotResourceFileValidator extends RobotFileValidator {

    public RobotResourceFileValidator(final ValidationContext context, final IFile file,
            final ValidationReportingStrategy reporter) {
        super(context, file, reporter);
    }

    @Override
    public void validate(final RobotSuiteFile fileModel, final FileValidationContext validationContext)
            throws CoreException {
        super.validate(fileModel, validationContext);

        final Optional<RobotSettingsSection> settingsSection = fileModel.findSection(RobotSettingsSection.class);
        validateIfThereAreNoForbiddenSettings(settingsSection);
    }

    private void validateIfThereAreNoForbiddenSettings(final Optional<RobotSettingsSection> settingsSection) {
        if (!settingsSection.isPresent()) {
            return;
        }
        final SettingTable settingsTable = settingsSection.get().getLinkedElement();
        for (final SuiteSetup setup : settingsTable.getSuiteSetups()) {
            reportProblem(setup.getDeclaration().getText(), setup);
        }
        for (final SuiteTeardown teardown : settingsTable.getSuiteTeardowns()) {
            reportProblem(teardown.getDeclaration().getText(), teardown);
        }
        for (final TestSetup testSetup : settingsTable.getTestSetups()) {
            reportProblem(testSetup.getDeclaration().getText(), testSetup);
        }
        for (final TestTeardown testTeardown : settingsTable.getTestTeardowns()) {
            reportProblem(testTeardown.getDeclaration().getText(), testTeardown);
        }
        for (final TestTemplate template : settingsTable.getTestTemplates()) {
            reportProblem(template.getDeclaration().getText(), template);
        }
        for (final TestTimeout testTimeout : settingsTable.getTestTimeouts()) {
            reportProblem(testTimeout.getDeclaration().getText(), testTimeout);
        }
        for (final ForceTags forceTag : settingsTable.getForceTags()) {
            reportProblem(forceTag.getDeclaration().getText(), forceTag);
        }
        for (final DefaultTags defaultTag : settingsTable.getDefaultTags()) {
            reportProblem(defaultTag.getDeclaration().getText(), defaultTag);
        }
        for (final Metadata metadata : settingsTable.getMetadatas()) {
            reportProblem(metadata.getDeclaration().getText(), metadata);
        }
    }

    private void reportProblem(final String declarationName, final AModelElement<?> element) {
        final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNSUPPORTED_SETTING)
                .formatMessageWith(declarationName, "resource");
        final ProblemPosition position = new ProblemPosition(element.getBeginPosition().getLine(),
                Range.closed(element.getBeginPosition().getOffset(), element.getEndPosition().getOffset()));
        reporter.handleProblem(problem, file, position);
    }
}