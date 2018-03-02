/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

/**
 * @author Michal Anglart
 *
 */
class UnknownTablesValidator implements ModelUnitValidator {

    private final RobotSuiteFile fileModel;

    private final ValidationReportingStrategy reporter;

    UnknownTablesValidator(final RobotSuiteFile fileModel, final ValidationReportingStrategy reporter) {
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
        final RobotProblem problem = RobotProblem.causedBy(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER)
                .formatMessageWith(token.getText());
        reporter.handleProblem(problem, fileModel.getFile(), token);
    }
}
