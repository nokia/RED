/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.header.CommentsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.TasksTableHeaderRecognizer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.ImmutableMap;

/**
 * @author Michal Anglart
 *
 */
class UnknownTablesValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final RobotSuiteFile fileModel;

    private final ValidationReportingStrategy reporter;

    UnknownTablesValidator(final FileValidationContext validationContext, final RobotSuiteFile fileModel,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.fileModel = fileModel;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        fileModel.parse(); // otherwise the file can be not-yet-parsed

        final RobotFile astModel = fileModel.getLinkedElement();
        if (astModel == null) {
            return;
        }

        for (final RobotLine line : astModel.getFileContent()) {
            for (final IRobotLineElement lineElement : line.getLineElements()) {
                for (final IRobotTokenType type : lineElement.getTypes()) {
                    if (type == RobotTokenType.USER_OWN_TABLE_HEADER) {
                        reportUnrecognizedTableHeader((RobotToken) lineElement);
                    }
                }
            }
        }
    }

    private void reportUnrecognizedTableHeader(final RobotToken token) {
        if (CommentsTableHeaderRecognizer.EXPECTED.matcher(token.getText()).matches()) {
            return;
        }

        final RobotProblem problem;
        final String extractedName = extractSectionName(token.getText());
        if (validationContext.getVersion().isOlderThan(new RobotVersion(3, 1))) {
            final boolean isFutureTaskTable = TasksTableHeaderRecognizer.EXPECTED.matcher(token.getText()).matches();
            final String additionalMsg = isFutureTaskTable
                    ? ". The tasks table is introduced in Robot Framework 3.1. "
                            + "Please verify if your project uses at least that version."
                    : "";

            problem = RobotProblem.causedBy(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER)
                    .formatMessageWith(extractedName, additionalMsg);
        } else {
            problem = RobotProblem.causedBy(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31)
                    .formatMessageWith(extractedName);

        }
        final Map<String, Object> additionalAttributes = ImmutableMap.of(AdditionalMarkerAttributes.VALUE,
                extractedName);
        reporter.handleProblem(problem, fileModel.getFile(), token, additionalAttributes);
    }

    String extractSectionName(final String sectionName) {
        int firstChar = 0;
        for (int i = 0; i < sectionName.length(); i++) {
            final char ch = sectionName.charAt(i);
            if (ch != ' ' && ch != '*') {
                firstChar = i;
                break;
            }
        }
        int lastChar = sectionName.length() - 1;
        for (int i = sectionName.length() - 1; i >= 0; i--) {
            final char ch = sectionName.charAt(i);
            if (ch != ' ' && ch != '*') {
                lastChar = i;
                break;
            }
        }

        return sectionName.substring(firstChar, lastChar + 1);
    }
}
