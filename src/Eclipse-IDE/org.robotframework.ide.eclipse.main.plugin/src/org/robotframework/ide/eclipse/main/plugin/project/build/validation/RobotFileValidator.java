/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

public abstract class RobotFileValidator implements ModelUnitValidator {

    protected final ValidationContext validationContext;

    protected final IFile file;

    protected final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    public RobotFileValidator(final ValidationContext context, final IFile file) {
        this.validationContext = context;
        this.file = file;
    }

    @Override
    public final void validate(final IProgressMonitor monitor) throws CoreException {
        final RobotSuiteFile robotFile = new RobotSuiteFile(null, file);
        validate(robotFile, monitor);
    }

    /**
     * This method does common validation for different file types (resources, inits, suites).
     * It should be overridden and called by subclasses
     * 
     * @param fileModel
     * @param monitor
     * @throws CoreException
     */
    public void validate(final RobotSuiteFile fileModel, final IProgressMonitor monitor) throws CoreException {
        // TODO : check output status and parsing messages
        new TestCasesTableValidator(fileModel.findSection(RobotCasesSection.class)).validate(monitor);
        new GeneralSettingsTableValidator(fileModel.findSection(RobotSettingsSection.class)).validate(monitor);
        new KeywordTableValidator(fileModel.findSection(RobotKeywordsSection.class))
                .validate(monitor);
        new VariablesTableValidator(validationContext, fileModel.findSection(RobotVariablesSection.class))
                .validate(monitor);
    }
}