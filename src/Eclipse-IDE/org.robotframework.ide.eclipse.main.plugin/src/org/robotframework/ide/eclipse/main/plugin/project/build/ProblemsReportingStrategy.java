package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class ProblemsReportingStrategy {

    public void handleProblem(final RobotProblem problem, final IFile file, final int line) throws ReportingInterruptedException {
        handleProblem(problem, file, new ProblemPosition(line), new HashMap<String, Object>());
    }

    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Map<String, Object> additionalAttributes) throws ReportingInterruptedException {
        handleProblem(problem, file, new ProblemPosition(line), additionalAttributes);
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
