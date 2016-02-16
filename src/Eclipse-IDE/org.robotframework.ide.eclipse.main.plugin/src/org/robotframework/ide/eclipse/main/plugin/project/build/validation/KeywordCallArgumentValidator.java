/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

/**
 * @author Michal Anglart
 *
 */
public class KeywordCallArgumentValidator implements ModelUnitValidator {

    private final ProblemsReportingStrategy reporter;

    public KeywordCallArgumentValidator(final ProblemsReportingStrategy reporter) {
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        // TODO Auto-generated method stub

    }
}
