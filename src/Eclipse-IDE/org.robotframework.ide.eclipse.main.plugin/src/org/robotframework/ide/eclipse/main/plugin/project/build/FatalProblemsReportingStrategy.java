package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.Map;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.Range;

public class FatalProblemsReportingStrategy extends ProblemsReportingStrategy {

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line) {
        super.handleProblem(problem, file, line);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Map<String, Object> additionalAttributes) throws ReportingInterruptedException {
        super.handleProblem(problem, file, line, additionalAttributes);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Range<Integer> charRange) throws ReportingInterruptedException {
        super.handleProblem(problem, file, line, charRange);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Range<Integer> charRange, final Map<String, Object> additionalAttributes)
            throws ReportingInterruptedException {
        super.handleProblem(problem, file, line, charRange, additionalAttributes);
        throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
    }
}
