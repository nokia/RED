/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
class UnknownTablesValidator implements ModelUnitValidator {

    private final RobotSuiteFile fileModel;

    private final ProblemsReportingStrategy reporter;

    UnknownTablesValidator(final RobotSuiteFile fileModel) {
        this.fileModel = fileModel;
        this.reporter = new ProblemsReportingStrategy();
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final RobotFile astModel = fileModel.getLinkedElement();
        if (astModel == null) {
            return;
        }

        for (final RobotLine line : astModel.getFileContent()) {
            for (final IRobotLineElement lineElement : line.getLineElements()) {
                for (final IRobotTokenType type : lineElement.getTypes()) {
                    if (type == RobotTokenType.USER_OWN_TABLE_HEADER) {
                        reportUnrecognizedTableHeader(lineElement);
                    }
                }
            }
        }
    }

    private void reportUnrecognizedTableHeader(final IRobotLineElement lineElement) {
        final String content = lineElement.getText().toString();
        final RobotProblem problem = RobotProblem.causedBy(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER)
                .formatMessageWith(content);
        final Range<Integer> offsetRange = Range.closed(lineElement.getStartOffset(),
                lineElement.getStartOffset() + content.length());
        final ProblemPosition filePosition = new ProblemPosition(lineElement.getLineNumber(), offsetRange);
        reporter.handleProblem(problem, fileModel.getFile(), filePosition);
    }

}
