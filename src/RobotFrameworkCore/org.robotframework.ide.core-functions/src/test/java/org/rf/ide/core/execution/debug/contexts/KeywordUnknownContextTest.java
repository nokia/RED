/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

public class KeywordUnknownContextTest {

    @Test
    public void unknownKeywordContextIsErroneous() {
        assertThat(new KeywordUnknownContext(null).isErroneous()).isTrue();
        assertThat(new KeywordUnknownContext("").isErroneous()).isTrue();
        assertThat(new KeywordUnknownContext("error").isErroneous()).isTrue();
    }

    @Test
    public void theErrorMessageCannotBeNull() {
        assertThatNullPointerException().isThrownBy(() -> new KeywordUnknownContext(null).getErrorMessage());
    }

    @Test
    public void errorMessageIsReturned() {
        assertThat(new KeywordUnknownContext("error").getErrorMessage()).contains("error");
    }

    @Test
    public void thereIsNoSourceContext() {
        assertThat(new KeywordUnknownContext("error").getAssociatedPath()).isEmpty();
        assertThat(new KeywordUnknownContext("error").getFileRegion()).isEmpty();
    }

    @Test
    public void erroneousExecutableContextIsReturned_whenMovingToRunningKeyword() {
        final KeywordUnknownContext context = new KeywordUnknownContext("error");

        final RunningKeyword keyword = new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL);
        final StackFrameContext newContext = context.moveTo(keyword, mock(RobotBreakpointSupplier.class));
        
        assertThat(newContext).isInstanceOf(ErroneousExecutableCallContext.class);
        assertThat(newContext.getAssociatedPath()).isEmpty();
        assertThat(newContext.getFileRegion())
                .contains(new FileRegion(new FilePosition(-1, -1, -1), new FilePosition(-1, -1, -1)));
        assertThat(newContext.getErrorMessage()).contains("Unable to find executable call of 'lib.kw' keyword\n");
    }

}
