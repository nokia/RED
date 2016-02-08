/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.validation.ProblemsReportingStrategyFactory.HeadlessValidationReportingStrategy;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
public class CheckstyleReportingStrategy extends HeadlessValidationReportingStrategy {

    private final Logger logger;
    private final String reportFilepath;

    private int numberOfProblems;

    private final Multimap<IPath, RobotProblemWithPosition> problems;

    private long start;

    CheckstyleReportingStrategy(final boolean shouldPanic, final String reportFilepath, final Logger logger) {
        super(shouldPanic);
        this.logger = logger;
        this.reportFilepath = reportFilepath;
        this.numberOfProblems = 0;
        this.problems = ArrayListMultimap.create();
    }

    @Override
    public synchronized void handleProblem(final RobotProblem problem, final IFile file,
            final ProblemPosition filePosition, final Map<String, Object> additionalAttributes) {
        numberOfProblems++;
        if (reportFilepath != null) {
            problems.put(file.getLocation(), new RobotProblemWithPosition(problem, filePosition));
        }
        if (shouldPanic) {
            throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
        }
    }

    @Override
    public void finishReporting() {
        if (reportFilepath != null) {
            if (numberOfProblems != problems.size()) {
                logger.logError("There is an invalid number of problems handled: " + numberOfProblems + " counted, but "
                        + problems.size() + " stored");
            }
            generateFile(new File(reportFilepath), problems);
        }
    }

    private void generateFile(final File file, final Multimap<IPath, RobotProblemWithPosition> problems) {
        logger.log("Generating report file '" + file.getAbsolutePath() + "'");
        try (ReportWithCheckstyleFormat checkstyleReporter = new ReportWithCheckstyleFormat(file)) {
            checkstyleReporter.writeHeader();
            checkstyleReporter.writeEntries(problems);
            checkstyleReporter.writeFooter();
            logger.log("Report file '" + file.getAbsolutePath() + "' has been generated");
        } catch (final IOException e) {
            logger.logError("Unable to create report file '" + file.getAbsolutePath() + "'. Reason: " + e.getMessage());
        }
    }

    @Override
    void projectValidationStarted(final String projectName) {
        this.start = System.currentTimeMillis();
    }

    @Override
    void projectValidationFinished(final String projectName) {
        final long end = System.currentTimeMillis();
        final double duration = (end - start) / 1000.0;

        if (shouldPanic && numberOfProblems > 0) {
            logger.log(
                    String.format("Project %s validation has FINISHED (took %.3f seconds and found %d fatal problems)",
                            projectName, duration, numberOfProblems));
        } else if (!shouldPanic) {
            logger.log(String.format("Project %s validation has FINISHED (took %.3f seconds and found %d problems)",
                    projectName, duration, numberOfProblems));
        }
    }

    static class RobotProblemWithPosition {

        private final RobotProblem problem;

        private final ProblemPosition position;

        RobotProblemWithPosition(final RobotProblem problem, final ProblemPosition filePosition) {
            this.problem = problem;
            this.position = filePosition;
        }

        public ProblemPosition getPosition() {
            return position;
        }

        public RobotProblem getProblem() {
            return problem;
        }
    }
}
