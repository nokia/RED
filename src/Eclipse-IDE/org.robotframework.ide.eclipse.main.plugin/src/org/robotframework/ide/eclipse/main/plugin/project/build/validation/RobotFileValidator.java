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

    private final ValidationContext context;

    protected final IFile file;

    protected final ProblemsReportingStrategy reporter;

    public RobotFileValidator(final ValidationContext context, final IFile file,
            final ProblemsReportingStrategy reporter) {
        this.context = context;
        this.file = file;
        this.reporter = reporter;
    }

    @Override
    public final void validate(final IProgressMonitor monitor) throws CoreException {
        final RobotSuiteFile suiteFile = context.getModel().createSuiteFile(file);
        validate(suiteFile, monitor);
    }

    public final void validate(final RobotSuiteFile suiteFile, final IProgressMonitor monitor) throws CoreException {
        final FileValidationContext fileValidationContext = context.createUnitContext(file);

        monitor.setTaskName(file.getFullPath().toPortableString());
        validate(suiteFile, fileValidationContext);
    }

    /**
     * This method does common validation for different file types (resources, inits, suites).
     * It should be overridden and called by subclasses
     * 
     * @param fileModel
     * @param monitor
     * @param validationContext
     * @throws CoreException
     */
    protected void validate(final RobotSuiteFile fileModel, final FileValidationContext validationContext)
            throws CoreException {
        // TODO : check output status and parsing messages

        new UnknownTablesValidator(fileModel, reporter).validate(null);
        new TestCasesTableValidator(validationContext, fileModel.findSection(RobotCasesSection.class), reporter)
                .validate(null);
        new GeneralSettingsTableValidator(validationContext, fileModel.findSection(RobotSettingsSection.class),
                reporter).validate(null);
        new KeywordTableValidator(validationContext, fileModel.findSection(RobotKeywordsSection.class), reporter)
                .validate(null);
        new VariablesTableValidator(validationContext, fileModel.findSection(RobotVariablesSection.class), reporter)
                .validate(null);
    }
}