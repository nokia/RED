/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author Michal Anglart
 *
 */
public class ProblemsGatheringReportingStrategy extends ProblemsReportingStrategy {

    static ProblemsGatheringReportingStrategy reportOnly() {
        return new ProblemsGatheringReportingStrategy(false);
    }

    static ProblemsGatheringReportingStrategy reportAndPanic() {
        return new ProblemsGatheringReportingStrategy(true);
    }

    protected ProblemsGatheringReportingStrategy(final boolean shouldPanic) {
        super(shouldPanic);
    }

    private final Table<IPath, ProblemPosition, RobotProblem> problems = HashBasedTable.create();

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
            final Map<String, Object> additionalAttributes) {
        problems.put(file.getLocation(), filePosition, problem);
        if (shouldPanic) {
            throw new ReportingInterruptedException("Building and validation was interrupted by fatal problem");
        }
    }

    int getNumberOfProblems() {
        return problems.size();
    }

    public Table<IPath, ProblemPosition, RobotProblem> getProblems() {
        return problems;
    }
}
