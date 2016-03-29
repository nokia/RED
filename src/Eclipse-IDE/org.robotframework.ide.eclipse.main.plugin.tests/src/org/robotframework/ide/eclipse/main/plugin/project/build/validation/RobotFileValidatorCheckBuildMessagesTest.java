/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.base.Optional;

public class RobotFileValidatorCheckBuildMessagesTest {

    private IFile file;

    private IPath path;

    private RobotModel model;

    private RobotSuiteFile suiteFile;

    private RobotFile coreModel;

    private RobotFileOutput toUpdateMessages;

    private ValidationContext context;

    private BuildLogger logger;

    private ProblemsReportingStrategy reporter;

    private RobotFileValidator rfv;

    private IProgressMonitor monitor;

    @Test
    public void test_validateForParsingFailed_noExtraMessages() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        // execute
        rfv.validate(monitor);

        // validate
        InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(file, times(1)).getName();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verifyNoMoreInteractions();

        Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(1);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays
                .asList(new Problem[] { new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)) }));
    }

    @Before
    public void setUp() {
        this.reporter = new MockReporter();
        this.file = mock(IFile.class);
        this.path = mock(IPath.class);
        when(file.getFullPath()).thenReturn(path);
        this.suiteFile = createSuiteFile();
        this.coreModel = mock(RobotFile.class);
        when(suiteFile.getLinkedElement()).thenReturn(coreModel);
        this.toUpdateMessages = mock(RobotFileOutput.class);
        when(coreModel.getParent()).thenReturn(toUpdateMessages);

        this.model = mock(RobotModel.class);
        when(model.createSuiteFile(file)).thenReturn(suiteFile);
        this.logger = mock(BuildLogger.class);
        this.monitor = mock(IProgressMonitor.class);
        this.context = createValidationContext(model, logger);
        this.rfv = new MockRobotFileValidator(context, file, reporter);
    }

    @After
    public void tearDown() {
        this.reporter = null;
        this.file = null;
        this.path = null;
        this.model = null;
        this.suiteFile = null;
        this.context = null;
        this.rfv = null;
        this.monitor = null;
        this.logger = null;
    }

    private RobotSuiteFile createSuiteFile() {
        RobotSuiteFile mySuiteFile = mock(RobotSuiteFile.class);

        Optional<RobotCasesSection> cases = Optional.absent();
        when(mySuiteFile.findSection(RobotCasesSection.class)).thenReturn(cases);
        Optional<RobotSettingsSection> settings = Optional.absent();
        when(mySuiteFile.findSection(RobotSettingsSection.class)).thenReturn(settings);
        Optional<RobotKeywordsSection> keywords = Optional.absent();
        when(mySuiteFile.findSection(RobotKeywordsSection.class)).thenReturn(keywords);
        Optional<RobotVariablesSection> variables = Optional.absent();
        when(mySuiteFile.findSection(RobotVariablesSection.class)).thenReturn(variables);

        return mySuiteFile;
    }

    private ValidationContext createValidationContext(final RobotModel model, final BuildLogger logger) {
        final IProject project = mock(IProject.class);
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotRuntimeEnvironment robotRuntime = mock(RobotRuntimeEnvironment.class);

        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(robotRuntime);
        when(robotProject.getVersion()).thenReturn("3.0");

        return new ValidationContext(model, project, logger);
    }

    private class MockRobotFileValidator extends RobotFileValidator {

        public MockRobotFileValidator(final ValidationContext context, final IFile file,
                final ProblemsReportingStrategy reporter) {
            super(context, file, reporter);
        }
    }
}
