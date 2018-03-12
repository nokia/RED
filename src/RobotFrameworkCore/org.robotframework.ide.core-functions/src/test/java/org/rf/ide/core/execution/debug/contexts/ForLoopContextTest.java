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

public class ForLoopContextTest {

    @Test
    public void itIsIllegalToConstructForLoopContext_whenCurrentFrameContextIsNotExecutableOne() {
        assertThatExceptionOfType(IllegalDebugContextStateException.class)
                .isThrownBy(() -> ForLoopContext.findContextForLoop(mock(StackFrameContext.class)))
                .withMessage("For loop can only be called when already context was moved to executable call")
                .withNoCause();
    }

    @Test
    public void erroneousForLoopContextIsFound_whenCurrentFrameContextIsErroneousExecutableContext() {
        final StackFrameContext currentContext = new ErroneousExecutableCallContext(URI.create("file:///file.robot"),
                42, "error");
        final StackFrameContext forLoopContext = ForLoopContext.findContextForLoop(currentContext);

        assertThat(forLoopContext.isErroneous()).isTrue();
        assertThat(forLoopContext.getErrorMessage()).contains("error");
        assertThat(forLoopContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(forLoopContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void erroneousForLoopContextIsFound_whenCurrentFrameContextIsExecutableContextButNotOnLoopElement() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "20")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        final StackFrameContext currentContext = new ExecutableCallContext(newArrayList(model), executables, 2,
                URI.create("file:///file.robot"), 42, mock(RobotBreakpointSupplier.class));

        final StackFrameContext forLoopContext = ForLoopContext.findContextForLoop(currentContext);

        assertThat(forLoopContext.isErroneous()).isTrue();
        assertThat(forLoopContext.getErrorMessage())
                .contains("Unable to find :FOR loop\nAn executable was found calling 'log' keyword\n");
        assertThat(forLoopContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(forLoopContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }
    
    @Test
    public void validForLoopContextIsFound_whenCurrentFrameContextIsExecutableContextOnLoopElement() {
        final RobotFile model = ModelBuilder.modelForFile()
                .withTestCasesTable()
                    .withTestCase("test")
                        .executable("log", "10")
                        .executable(":FOR", "${x}", "IN", "1", "2", "3")
                        .executable("\\", "write", "${x}")
                        .executable("log", "20")
                .build();
        final List<RobotExecutableRow<TestCase>> rows = model.getTestCaseTable()
                .getTestCases()
                .get(0)
                .getExecutionContext();
        final List<ExecutableWithDescriptor> executables = ExecutablesCompiler.compileExecutables(rows, null);
        final StackFrameContext currentContext = new ExecutableCallContext(newArrayList(model), executables, 1,
                URI.create("file:///file.robot"), 42, mock(RobotBreakpointSupplier.class));

        final StackFrameContext forLoopContext = ForLoopContext.findContextForLoop(currentContext);

        assertThat(forLoopContext.isErroneous()).isFalse();
        assertThat(forLoopContext.getErrorMessage()).isEmpty();
        assertThat(forLoopContext.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(forLoopContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void forLoopContextIsNotErroneousWhenNoErrorMessageIsProvided() {
        final ForLoopContext context1 = new ForLoopContext(URI.create("file:///file.robot"), 42, null);
        final ForLoopContext context2 = new ForLoopContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"), 42, null);

        assertThat(context1.isErroneous()).isFalse();
        assertThat(context1.getErrorMessage()).isEmpty();

        assertThat(context2.isErroneous()).isFalse();
        assertThat(context2.getErrorMessage()).isEmpty();
    }

    @Test
    public void forLoopContextIsErroneousWhenErrorMessageIsProvided() {
        final ForLoopContext context1 = new ForLoopContext(URI.create("file:///file.robot"), 42, "error1");
        final ForLoopContext context2 = new ForLoopContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file.robot"), 42, "error2");

        assertThat(context1.isErroneous()).isTrue();
        assertThat(context1.getErrorMessage()).contains("error1");

        assertThat(context2.isErroneous()).isTrue();
        assertThat(context2.getErrorMessage()).contains("error2");
    }

    @Test
    public void forLoopContextHasNoSourceAssociatedIfNotProvided() {
        final ForLoopContext context1 = new ForLoopContext(null, -1, "error1");
        final ForLoopContext context2 = new ForLoopContext(mock(ExecutableWithDescriptor.class), null, -1, "error2");

        assertThat(context1.getAssociatedPath()).isEmpty();
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));

        assertThat(context2.getAssociatedPath()).isEmpty();
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void forLoopContextHasSourceAssociatedWhenProvided() {
        final ForLoopContext context1 = new ForLoopContext(URI.create("file:///file1.robot"), 42, null);
        final ForLoopContext context2 = new ForLoopContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file2.robot"), 43, null);

        assertThat(context1.getAssociatedPath()).contains(URI.create("file:///file1.robot"));
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));

        assertThat(context2.getAssociatedPath()).contains(URI.create("file:///file2.robot"));
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(43, -1, -1), new FilePosition(43, -1, -1)));
    }

    @Test
    public void currentElementIsReturnedAsProvidedViaConstructor() {
        final ExecutableWithDescriptor desc = mock(ExecutableWithDescriptor.class);

        final ForLoopContext context1 = new ForLoopContext(URI.create("file:///file1.robot"), 42, null);
        final ForLoopContext context2 = new ForLoopContext(desc, URI.create("file:///file2.robot"), 43, null);

        assertThat(context1.currentElement()).isNull();
        assertThat(context2.currentElement()).isSameAs(desc);
    }

    @Test
    public void sameContextIsReturned_whenMovingToAnyTypeOfKeyword() {
        final ForLoopContext context = new ForLoopContext(mock(ExecutableWithDescriptor.class),
                URI.create("file:///file2.robot"), 42, null);

        for (final KeywordCallType type : EnumSet.allOf(KeywordCallType.class)) {
            final StackFrameContext contextAfterMove = context.moveTo(new RunningKeyword("_", "_", type),
                    mock(RobotBreakpointSupplier.class));

            assertThat(contextAfterMove).isSameAs(context);
        }

    }
}
