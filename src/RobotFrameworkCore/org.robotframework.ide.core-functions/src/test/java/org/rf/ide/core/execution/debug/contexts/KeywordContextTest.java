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

public class KeywordContextTest {

    @Test
    public void defaultKeywordContext_isNotTheLibraryKeywordContext() {
        final KeywordContext context = keywordContext();

        assertThat(context.isLibraryKeywordContext()).isFalse();
    }

    private static KeywordContext keywordContext() {
        return new KeywordContext() {

            @Override
            public StackFrameContext moveTo(final RunningKeyword keyword,
                    final RobotBreakpointSupplier breakpointSupplier) {
                return null;
            }
        };
    }

}
