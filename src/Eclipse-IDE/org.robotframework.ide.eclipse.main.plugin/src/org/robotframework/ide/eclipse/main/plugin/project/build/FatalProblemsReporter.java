package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;

public class FatalProblemsReporter implements IProblemsReporter {

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line) {
        problem.createMarker(file, line);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }

    public class ReportingInterruptedException extends RuntimeException {
        public ReportingInterruptedException(final String message) {
            super(message);
        }
    }
}
