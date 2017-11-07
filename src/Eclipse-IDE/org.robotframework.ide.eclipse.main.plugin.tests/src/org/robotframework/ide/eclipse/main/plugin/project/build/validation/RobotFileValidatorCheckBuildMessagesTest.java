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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage.LogLevel;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

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
    public void test_validateForParsingPassed_oneInfo_oneWarn_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage infoMsg = mock(BuildMessage.class);
        when(infoMsg.getType()).thenReturn(LogLevel.INFO);
        final BuildMessage warningMsg = mock(BuildMessage.class);
        when(warningMsg.getType()).thenReturn(LogLevel.WARN);
        final BuildMessage errorMsg = mock(BuildMessage.class);
        when(errorMsg.getType()).thenReturn(LogLevel.ERROR);
        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);
        when(warningMsg.getFileRegion()).thenReturn(region);
        when(errorMsg.getFileRegion()).thenReturn(region);

        buildMsgs.add(infoMsg);
        buildMsgs.add(warningMsg);
        buildMsgs.add(errorMsg);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, infoMsg, warningMsg, errorMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(infoMsg, times(2)).getType();
        order.verify(warningMsg, times(2)).getType();
        order.verify(warningMsg, times(1)).getMessage();
        order.verify(warningMsg, times(1)).getFileRegion();
        order.verify(errorMsg, times(1)).getType();
        order.verify(errorMsg, times(1)).getMessage();
        order.verify(errorMsg, times(1)).getFileRegion();

        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(2);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays.asList(new Problem[] {
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))) }));
    }

    @Test
    public void test_validateForParsingPassed_oneInfo() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage infoMsg = mock(BuildMessage.class);
        buildMsgs.add(infoMsg);
        when(infoMsg.getType()).thenReturn(LogLevel.INFO);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, infoMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(infoMsg, times(2)).getType();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).isEmpty();
    }

    @Test
    public void test_validateForParsingPassed_oneWarning() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage warningMsg = mock(BuildMessage.class);
        buildMsgs.add(warningMsg);
        when(warningMsg.getType()).thenReturn(LogLevel.WARN);
        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);
        when(warningMsg.getFileRegion()).thenReturn(region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, warningMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(warningMsg, times(2)).getType();
        order.verify(warningMsg, times(1)).getMessage();
        order.verify(warningMsg, times(1)).getFileRegion();

        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(1);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays.asList(new Problem[] {
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))) }));
    }

    @Test
    public void test_validateForParsingPassed_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage errorMsg = mock(BuildMessage.class);
        buildMsgs.add(errorMsg);
        when(errorMsg.getType()).thenReturn(LogLevel.ERROR);
        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);
        when(errorMsg.getFileRegion()).thenReturn(region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, errorMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(errorMsg, times(1)).getType();
        order.verify(errorMsg, times(1)).getMessage();
        order.verify(errorMsg, times(1)).getFileRegion();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(1);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays.asList(new Problem[] {
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))) }));
    }

    @Test
    public void test_validateForParsingPassed_noExtraMessages() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).isEmpty();
    }

    @Test
    public void test_validateForParsingFailed_oneInfo_oneWarn_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage infoMsg = mock(BuildMessage.class);
        when(infoMsg.getType()).thenReturn(LogLevel.INFO);
        final BuildMessage warningMsg = mock(BuildMessage.class);
        when(warningMsg.getType()).thenReturn(LogLevel.WARN);
        final BuildMessage errorMsg = mock(BuildMessage.class);
        when(errorMsg.getType()).thenReturn(LogLevel.ERROR);
        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);
        when(warningMsg.getFileRegion()).thenReturn(region);
        when(errorMsg.getFileRegion()).thenReturn(region);

        buildMsgs.add(infoMsg);
        buildMsgs.add(warningMsg);
        buildMsgs.add(errorMsg);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, infoMsg, warningMsg, errorMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(file, times(1)).getName();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(infoMsg, times(2)).getType();
        order.verify(warningMsg, times(2)).getType();
        order.verify(warningMsg, times(1)).getMessage();
        order.verify(warningMsg, times(1)).getFileRegion();
        order.verify(errorMsg, times(1)).getType();
        order.verify(errorMsg, times(1)).getMessage();
        order.verify(errorMsg, times(1)).getFileRegion();

        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(3);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays.asList(new Problem[] {
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))) }));
    }

    @Test
    public void test_validateForParsingFailed_oneInfo() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage infoMsg = mock(BuildMessage.class);
        buildMsgs.add(infoMsg);
        when(infoMsg.getType()).thenReturn(LogLevel.INFO);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, infoMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(file, times(1)).getName();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(infoMsg, times(2)).getType();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(1);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays
                .asList(new Problem[] { new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)) }));
    }

    @Test
    public void test_validateForParsingFailed_oneWarning() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage warningMsg = mock(BuildMessage.class);
        buildMsgs.add(warningMsg);
        when(warningMsg.getType()).thenReturn(LogLevel.WARN);
        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);
        when(warningMsg.getFileRegion()).thenReturn(region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, warningMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(file, times(1)).getName();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(warningMsg, times(2)).getType();
        order.verify(warningMsg, times(1)).getMessage();
        order.verify(warningMsg, times(1)).getFileRegion();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(2);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays.asList(new Problem[] {
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))) }));
    }

    @Test
    public void test_validateForParsingFailed_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final List<BuildMessage> buildMsgs = new ArrayList<>();
        final BuildMessage errorMsg = mock(BuildMessage.class);
        buildMsgs.add(errorMsg);
        when(errorMsg.getType()).thenReturn(LogLevel.ERROR);
        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);
        when(errorMsg.getFileRegion()).thenReturn(region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(buildMsgs);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file, errorMsg);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(file, times(1)).getName();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verify(errorMsg, times(1)).getType();
        order.verify(errorMsg, times(1)).getMessage();
        order.verify(errorMsg, times(1)).getFileRegion();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
        assertThat(reportedProblems).hasSize(2);
        assertThat(reportedProblems).containsOnlyElementsOf(Arrays.asList(new Problem[] {
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))) }));
    }

    @Test
    public void test_validateForParsingFailed_noExtraMessages() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        // execute
        rfv.validate(monitor);

        // validate
        final InOrder order = inOrder(suiteFile, coreModel, toUpdateMessages, file);
        order.verify(suiteFile, times(1)).getLinkedElement();
        order.verify(coreModel, times(1)).getParent();
        order.verify(toUpdateMessages, times(1)).getStatus();
        order.verify(file, times(1)).getName();
        order.verify(toUpdateMessages, times(1)).getBuildingMessages();
        order.verifyNoMoreInteractions();

        final Collection<Problem> reportedProblems = ((MockReporter) this.reporter).getReportedProblems();
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
        final RobotSuiteFile mySuiteFile = mock(RobotSuiteFile.class);

        final Optional<RobotCasesSection> cases = Optional.empty();
        when(mySuiteFile.findSection(RobotCasesSection.class)).thenReturn(cases);
        final Optional<RobotSettingsSection> settings = Optional.empty();
        when(mySuiteFile.findSection(RobotSettingsSection.class)).thenReturn(settings);
        final Optional<RobotKeywordsSection> keywords = Optional.empty();
        when(mySuiteFile.findSection(RobotKeywordsSection.class)).thenReturn(keywords);
        final Optional<RobotVariablesSection> variables = Optional.empty();
        when(mySuiteFile.findSection(RobotVariablesSection.class)).thenReturn(variables);

        return mySuiteFile;
    }

    private ValidationContext createValidationContext(final RobotModel model, final BuildLogger logger) {
        final IProject project = mock(IProject.class);
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotProjectConfig robotProjectConfig = mock(RobotProjectConfig.class);
        final RobotRuntimeEnvironment robotRuntime = mock(RobotRuntimeEnvironment.class);

        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getParent()).thenReturn(model);
        when(robotProject.getRobotProjectConfig()).thenReturn(robotProjectConfig);
        when(robotProjectConfig.isReferencedLibrariesAutoDiscoveringEnabled()).thenReturn(false);
        when(robotProject.getRuntimeEnvironment()).thenReturn(robotRuntime);
        when(robotProject.getVersion()).thenReturn("3.0");

        return new ValidationContext(robotProject, logger);
    }

    private static class MockRobotFileValidator extends RobotFileValidator {

        public MockRobotFileValidator(final ValidationContext context, final IFile file,
                final ProblemsReportingStrategy reporter) {
            super(context, file, reporter);
        }
    }
}
