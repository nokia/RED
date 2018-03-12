/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

public class ErroneousExecutableCallContextTest {

    @Test
    public void theContextIsAlwaysErroneous() {
        assertThat(new ErroneousExecutableCallContext(null, -1, "error").isErroneous()).isTrue();
        assertThat(new ErroneousExecutableCallContext(URI.create("file:///source.robot"), -1, "error").isErroneous())
                .isTrue();
        assertThat(new ErroneousExecutableCallContext(null, 1, "error").isErroneous()).isTrue();
        assertThat(new ErroneousExecutableCallContext(URI.create("file:///source.robot"), 1, "error").isErroneous())
                .isTrue();
        assertThat(new ErroneousExecutableCallContext(null, 1, null).isErroneous()).isTrue();
        assertThat(new ErroneousExecutableCallContext(URI.create("file:///source.robot"), 1, "").isErroneous())
                .isTrue();
    }

    @Test
    public void theErrorMessageCannotBeNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ErroneousExecutableCallContext(URI.create("file:///source.robot"), 1, null)
                        .getErrorMessage());
    }

    @Test
    public void errorMessageIsReturned() {
        assertThat(new ErroneousExecutableCallContext(URI.create("file:///source.robot"), 1, "error").getErrorMessage())
                .contains("error");
    }

    @Test
    public void thereIsNoSourceOfContext_ifNotProvided() {
        assertThat(new ErroneousExecutableCallContext(null, -1, "error").getAssociatedPath()).isEmpty();
        assertThat(new ErroneousExecutableCallContext(null, -1, "error").getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
    }

    @Test
    public void sourceOfContextIsReturned_ifProvided() {
        assertThat(
                new ErroneousExecutableCallContext(URI.create("file:///source.robot"), 42, "error").getAssociatedPath())
                        .contains(URI.create("file:///source.robot"));
        assertThat(new ErroneousExecutableCallContext(URI.create("file:///source.robot"), 42, "error").getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
    }

    @Test
    public void anotherErroneousExecutableContextIsReturned_whenMovingToRunningKeyword() {
        final ErroneousExecutableCallContext context = new ErroneousExecutableCallContext(
                URI.create("file:///source.robot"), 42,
                "error");

        final RunningKeyword keyword = new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL);
        final StackFrameContext newContext = context.moveTo(keyword, mock(RobotBreakpointSupplier.class));

        assertThat(newContext).isNotSameAs(context).isInstanceOf(ErroneousExecutableCallContext.class);
        assertThat(newContext.getAssociatedPath()).contains(URI.create("file:///source.robot"));
        assertThat(newContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(42, -1, -1), new FilePosition(42, -1, -1)));
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
    }
}
