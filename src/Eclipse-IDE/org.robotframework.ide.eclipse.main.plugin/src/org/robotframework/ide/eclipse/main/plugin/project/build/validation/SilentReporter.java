/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

class SilentReporter extends ValidationReportingStrategy {

    SilentReporter() {
        super(false);
    }

    @Override
    protected void reportProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
            final Map<String, Object> additionalAttributes) {
        // this reporter will not report anything; this is used for general settings suite/test setups which are
        // validated elsewhere but here they may introduce new variables
    }
}