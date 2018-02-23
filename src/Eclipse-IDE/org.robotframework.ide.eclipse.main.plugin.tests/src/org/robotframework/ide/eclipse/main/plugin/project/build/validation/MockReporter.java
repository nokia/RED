/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class MockReporter extends ValidationReportingStrategy {

    private final List<Problem> problems = new ArrayList<>();

    public MockReporter() {
        super(false);
    }

    public boolean wasProblemReported() {
        return !problems.isEmpty();
    }

    public int getNumberOfReportedProblems() {
        return problems.size();
    }

    public Collection<Problem> getReportedProblems() {
        return problems;
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line)
            throws ReportingInterruptedException {
        handleProblem(problem, file, new ProblemPosition(line), new HashMap<>());
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final int line,
            final Map<String, Object> additionalAttributes) throws ReportingInterruptedException {
        handleProblem(problem, file, new ProblemPosition(line), additionalAttributes);
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final RobotToken token) {
        final ProblemPosition position = new ProblemPosition(token.getLineNumber(),
                Range.closed(token.getStartOffset(), token.getStartOffset() + token.getText().length()));
        handleProblem(problem, file, position, new HashMap<>());
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final RobotToken token,
            final Map<String, Object> additionalAttributes) {
        final ProblemPosition position = new ProblemPosition(token.getLineNumber(),
                Range.closed(token.getStartOffset(), token.getStartOffset() + token.getText().length()));
        handleProblem(problem, file, position, additionalAttributes);
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition) {
        handleProblem(problem, file, filePosition, new HashMap<>());
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
            final Map<String, Object> additionalAttributes) {
        problems.add(new Problem(problem.getCause(), filePosition));
    }

    public static final class Problem {

        private final IProblemCause cause;

        private final ProblemPosition position;

        public Problem(final IProblemCause cause, final ProblemPosition filePosition) {
            this.cause = cause;
            this.position = filePosition;
        }

        public IProblemCause getCause() {
            return cause;
        }

        public ProblemPosition getPosition() {
            return position;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Problem) {
                final Problem that = (Problem) obj;
                return Objects.equals(this.cause, that.cause) && Objects.equals(this.position, that.position);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cause, position);
        }

        @Override
        public String toString() {
            return "Problem: " + cause + "@" + position;
        }
    }
}
