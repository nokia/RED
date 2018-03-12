/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RobotLineBreakpoint;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class ExecutableCallContextTest {

    @Test
    public void executableContextIsNotErroneousWhenNoErrorMessageIsProvided() {
        final ExecutableCallContext context1 = new ExecutableCallContext(newArrayList(), newArrayList(), 0,
                URI.create("file:///file.robot"), 1, mock(RobotBreakpointSupplier.class));
        final ExecutableCallContext context2 = new ExecutableCallContext(newArrayList(), newArrayList(), 0,
                URI.create("file:///file.robot"), 1, null, mock(RobotBreakpointSupplier.class));

        assertThat(context1.isErroneous()).isFalse();
        assertThat(context1.getErrorMessage()).isEmpty();

        assertThat(context2.isErroneous()).isFalse();
        assertThat(context2.getErrorMessage()).isEmpty();
    }

    @Test
    public void executableContextIsErroneousWhenErrorMessageIsProvided() {
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), newArrayList(), 0,
                URI.create("file:///file.robot"), 1, "error", mock(RobotBreakpointSupplier.class));

        assertThat(context.isErroneous()).isTrue();
        assertThat(context.getErrorMessage()).contains("error");
    }

    @Test
    public void executableContextHasNoSourceAssociatedIfNotProvided() {
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), newArrayList(), 0,
                null, -1, mock(RobotBreakpointSupplier.class));

        assertThat(context.getAssociatedPath()).isEmpty();
        assertThat(context.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void executableContextHasSourceAssociatedWhenProvided() {
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), newArrayList(), 0,
                URI.create("file:///file.robot"), 42, mock(RobotBreakpointSupplier.class));

        assertThat(context.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(context.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void lineBreakpointIsProvidedThroughSupplier() {
        final RobotBreakpointSupplier breakpointSupplier = mock(RobotBreakpointSupplier.class);
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpointSupplier.breakpointFor(URI.create("file:///file.robot"), 42))
                .thenReturn(Optional.of(breakpoint));

        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), newArrayList(), 0,
                URI.create("file:///file.robot"), 42, breakpointSupplier);

        assertThat(context.getLineBreakpoint()).contains(breakpoint);
    }

    @Test
    public void contextIsOnLastExecutableCheck() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("log", "20")
                        .executable("log", "30")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 2,
                URI.create("file:///file.robot"), 42, mock(RobotBreakpointSupplier.class));
        
        assertThat(context.isOnLastExecutable()).isTrue();
    }

    @Test
    public void contextIsNotOnLastExecutableCheck() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("log", "20")
                        .executable("log", "30")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 1,
                URI.create("file:///file.robot"), 42, mock(RobotBreakpointSupplier.class));
        
        assertThat(context.isOnLastExecutable()).isFalse();
    }

    @Test
    public void itIsIllegalToMoveToSetupWhenAlreadyInExecutableContext_insideTestCase() throws Exception {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);
        
        assertThatExceptionOfType(IllegalDebugContextStateException.class)
                .isThrownBy(() -> context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP), bpSupplier))
                .withMessage(
                        "Setup keyword cannot be called when already executing keywords inside test case or other keyword")
                .withNoCause();
    }

    @Test
    public void itIsIllegalToMoveToSetupWhenAlreadyInExecutableContext_insideUserKeyword() throws Exception {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable("log", "20")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);
        
        assertThatExceptionOfType(IllegalDebugContextStateException.class)
                .isThrownBy(() -> context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP), bpSupplier))
                .withMessage(
                        "Setup keyword cannot be called when already executing keywords inside test case or other keyword")
                .withNoCause();
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingOutsideTheExecutablesListOfTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("keyword", "${x}", "${y}")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        
        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 1,
                URI.create("file:///file.robot"), 42, bpSupplier);
        
        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingOutsideTheExecutablesListOfUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable("keyword", "${x}", "${y}")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        
        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 1,
                URI.create("file:///file.robot"), 42, bpSupplier);
        
        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
    }

    @Test
    public void erreneousExecutableContextIsReturned_whenMovingToForLoopButDifferentLoopIsFoundInsideTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("", "${y} IN [ 1 | 2 | 3 | 4 ]", KeywordCallType.FOR), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).contains("Unable to find matching :FOR loop\n"
                + "':FOR ${x} IN [ 1 | 2 | 3 ]' was found but ':FOR ${y} IN [ 1 | 2 | 3 | 4 ]' is being executed\n");
    }

    @Test
    public void erreneousExecutableContextIsReturned_whenMovingToForLoopButDifferentLoopIsFoundInsideUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("", "${y} IN [ 1 | 2 | 3 | 4 ]", KeywordCallType.FOR), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).contains("Unable to find matching :FOR loop\n"
                + "':FOR ${x} IN [ 1 | 2 | 3 ]' was found but ':FOR ${y} IN [ 1 | 2 | 3 | 4 ]' is being executed\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToOrdinaryKeywordButLoopIsFoundInsideTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of 'lib.kw' keyword\n:FOR loop was found instead\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToOrdinaryKeywordButLoopIsFoundInsideUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find executable call of 'lib.kw' keyword\n:FOR loop was found instead\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButOrdinaryCallIsFoundInsideTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        
        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword(null, "${y} IN [ 1 | 2 | 3 | 4 ]", KeywordCallType.FOR), bpSupplier);
        
        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find :FOR loop\nAn executable was found calling 'log' keyword\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToForLoopButOrdinaryCallIsFoundInsideUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        
        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword(null, "${y} IN [ 1 | 2 | 3 | 4 ]", KeywordCallType.FOR), bpSupplier);
        
        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage())
                .contains("Unable to find :FOR loop\nAn executable was found calling 'log' keyword\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToOrdinaryExecutableButNonMatchingExecutableIsFoundInsideTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n"
                + "An executable was found but seem to call non-matching keyword 'log'\n");
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToOrdinaryExecutableButNonMatchingExecutableIsFoundInsideUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n"
                + "An executable was found but seem to call non-matching keyword 'log'\n");
    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToOrdinaryExecutableAndMatchingOneIsFoundInsideTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable("write", "${x}")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "write", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).isEmpty();
    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToOrdinaryExecutableAndMatchingOneIsFoundInsideUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable("write", "${x}")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "write", KeywordCallType.NORMAL_CALL), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).isEmpty();
    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToForLoopAndMatchingOneIsFoundInsideTestCase() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword(null, "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).isEmpty();
    }

    @Test
    public void validExecutableContextIsReturned_whenMovingToForLoopAndMatchingOneIsFoundInsideUserKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword(null, "${x} IN [ 1 | 2 | 3 ]", KeywordCallType.FOR), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(newContext.getErrorMessage()).isEmpty();
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButThereIsEmptySetting() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .withTestTeardown(null)
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToKeywordTeardownButThereIsEmptySetting() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .withKeywordTeardown(null)
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Keyword Teardown call of 'lib.kw' keyword\n"
                + "Keyword Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButThereIsEmptySetting_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .withTestTeardown("")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToKeywordTeardownButThereIsEmptySetting_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .withKeywordTeardown("")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Keyword Teardown call of 'lib.kw' keyword\n"
                + "Keyword Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButTheTeardownDoesNotMatchKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .withTestTeardown("non-matching", "arg")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToKeywordTeardownButTheTeardownDoesNotMatchKeyword() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .withKeywordTeardown("non-matching", "arg")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Keyword Teardown call of 'lib.kw' keyword\n"
                + "Keyword Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }
    
    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButItsNotDefinedLocallyOrInSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1", "2")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("kw", "arg")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }
    
    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToKeywordTeardownButItsNotDefinedLocallyOrInSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestSetup("kw", "1", "2")
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("kw", "arg")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Keyword Teardown call of 'lib.kw' keyword\n"
                + "Keyword Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingOneIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .withTestTeardown("kw", "arg")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToKeywordTeardownAndMatchingOneIsFound() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withKeywordsTable()
                    .withUserKeyword("keyword")
                        .executable("log", "10")
                        .withKeywordTeardown("kw", "arg")
                .build();
        final List<RobotExecutableRow<UserKeyword>> rows = model.getKeywordTable()
                .getKeywords()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButThereIsNoLocalSettingOrInSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButThereIsEmptySettingInSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown(null)
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownButThereIsEmptySettingInSettingsTable_2() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownAndSettingInSettingsTableDoesNotMatch() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("non-matching", "x")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingCallIsFoundInSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingCallIsFoundInParentSettingsTable() {
        final RobotFile parentModel = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .build();
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model, parentModel), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenMovingToTestTeardownAndMatchingCallIsOverridenWithNonMatchingInFileUnder() {
        final RobotFile parentModel = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1")
                .build();
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("non-matching", "1")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model, parentModel), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);
        
        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Test Teardown call of 'lib.kw' keyword\n"
                + "Test Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturnedFromSettingsTable_whenMovingToTestTeardownAndOneIsFoundInSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withSettingsTable()
                    .withTestTeardown("kw", "1", "2")
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("kw", "arg")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);

        final RobotBreakpointSupplier bpSupplier = mock(RobotBreakpointSupplier.class);
        final ExecutableCallContext context = new ExecutableCallContext(newArrayList(model), executables, 0,
                URI.create("file:///file.robot"), 42, bpSupplier);

        final StackFrameContext newContext = context
                .moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN), bpSupplier);

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }
}
