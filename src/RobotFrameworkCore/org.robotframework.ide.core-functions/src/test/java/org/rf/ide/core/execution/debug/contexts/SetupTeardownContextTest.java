/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RobotLineBreakpoint;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

public class SetupTeardownContextTest {

    @Test
    public void setupTeardownContextIsNotErroneousWhenNoErrorMessageIsProvided() {
        final SetupTeardownContext context1 = new SetupTeardownContext(null, mock(StackFrameContext.class));
        final SetupTeardownContext context2 = new SetupTeardownContext(URI.create("file:///file.robot"), 42,
                mock(StackFrameContext.class), mock(RobotBreakpointSupplier.class));
        final SetupTeardownContext context3 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class));
        final SetupTeardownContext context4 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class), mock(RobotBreakpointSupplier.class));

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
    public void setupTeardownContextIsErroneousWhenErrorMessageIsProvided() {
        final SetupTeardownContext context1 = new SetupTeardownContext("error1", mock(StackFrameContext.class));
        final SetupTeardownContext context2 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, "error2",
                mock(StackFrameContext.class));
        final SetupTeardownContext context3 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, "error3",
                mock(StackFrameContext.class), mock(RobotBreakpointSupplier.class));

        assertThat(context1.isErroneous()).isTrue();
        assertThat(context1.getErrorMessage()).contains("error1");

        assertThat(context2.isErroneous()).isTrue();
        assertThat(context2.getErrorMessage()).contains("error2");

        assertThat(context3.isErroneous()).isTrue();
        assertThat(context3.getErrorMessage()).contains("error3");
    }

    @Test
    public void suiteTeardownContextHasNoSourceAssociatedIfNotProvided() {
        final SetupTeardownContext context1 = new SetupTeardownContext(null, mock(StackFrameContext.class));
        final SetupTeardownContext context2 = new SetupTeardownContext(null, -1, mock(StackFrameContext.class),
                mock(RobotBreakpointSupplier.class));
        final SetupTeardownContext context3 = new SetupTeardownContext(null, -1, null, mock(StackFrameContext.class));
        final SetupTeardownContext context4 = new SetupTeardownContext(null, -1, null, mock(StackFrameContext.class),
                mock(RobotBreakpointSupplier.class));

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
    public void suiteTeardownContextHasSourceAssociatedWhenProvided() {
        final SetupTeardownContext context1 = new SetupTeardownContext(URI.create("file:///file.robot"), 42,
                mock(StackFrameContext.class), mock(RobotBreakpointSupplier.class));
        final SetupTeardownContext context2 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class));
        final SetupTeardownContext context3 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class), mock(RobotBreakpointSupplier.class));

        assertThat(context1.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(context1.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));

        assertThat(context2.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(context2.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));

        assertThat(context3.getAssociatedPath()).contains(URI.create("file:///file.robot"));
        assertThat(context3.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void previousContextIsReturnedAsProvided() {
        final StackFrameContext previousContext1 = mock(StackFrameContext.class);
        final StackFrameContext previousContext2 = mock(StackFrameContext.class);
        final StackFrameContext previousContext3 = mock(StackFrameContext.class);
        final StackFrameContext previousContext4 = mock(StackFrameContext.class);

        final SetupTeardownContext context1 = new SetupTeardownContext(null, previousContext1);
        final SetupTeardownContext context2 = new SetupTeardownContext(URI.create("file:///file.robot"), 42,
                previousContext2, mock(RobotBreakpointSupplier.class));
        final SetupTeardownContext context3 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                previousContext3);
        final SetupTeardownContext context4 = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                previousContext4, mock(RobotBreakpointSupplier.class));

        assertThat(context1.previousContext()).isSameAs(previousContext1);
        assertThat(context2.previousContext()).isSameAs(previousContext2);
        assertThat(context3.previousContext()).isSameAs(previousContext3);
        assertThat(context4.previousContext()).isSameAs(previousContext4);
    }

    @Test
    public void lineBreakpointIsProvidedThroughSupplier_1() {
        final RobotBreakpointSupplier breakpointSupplier = mock(RobotBreakpointSupplier.class);
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpointSupplier.breakpointFor(URI.create("file:///file.robot"), 42))
                .thenReturn(Optional.of(breakpoint));

        final SetupTeardownContext context = new SetupTeardownContext(URI.create("file:///file.robot"), 42,
                mock(StackFrameContext.class), breakpointSupplier);

        assertThat(context.getLineBreakpoint()).contains(breakpoint);
    }

    @Test
    public void lineBreakpointIsProvidedThroughSupplier_2() {
        final RobotBreakpointSupplier breakpointSupplier = mock(RobotBreakpointSupplier.class);
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpointSupplier.breakpointFor(URI.create("file:///file.robot"), 42))
                .thenReturn(Optional.of(breakpoint));

        final SetupTeardownContext context = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class), breakpointSupplier);

        assertThat(context.getLineBreakpoint()).contains(breakpoint);
    }

    @Test
    public void noLineBreakpointIsProvided_1() {
        final SetupTeardownContext context = new SetupTeardownContext(null, mock(StackFrameContext.class));

        assertThat(context.getLineBreakpoint()).isEmpty();
    }

    @Test
    public void noLineBreakpointIsProvided_2() {
        final SetupTeardownContext context = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class));

        assertThat(context.getLineBreakpoint()).isEmpty();
    }

    @Test
    public void itIsIllegalTryingToMoveToAnthoerKeywordWhenInSetupOrTeardownContext() {
        final RobotBreakpointSupplier breakpointSupplier = mock(RobotBreakpointSupplier.class);
        final SetupTeardownContext context = new SetupTeardownContext(URI.create("file:///file.robot"), 42, null,
                mock(StackFrameContext.class), breakpointSupplier);

        for (final KeywordCallType type : EnumSet.allOf(KeywordCallType.class)) {
            assertThatExceptionOfType(IllegalDebugContextStateException.class)
                    .isThrownBy(() -> context.moveTo(new RunningKeyword("_", "_", type), breakpointSupplier))
                    .withMessage(
                            "Only single keyword can be called as setup or teardown, so it impossible to move to next one if already positioned at first one")
                    .withNoCause();
        }
    }
}
