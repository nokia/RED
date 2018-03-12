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
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForLoopIterationContextTest {

    @Test
    public void itIsIllegalToConstructForLoopContext_whenCurrentFrameContextIsNotForLoopContext() {
        assertThatExceptionOfType(IllegalDebugContextStateException.class)
                .isThrownBy(
                        () -> ForLoopIterationContext.findContextForLoopIteration(mock(StackFrameContext.class),
                                "${x} = 1"))
                .withMessage("For loop iteration can only be called when already context was moved to for-loop context")
                .withNoCause();
    }

    @Test
    public void erroneousLoopIterationContextIsFound_whenLoopContextWasAlreadyErroneous() {
        final StackFrameContext currentContext = new ForLoopContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"), 42, "error");
        final StackFrameContext loopIterationContext = ForLoopIterationContext
                .findContextForLoopIteration(currentContext, "${x} = 1");

        assertThat(loopIterationContext.isErroneous()).isTrue();
        assertThat(loopIterationContext.getErrorMessage()).contains("No loop found for iteration of '${x} = 1'\n");
        assertThat(loopIterationContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(loopIterationContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void erroneousLoopIterationContextIsFound_whenVariablesInUseAreDifferentThanThoseExpected_1() {
        final ExecutableWithDescriptor descriptor = mock(ExecutableWithDescriptor.class);
        when(descriptor.getForVariables()).thenReturn(newArrayList(RobotToken.create("${y}")));
        when(descriptor.getForVariablesRegion())
                .thenReturn(new FileRegion(new FilePosition(42, 7, 50), new FilePosition(42, 10, 53)));

        final StackFrameContext currentContext = new ForLoopContext(descriptor,
                URI.create("file:///file.robot"), 42, null);
        final StackFrameContext loopIterationContext = ForLoopIterationContext
                .findContextForLoopIteration(currentContext, "${x} = 1");

        assertThat(loopIterationContext.isErroneous()).isTrue();
        assertThat(loopIterationContext.getErrorMessage())
                .contains("The loop is iterating with [${x}] variables but [${y}] were expected\n");
        assertThat(loopIterationContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(loopIterationContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, 7, 50), new FilePosition(42, 10, 53)));
    }

    @Test
    public void erroneousLoopIterationContextIsFound_whenVariablesInUseAreDifferentThanThoseExpected_2() {
        final ExecutableWithDescriptor descriptor = mock(ExecutableWithDescriptor.class);
        when(descriptor.getForVariables())
                .thenReturn(newArrayList(RobotToken.create("${x}"), RobotToken.create("${y}")));
        when(descriptor.getForVariablesRegion())
                .thenReturn(new FileRegion(new FilePosition(42, 7, 50), new FilePosition(42, 10, 53)));

        final StackFrameContext currentContext = new ForLoopContext(descriptor, URI.create("file:///file.robot"), 42,
                null);
        final StackFrameContext loopIterationContext = ForLoopIterationContext
                .findContextForLoopIteration(currentContext, "${y} = 5, ${x} = 1");

        assertThat(loopIterationContext.isErroneous()).isTrue();
        assertThat(loopIterationContext.getErrorMessage())
                .contains("The loop is iterating with [${y}, ${x}] variables but [${x}, ${y}] were expected\n");
        assertThat(loopIterationContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(loopIterationContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, 7, 50), new FilePosition(42, 10, 53)));
    }

    @Test
    public void validLoopIterationContextIsFound_whenVariablesInUseAreSameAsThoseExpected() {
        final ExecutableWithDescriptor descriptor = mock(ExecutableWithDescriptor.class);
        when(descriptor.getForVariables())
                .thenReturn(newArrayList(RobotToken.create("${x}"), RobotToken.create("${y}")));
        when(descriptor.getForVariablesRegion())
                .thenReturn(new FileRegion(new FilePosition(42, 7, 50), new FilePosition(42, 10, 53)));

        final StackFrameContext currentContext = new ForLoopContext(descriptor, URI.create("file:///file.robot"), 42,
                null);
        final StackFrameContext loopIterationContext = ForLoopIterationContext
                .findContextForLoopIteration(currentContext, "${x} = 5, ${y} = 1");

        assertThat(loopIterationContext.isErroneous()).isFalse();
        assertThat(loopIterationContext.getErrorMessage()).isEmpty();
        assertThat(loopIterationContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(loopIterationContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, 7, 50), new FilePosition(42, 10, 53)));
    }

    @Test
    public void loopIterationContextIsNotErroneousWhenNoErrorMessageIsProvided() {
        final ForLoopIterationContext context1 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)));
        final ForLoopIterationContext context2 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);
        final ForLoopIterationContext context3 = new ForLoopIterationContext(URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);

        assertThat(context1.isErroneous()).isFalse();
        assertThat(context1.getErrorMessage()).isEmpty();

        assertThat(context2.isErroneous()).isFalse();
        assertThat(context2.getErrorMessage()).isEmpty();

        assertThat(context3.isErroneous()).isFalse();
        assertThat(context3.getErrorMessage()).isEmpty();
    }

    @Test
    public void forLoopContextIsErroneousWhenErrorMessageIsProvided() {
        final ForLoopIterationContext context1 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), "error1");
        final ForLoopIterationContext context2 = new ForLoopIterationContext(URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), "error2");

        assertThat(context1.isErroneous()).isTrue();
        assertThat(context1.getErrorMessage()).contains("error1");

        assertThat(context2.isErroneous()).isTrue();
        assertThat(context2.getErrorMessage()).contains("error2");
    }

    @Test
    public void loopIterationContextHasNoSourceAssociatedIfNotProvided() {
        final ForLoopIterationContext context1 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                null, new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
        final ForLoopIterationContext context2 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class), null,
                new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)), null);
        final ForLoopIterationContext context3 = new ForLoopIterationContext(null,
                new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)), null);

        assertThat(context1.getAssociatedPath()).isEmpty();
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context2.getAssociatedPath()).isEmpty();
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context3.getAssociatedPath()).isEmpty();
        assertThat(context3.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void loopIterationContextHasSourceAssociatedWhenProvided() {
        final ForLoopIterationContext context1 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file1.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)));
        final ForLoopIterationContext context2 = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file2.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);
        final ForLoopIterationContext context3 = new ForLoopIterationContext(URI.create("file:///file3.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);

        assertThat(context1.getAssociatedPath()).contains(URI.create("file:///file1.robot"));
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)));

        assertThat(context2.getAssociatedPath()).contains(URI.create("file:///file2.robot"));
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)));

        assertThat(context3.getAssociatedPath()).contains(URI.create("file:///file3.robot"));
        assertThat(context3.getFileRegion())
                .contains(new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)));
    }
    
    @Test
    public void itIsIllegalToMoveToKeywordOfTypeDifferentThanNormal() {
        final ForLoopIterationContext context = new ForLoopIterationContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);

        for (final KeywordCallType type : EnumSet.complementOf(EnumSet.of(KeywordCallType.NORMAL_CALL))) {
            assertThatExceptionOfType(IllegalDebugContextStateException.class).isThrownBy(
                    () -> context.moveTo(new RunningKeyword("lib", "kw", type), mock(RobotBreakpointSupplier.class)))
                    .withMessage("Only normal keyword can be called when executing loop")
                    .withNoCause();
        }
    }

    @Test
    public void sameContextIsReturned_whenMovingToAnyTypeOfKeywordAndThereIsNoElement() {
        final ForLoopIterationContext context = new ForLoopIterationContext(URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);

        final StackFrameContext contextAfterMove = context
                .moveTo(new RunningKeyword("_", "_", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(contextAfterMove).isSameAs(context);
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToNormalCallAndThereIsTheLoopElement() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("\\", "${y}", "read")
                        .executable("log", "20")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        final ForLoopIterationContext context = new ForLoopIterationContext(executables.get(1),
                URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n"
                + "An executable was found but seem to call non-matching keyword 'write'\n");
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
    }
    
    @Test
    public void validExecutableContextIsReturned_whenMovingToNormalCallAndThereIsTheLoopElement() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("\\", "${y}", "read")
                        .executable("log", "20")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        final ForLoopIterationContext context = new ForLoopIterationContext(executables.get(1),
                URI.create("file:///file.robot"),
                new FileRegion(new FilePosition(2, 0, 15), new FilePosition(2, 5, 20)), null);

        final StackFrameContext newContext = context.moveTo(
                new RunningKeyword("lib", "write", KeywordCallType.NORMAL_CALL), mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
    }
}
