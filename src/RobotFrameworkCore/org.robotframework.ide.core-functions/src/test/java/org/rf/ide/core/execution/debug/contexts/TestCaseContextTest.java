/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseContextTest {

    @Test
    public void testCaseContextHasNoSourceAssociatedIfNotProvided() {
        final TestCase test = new TestCase(RobotToken.create("test"));

        final TestCaseContext context1 = new TestCaseContext("error");
        final TestCaseContext context2 = new TestCaseContext(newArrayList(), null, -1, "error");
        final TestCaseContext context3 = new TestCaseContext(test, null, newArrayList(), "template");
        final TestCaseContext context4 = new TestCaseContext(test, null, newArrayList(), "template", "error");

        assertThat(context1.getAssociatedPath()).isEmpty();
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context2.getAssociatedPath()).isEmpty();
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context3.getAssociatedPath()).isEmpty();
        assertThat(context3.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context4.getAssociatedPath()).isEmpty();
        assertThat(context4.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void testCaseContextHasSourceAssociatedWhenProvided() {
        final TestCase test = new TestCase(RobotToken.create("test"));

        final TestCaseContext context1 = new TestCaseContext(newArrayList(), URI.create("file:///file1.robot"), 42,
                "error");
        final TestCaseContext context2 = new TestCaseContext(test, URI.create("file:///file2.robot"), newArrayList(),
                "template");
        final TestCaseContext context3 = new TestCaseContext(test, URI.create("file:///file3.robot"), newArrayList(),
                "template", "error");

        assertThat(context1.getAssociatedPath()).contains(URI.create("file:///file1.robot"));
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));

        assertThat(context2.getAssociatedPath()).contains(URI.create("file:///file2.robot"));
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context3.getAssociatedPath()).contains(URI.create("file:///file3.robot"));
        assertThat(context3.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void testCaseContextIsNotErroneousWhenNoErrorMessageIsProvided() {
        final TestCase test = new TestCase(RobotToken.create("test"));

        final TestCaseContext context1 = new TestCaseContext(null);
        final TestCaseContext context2 = new TestCaseContext(newArrayList(), null, -1, null);
        final TestCaseContext context3 = new TestCaseContext(test, null, newArrayList(), "template");
        final TestCaseContext context4 = new TestCaseContext(test, null, newArrayList(), "template", null);

        assertThat(context1.isErroneous()).isFalse();
        assertThat(context1.getErrorMessage()).isEmpty();

        assertThat(context2.isErroneous()).isFalse();
        assertThat(context2.getErrorMessage()).isEmpty();

        assertThat(context3.isErroneous()).isFalse();
        assertThat(context3.getErrorMessage()).isEmpty();

        assertThat(context4.isErroneous()).isFalse();
        assertThat(context4.getErrorMessage()).isEmpty();
    }

    @Test
    public void testCaseContextIsErroneousWhenErrorMessageIsProvided() {
        final TestCase test = new TestCase(RobotToken.create("test"));

        final TestCaseContext context1 = new TestCaseContext("error1");
        final TestCaseContext context2 = new TestCaseContext(newArrayList(), null, -1, "error2");
        final TestCaseContext context3 = new TestCaseContext(test, null, newArrayList(), "template", "error3");

        assertThat(context1.isErroneous()).isTrue();
        assertThat(context1.getErrorMessage()).contains("error1");

        assertThat(context2.isErroneous()).isTrue();
        assertThat(context2.getErrorMessage()).contains("error2");

        assertThat(context3.isErroneous()).isTrue();
        assertThat(context3.getErrorMessage()).contains("error3");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereIsNoTestCaseInContext() {
        final RobotFile model = ModelBuilder.modelForFile().build();
        final TestCaseContext context = new TestCaseContext(newArrayList(model), URI.create("file:///file.robot"), 42,
                null);

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereAreNoExecutablesInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButThereAreNoExecutablesInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of '${x} IN [ 1 | 2 | 3 ]' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereIsALoopInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "log", "${x}")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of 'lib.kw' keyword\n:FOR loop was found instead\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButThereIsANormalKeywordInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find :FOR loop\nAn executable was found calling 'log' keyword\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalKeywordButThereIsSomeOtherKeywordInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of 'lib.kw' keyword\n"
                        + "An executable was found but seem to call non-matching keyword 'log'\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButThereIsNonMatchingLoopInside() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "log", "${x}")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 | 4 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find matching :FOR loop\n"
                        + "':FOR ${x} IN [ 1 | 2 | 3 ]' was found but ':FOR ${x} IN [ 1 | 2 | 3 | 4 ]' is being executed\n");
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToNormalKeywordAndMatchingOneIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "log", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.getAssociatedPath()).contains(modelUri);

    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToForLoopAndMatchingLoopIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "log", "${x}")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("", "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.getAssociatedPath()).contains(modelUri);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToSetupButThereIsNoTestAndNoModelsToLook() {
        final TestCaseContext context = new TestCaseContext("error");
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n");
        assertThat(newContext.getAssociatedPath()).isEmpty();
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTeardownButThereIsNoTestAndNoModelsToLook() {
        final TestCaseContext context = new TestCaseContext("error");
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n");
        assertThat(newContext.getAssociatedPath()).isEmpty();
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToLocalTestSetupButThereIsEmptySetting() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                .withTestCase("test")
                .withTestSetup(null)
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToLocalTestTeardownButThereIsEmptySetting() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .withTestTeardown(null)
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToLocalTestSetupButThereIsEmptySetting_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                .       withTestSetup("")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToLocalTestTeardownButThereIsEmptySetting_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .withTestTeardown("")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToLocalTestSetupButTheSetupDoesNotMatchKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .withTestSetup("non-matching", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToLocalTestTeardownButTheTearodownDoesNotMatchKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .withTestTeardown("non-matching", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenBothTheTestCaseAndSettingTableDoesNotHaveTestSetup() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1", "2")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("kw", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenBothTheTestCaseAndSettingTableDoesNotHaveTestTeardown() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1", "2")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("kw", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validSetupContextIsReturned_whenMovingToLocalTestSetupAndMatchingOneIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .withTestSetup("kw", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToLocalTestTeardownAndMatchingOneIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .withTestTeardown("kw", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validSetupContextIsReturnedFromSettingsTable_whenMovingToLocalTestSetupButTestHaveNoLocalSetup() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1", "2")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("kw", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturnedFromSettingsTable_whenMovingToLocalTestTeardownButTestHaveNoLocalTeardown() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1", "2")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("kw", "arg")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        final TestCase test = model.getTestCaseTable().getTestCases().get(0);

        final TestCaseContext context = new TestCaseContext(test, modelUri, newArrayList(model), null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenThereIsNoTestSetupAnywhere() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenThereIsNoTestTeardownAnywhere() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToTestSetupInSettingsTableButThereIsAnEmptySetting() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup(null)
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownInSettingsTableButThereIsAnEmptySetting() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown(null)
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToTestSetupInSettingsTableButThereIsAnEmptySetting_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownInSettingsTableButThereIsAnEmptySetting_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToTestSetupButTheFoundOneDoesNotMatch() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("non-matching", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButTheFoundOneDoesNotMatch() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("non-matching", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validSetupContextIsReturned_whenMovingToTestSetupAndMatchingCallIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingCallIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validSetupContextIsReturned_whenMovingToTestSetupAndMatchingCallIsFound_withMultipleParents() {
        final RobotFile model1 = ModelBuilder.modelForFile().build();
        final RobotFile model2 = ModelBuilder.modelForFile().build();
        final RobotFile model3 = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1")
                .build();
        final URI modelUri = model3.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model1, model2, model3), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingCallIsFound_withMultipleParents() {
        final RobotFile model1 = ModelBuilder.modelForFile().build();
        final RobotFile model2 = ModelBuilder.modelForFile().build();
        final RobotFile model3 = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .build();
        final URI modelUri = model3.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model1, model2, model3), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenMovingToTestSetupAndMatchingCallIsOverridenWithNonMatchingInFileUnder() {
        final RobotFile model1 = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("non-matching", "2")
                .build();
        final RobotFile model2 = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1")
                .build();
        final URI modelUri = model2.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model1, model2), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Setup call of 'lib.kw' keyword\n"
                + "Test Setup setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingCallIsOverridenWithNonMatchingInFileUnder() {
        final RobotFile model1 = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("non-matching", "2")
                .build();
        final RobotFile model2 = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .build();
        final URI modelUri = model2.getParent().getProcessedFile().toURI();

        final TestCaseContext context = new TestCaseContext(newArrayList(model1, model2), modelUri, 42, null);
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }
}
