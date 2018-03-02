/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
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

    private MockReporter reporter;

    private RobotFileValidator validator;

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
        this.context = createValidationContext(model, logger);
        this.validator = new MockRobotFileValidator(context, file, reporter);
    }

    @After
    public void tearDown() {
        this.reporter = null;
        this.file = null;
        this.path = null;
        this.model = null;
        this.suiteFile = null;
        this.context = null;
        this.validator = null;
        this.logger = null;
    }

    @Test
    public void test_validateForParsingPassed_oneInfo_oneWarn_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage infoMsg = createBuildInfoMessage("info", region);
        final BuildMessage warningMsg = createBuildWarnMessage("warn", region);
        final BuildMessage errorMsg = createBuildErrorMessage("error", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(infoMsg, warningMsg, errorMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_oneInfo() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage infoMsg = createBuildInfoMessage("info", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(infoMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void test_validateForParsingPassed_oneWarning() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage warningMsg = createBuildWarnMessage("warn", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(warningMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage errorMsg = createBuildErrorMessage("error", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(errorMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_noExtraMessages() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.PASSED);

        // execute
        validator.validate(new NullProgressMonitor());

        // validate
        assertThat(reporter.getReportedProblems()).isEmpty();
    }

    @Test
    public void test_validateForParsingFailed_oneInfo_oneWarn_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage infoMsg = createBuildInfoMessage("info", region);
        final BuildMessage warningMsg = createBuildWarnMessage("warn", region);
        final BuildMessage errorMsg = createBuildErrorMessage("error", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(infoMsg, warningMsg, errorMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_oneInfo() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage infoMsg = createBuildInfoMessage("info", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(infoMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems())
                .containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)));
    }

    @Test
    public void test_validateForParsingFailed_oneWarning() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage warningMsg = createBuildWarnMessage("warn", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(warningMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_oneError() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        final FilePosition position = new FilePosition(0, 0, 0);
        final FileRegion region = new FileRegion(position, position);

        final BuildMessage errorMsg = createBuildErrorMessage("error", region);

        when(toUpdateMessages.getBuildingMessages()).thenReturn(newArrayList(errorMsg));

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems()).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_noExtraMessages() throws CoreException {
        // prepare
        when(toUpdateMessages.getStatus()).thenReturn(Status.FAILED);

        // execute
        validator.validate(new NullProgressMonitor());

        assertThat(reporter.getReportedProblems())
                .containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)));
    }

    private static RobotSuiteFile createSuiteFile() {
        return spy(new RobotSuiteFileCreator().build());
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

    private static BuildMessage createBuildInfoMessage(final String message, final FileRegion fileRegion) {
        final BuildMessage msg = BuildMessage.createInfoMessage(message, "file");
        msg.setFileRegion(fileRegion);
        return msg;
    }

    private static BuildMessage createBuildWarnMessage(final String message, final FileRegion fileRegion) {
        final BuildMessage msg = BuildMessage.createWarnMessage(message, "file");
        msg.setFileRegion(fileRegion);
        return msg;
    }

    private static BuildMessage createBuildErrorMessage(final String message, final FileRegion fileRegion) {
        final BuildMessage msg = BuildMessage.createErrorMessage(message, "file");
        msg.setFileRegion(fileRegion);
        return msg;
    }

    private static class MockRobotFileValidator extends RobotFileValidator {

        public MockRobotFileValidator(final ValidationContext context, final IFile file,
                final ValidationReportingStrategy reporter) {
            super(context, file, reporter);
        }
    }
}
