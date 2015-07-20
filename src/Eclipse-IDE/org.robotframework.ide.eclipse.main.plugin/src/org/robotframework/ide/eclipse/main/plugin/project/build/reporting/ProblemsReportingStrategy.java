package org.robotframework.ide.eclipse.main.plugin.project.build.reporting;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;

import com.google.common.collect.Range;

public class ProblemsReportingStrategy {

    public void handleProblem(final RobotProblem problem, final IFile file, final int line) throws ReportingInterruptedException {
        if (problem != null) {
            problem.createMarker(file, line);
        }
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Range<Integer> charRange) throws ReportingInterruptedException {
        if (problem != null) {
            problem.createMarker(file, line, charRange);
        }
    }

    public class ReportingInterruptedException extends RuntimeException {
        ReportingInterruptedException(final String message) {
            super(message);
        }
    }
}
