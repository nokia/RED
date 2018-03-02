/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage.LogLevel;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

public abstract class RobotFileValidator implements ModelUnitValidator {

    private final ValidationContext context;

    protected final IFile file;

    protected final ValidationReportingStrategy reporter;

    public RobotFileValidator(final ValidationContext context, final IFile file,
            final ValidationReportingStrategy reporter) {
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

        context.getLogger().log("VALIDATING: " + file.getFullPath().toString());
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
        
        new UnknownTablesValidator(fileModel, reporter).validate(null);
        new TestCaseTableValidator(validationContext, fileModel.findSection(RobotCasesSection.class), reporter)
                .validate(null);
        new GeneralSettingsTableValidator(validationContext, fileModel.findSection(RobotSettingsSection.class),
                reporter).validate(null);
        new KeywordTableValidator(validationContext, fileModel.findSection(RobotKeywordsSection.class), reporter)
                .validate(null);
        new VariablesTableValidator(validationContext, fileModel.findSection(RobotVariablesSection.class), reporter)
                .validate(null);

        checkRobotFileOutputStatus(fileModel);

        new RobotTasksReporter(fileModel, reporter).reportTasks();
    }
    
    private void checkRobotFileOutputStatus(final RobotSuiteFile fileModel) {
        final RobotFile linkedElement = fileModel.getLinkedElement();
        if (linkedElement == null) {
            return;
        }
        final RobotFileOutput robotFileOutput = linkedElement.getParent();
        if (robotFileOutput == null) {
            return;
        }
        if (robotFileOutput.getStatus() == Status.FAILED) {
            final RobotProblem problem = RobotProblem.causedBy(SuiteFileProblem.FILE_PARSING_FAILED)
                    .formatMessageWith(file.getName());
            reporter.handleProblem(problem, file, -1);
        }
        for (final BuildMessage buildMessage : robotFileOutput.getBuildingMessages()) {
            if (buildMessage.getType() == LogLevel.ERROR) {
                final RobotProblem problem = RobotProblem.causedBy(SuiteFileProblem.BUILD_ERROR_MESSAGE)
                        .formatMessageWith(buildMessage.getMessage());
                final ProblemPosition position = ProblemPosition.fromRegion(buildMessage.getFileRegion());
                reporter.handleProblem(problem, file, position);

            } else if (buildMessage.getType() == LogLevel.WARN) {
                final RobotProblem problem = RobotProblem.causedBy(SuiteFileProblem.BUILD_WARNING_MESSAGE)
                        .formatMessageWith(buildMessage.getMessage());
                final ProblemPosition position = ProblemPosition.fromRegion(buildMessage.getFileRegion());
                reporter.handleProblem(problem, file, position);
            }
        }
    }
}