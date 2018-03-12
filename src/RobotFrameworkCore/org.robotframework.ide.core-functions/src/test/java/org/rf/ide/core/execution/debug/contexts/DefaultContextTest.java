/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;

public class DefaultContextTest {

    @Test
    public void defaultContextHasNoErrors() {
        assertThat(defaultContext().isErroneous()).isFalse();
        assertThat(defaultContext().getErrorMessage()).isEmpty();
    }

    @Test
    public void defaultContextHasNoAssociatedSource() {
        assertThat(defaultContext().getAssociatedPath()).isEmpty();
        assertThat(defaultContext().getFileRegion()).isEmpty();
    }

    @Test
    public void defaultContextHasNoAssociatedBreakpoint() {
        assertThat(defaultContext().getLineBreakpoint()).isEmpty();
    }

    @Test
    public void defaultContextReturnsItselfAsPreviousContext() {
        final DefaultContext defaultContext = defaultContext();
        assertThat(defaultContext.previousContext()).isSameAs(defaultContext);
    }

    private static DefaultContext defaultContext() {
        return new DefaultContext() {

            @Override
            public StackFrameContext moveTo(final RunningKeyword keyword,
                    final RobotBreakpointSupplier breakpointSupplier) {
                return null;
            }
        };
    }
}
