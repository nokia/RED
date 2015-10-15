/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;

import com.google.common.collect.Range;

public class ProblemsReportingStrategy {

    public void handleProblem(final RobotProblem problem, final IFile file, final int line) throws ReportingInterruptedException {
        handleProblem(problem, file, new ProblemPosition(line), new HashMap<String, Object>());
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Map<String, Object> additionalAttributes) throws ReportingInterruptedException {
        handleProblem(problem, file, new ProblemPosition(line), additionalAttributes);
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final RobotToken token) {
        final ProblemPosition position = new ProblemPosition(token.getLineNumber(),
                Range.closed(token.getStartOffset(), token.getStartOffset() + token.getText().length()));
        handleProblem(problem, file, position, new HashMap<String, Object>());
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final RobotToken token,
            final Map<String, Object> additionalAttributes) {
        final ProblemPosition position = new ProblemPosition(token.getLineNumber(),
                Range.closed(token.getStartOffset(), token.getStartOffset() + token.getText().length()));
        handleProblem(problem, file, position, additionalAttributes);
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition) {
        handleProblem(problem, file, filePosition, new HashMap<String, Object>());
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
            final Map<String, Object> additionalAttributes) {
        if (problem != null) {
            problem.createMarker(file, filePosition, additionalAttributes);
        }
    }

    public class ReportingInterruptedException extends RuntimeException {
        ReportingInterruptedException(final String message) {
            super(message);
        }
    }
}
