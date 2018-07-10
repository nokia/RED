/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Range;

public class RobotFileValidatorCheckBuildMessagesTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotFileValidatorCheckBuildMessagesTest.class);

    private static IFile file;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        file = projectProvider.createFile(new Path("suite.robot"), "");
    }

    @AfterClass
    public static void afterSuite() {
        file = null;
    }

    @Test
    public void test_validateForParsingPassed_oneInfo_oneWarn_oneError() {
        final Collection<Problem> problems = validate(Status.PASSED, createBuildInfoMessage(), createBuildWarnMessage(),
                createBuildErrorMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_oneInfo() {
        final Collection<Problem> problems = validate(Status.PASSED, createBuildInfoMessage());

        assertThat(problems).isEmpty();
    }

    @Test
    public void test_validateForParsingPassed_oneWarning() {
        final Collection<Problem> problems = validate(Status.PASSED, createBuildWarnMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_oneError() {
        final Collection<Problem> problems = validate(Status.PASSED, createBuildErrorMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_noExtraMessages() {
        final Collection<Problem> problems = validate(Status.PASSED);

        assertThat(problems).isEmpty();
    }

    @Test
    public void test_validateForParsingFailed_oneInfo_oneWarn_oneError() {
        final Collection<Problem> problems = validate(Status.FAILED, createBuildInfoMessage(), createBuildWarnMessage(),
                createBuildErrorMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_oneInfo() {
        final Collection<Problem> problems = validate(Status.FAILED, createBuildInfoMessage());

        assertThat(problems).containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)));
    }

    @Test
    public void test_validateForParsingFailed_oneWarning() {
        final Collection<Problem> problems = validate(Status.FAILED, createBuildWarnMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_oneError() {
        final Collection<Problem> problems = validate(Status.FAILED, createBuildErrorMessage());

        assertThat(problems).containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_noExtraMessages() {
        final Collection<Problem> problems = validate(Status.FAILED);

        assertThat(problems).containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)));
    }

    private static Collection<Problem> validate(final Status status, final BuildMessage... messages) {
        final ValidationContext context = createValidationContext(new RobotModel(), new BuildLogger());

        final RobotSuiteFile fileModel = context.getModel().createSuiteFile(file);
        fileModel.parse();

        final RobotFileOutput fileOutput = fileModel.getLinkedElement().getParent();
        fileOutput.setStatus(status);
        for (final BuildMessage msg : messages) {
            fileOutput.addBuildMessage(msg);
        }

        final MockReporter reporter = new MockReporter();
        final MockRobotFileValidator validator = new MockRobotFileValidator(context, file, reporter);
        validator.validate();

        return reporter.getReportedProblems();
    }

    private static ValidationContext createValidationContext(final RobotModel model, final BuildLogger logger) {
        return new ValidationContext(model.createRobotProject(projectProvider.getProject()), logger);
    }

    private static BuildMessage createBuildInfoMessage() {
        final FileRegion region = new FileRegion(new FilePosition(0, 0, 0), new FilePosition(0, 0, 0));
        final BuildMessage msg = BuildMessage.createInfoMessage("info", "file");
        msg.setFileRegion(region);
        return msg;
    }

    private static BuildMessage createBuildWarnMessage() {
        final FileRegion region = new FileRegion(new FilePosition(0, 0, 0), new FilePosition(0, 0, 0));
        final BuildMessage msg = BuildMessage.createWarnMessage("warn", "file");
        msg.setFileRegion(region);
        return msg;
    }

    private static BuildMessage createBuildErrorMessage() {
        final FileRegion region = new FileRegion(new FilePosition(0, 0, 0), new FilePosition(0, 0, 0));
        final BuildMessage msg = BuildMessage.createErrorMessage("error", "file");
        msg.setFileRegion(region);
        return msg;
    }

    private static class MockRobotFileValidator extends RobotFileValidator {

        public MockRobotFileValidator(final ValidationContext context, final IFile file,
                final ValidationReportingStrategy reporter) {
            super(context, file, reporter);
        }
    }
}
