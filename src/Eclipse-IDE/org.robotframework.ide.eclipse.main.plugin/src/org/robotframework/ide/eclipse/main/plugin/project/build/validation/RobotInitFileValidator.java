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
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

public class RobotInitFileValidator extends RobotFileValidator {

    public RobotInitFileValidator(final ValidationContext context, final IFile file,
            final ValidationReportingStrategy reporter) {
        super(context, file, reporter);
    }

    @Override
    public void validate(final RobotSuiteFile fileModel, final FileValidationContext validationContext)
            throws CoreException {
        super.validate(fileModel, validationContext);
        
        validateIfThereAreNoForbiddenSections(fileModel);
        validateIfThereAreNoForbiddenSettings(fileModel);

    }

    private void validateIfThereAreNoForbiddenSections(final RobotSuiteFile fileModel) {
        Optional<ARobotSectionTable> table = fileModel.findSection(RobotCasesSection.class)
                .map(RobotCasesSection::getLinkedElement);
        if (!table.isPresent()) {
            table = fileModel.findSection(RobotTasksSection.class).map(RobotTasksSection::getLinkedElement);
        }

        table.map(ARobotSectionTable::getHeaders)
                .map(headers -> headers.get(0))
                .map(TableHeader::getDeclaration)
                .ifPresent(headerToken -> reporter.handleProblem(
                        RobotProblem.causedBy(SuiteFileProblem.INIT_FILE_CONTAINS_TESTS_OR_TASKS), file, headerToken));
    }

    private void validateIfThereAreNoForbiddenSettings(final RobotSuiteFile fileModel) {
        final Optional<RobotSettingsSection> settingsSection = fileModel.findSection(RobotSettingsSection.class);
        if (settingsSection.isPresent()) {
            final SettingTable settingsTable = settingsSection.get().getLinkedElement();
            for (final TestTemplate template : settingsTable.getTestTemplates()) {
                reportProblem(template.getDeclaration().getText(), template);
            }
            for (final DefaultTags defaultTag : settingsTable.getDefaultTags()) {
                reportProblem(defaultTag.getDeclaration().getText(), defaultTag);
            }
        }
    }

    private void reportProblem(final String declarationName, final AModelElement<?> element) {
        final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNSUPPORTED_SETTING)
                .formatMessageWith(declarationName, "initialization");
        final ProblemPosition position = new ProblemPosition(element.getBeginPosition().getLine(),
                Range.closed(element.getBeginPosition().getOffset(), element.getEndPosition().getOffset()));
        reporter.handleProblem(problem, file, position);
    }
}