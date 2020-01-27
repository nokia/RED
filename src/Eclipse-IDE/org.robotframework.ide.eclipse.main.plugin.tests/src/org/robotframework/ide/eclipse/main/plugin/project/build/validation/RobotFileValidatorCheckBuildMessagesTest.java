/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.Range;

@ExtendWith(ProjectExtension.class)
public class RobotFileValidatorCheckBuildMessagesTest {

    @Project
    static IProject project;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "error.py", "syntax");
        createFile(project, "suite.robot");
        createFile(project, "suite_with_vars.robot", "***Settings***", "Variables  error.py");
    }

    @Test
    public void test_validateForParsingPassed_oneInfo_oneWarn_oneError() {
        final Collection<Problem> problems = validate("suite.robot", Status.PASSED, createBuildWarnMessage(),
                createBuildErrorMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_oneWarning() {
        final Collection<Problem> problems = validate("suite.robot", Status.PASSED, createBuildWarnMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_oneError() {
        final Collection<Problem> problems = validate("suite.robot", Status.PASSED, createBuildErrorMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingPassed_noExtraMessages() {
        final Collection<Problem> problems = validate("suite.robot", Status.PASSED);

        assertThat(problems).isEmpty();
    }

    @Test
    public void test_validateForParsingFailed_oneInfo_oneWarn_oneError() {
        final Collection<Problem> problems = validate("suite.robot", Status.FAILED, createBuildWarnMessage(),
                createBuildErrorMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_oneWarning() {
        final Collection<Problem> problems = validate("suite.robot", Status.FAILED, createBuildWarnMessage());

        assertThat(problems).containsOnly(
                new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_WARNING_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_oneError() {
        final Collection<Problem> problems = validate("suite.robot", Status.FAILED, createBuildErrorMessage());

        assertThat(problems).containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(0, Range.closed(0, 0))));
    }

    @Test
    public void test_validateForParsingFailed_noExtraMessages() {
        final Collection<Problem> problems = validate("suite.robot", Status.FAILED);

        assertThat(problems).containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)));
    }

    @Test
    public void test_validateForParsingFailed_oneError_whenVariableImportFails() {
        final Collection<Problem> problems = validate("suite_with_vars.robot", Status.FAILED);

        assertThat(problems).containsOnly(new Problem(SuiteFileProblem.FILE_PARSING_FAILED, new ProblemPosition(-1)),
                new Problem(SuiteFileProblem.BUILD_ERROR_MESSAGE, new ProblemPosition(2, Range.closed(26, 34))));
    }

    private static Collection<Problem> validate(final String filePath, final Status status,
            final BuildMessage... messages) {
        final ValidationContext context = createValidationContext(new RobotModel(), new BuildLogger());

        final IFile file = getFile(project, filePath);
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
        return new ValidationContext(model.createRobotProject(project), logger);
    }

    private static BuildMessage createBuildWarnMessage() {
        final FileRegion region = new FileRegion(new FilePosition(0, 0, 0), new FilePosition(0, 0, 0));
        return BuildMessage.createWarnMessage("warn", region);
    }

    private static BuildMessage createBuildErrorMessage() {
        final FileRegion region = new FileRegion(new FilePosition(0, 0, 0), new FilePosition(0, 0, 0));
        return BuildMessage.createErrorMessage("error", region);
    }

    private static class MockRobotFileValidator extends RobotFileValidator {

        public MockRobotFileValidator(final ValidationContext context, final IFile file,
                final ValidationReportingStrategy reporter) {
            super(context, file, reporter);
        }
    }
}
