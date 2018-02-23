/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

/**
 * @author Michal Anglart
 *
 */
public class ProblemsReportingStrategyFactory {

    static HeadlessValidationReportingStrategy checkstylePanicReporter(final String reportFilepath,
            final Logger logger) {
        return new CheckstyleReportingStrategy(true, reportFilepath, logger);
    }

    static HeadlessValidationReportingStrategy checkstyleReporter(final String reportFilepath, final Logger logger) {
        return new CheckstyleReportingStrategy(false, reportFilepath, logger);
    }

    static abstract class HeadlessValidationReportingStrategy extends ValidationReportingStrategy {

        HeadlessValidationReportingStrategy(final boolean shouldPanic) {
            super(shouldPanic);
        }

        abstract void finishReporting();

        abstract void projectValidationStarted(final String projectName);

        abstract void projectValidationFinished(final String projectName);
    }
}
