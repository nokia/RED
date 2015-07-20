package org.robotframework.ide.eclipse.main.plugin.project.build.reporting;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;

import com.google.common.collect.Range;

public class FatalProblemsReportingStrategy extends ProblemsReportingStrategy {

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line) {
        super.handleProblem(problem, file, line);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Range<Integer> charRange) throws ReportingInterruptedException {
        super.handleProblem(problem, file, line, charRange);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }
}
