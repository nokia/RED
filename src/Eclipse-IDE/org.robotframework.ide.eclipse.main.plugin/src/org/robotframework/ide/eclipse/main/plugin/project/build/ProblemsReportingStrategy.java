package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.Range;

public class ProblemsReportingStrategy {

    public void handleProblem(final RobotProblem problem, final IFile file, final int line) throws ReportingInterruptedException {
        handleProblem(problem, file, line, null, new HashMap<String, Object>());
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Map<String, Object> additionalAttributes) throws ReportingInterruptedException {
        handleProblem(problem, file, line, null, additionalAttributes);
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Range<Integer> charRange) throws ReportingInterruptedException {
        handleProblem(problem, file, line, charRange, new HashMap<String, Object>());
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Range<Integer> charRange, final Map<String, Object> additionalAttributes)
            throws ReportingInterruptedException {
        if (problem != null) {
            problem.createMarker(file, line, charRange, additionalAttributes);
        }
    }

    public class ReportingInterruptedException extends RuntimeException {
        ReportingInterruptedException(final String message) {
            super(message);
        }
    }
}
