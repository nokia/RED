/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType.KeywordsTypesFixer;
import org.rf.ide.core.execution.debug.KeywordCallType.KeywordsTypesForRf29Fixer;
import org.rf.ide.core.execution.debug.contexts.ExecutableCallContext;

public class KeywordCallTypeTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTypeIsNotRecognized() {
        KeywordCallType.from("some unrecognized type");
    }

    @Test
    public void allRf3TypesAreCorrectlyTranslated() {
        assertThat(KeywordCallType.from("keyword")).isEqualTo(KeywordCallType.NORMAL_CALL);
        assertThat(KeywordCallType.from("setup")).isEqualTo(KeywordCallType.SETUP);
        assertThat(KeywordCallType.from("teardown")).isEqualTo(KeywordCallType.TEARDOWN);
        assertThat(KeywordCallType.from("for")).isEqualTo(KeywordCallType.FOR);
        assertThat(KeywordCallType.from("for item")).isEqualTo(KeywordCallType.FOR_ITERATION);
    }

    @Test
    public void allPreRf3TypesAreCorrectlyTranslated() {
        assertThat(KeywordCallType.from("keyword")).isEqualTo(KeywordCallType.NORMAL_CALL);
        assertThat(KeywordCallType.from("suite setup")).isEqualTo(KeywordCallType.SETUP);
        assertThat(KeywordCallType.from("test setup")).isEqualTo(KeywordCallType.SETUP);
        assertThat(KeywordCallType.from("suite teardown")).isEqualTo(KeywordCallType.TEARDOWN);
        assertThat(KeywordCallType.from("test teardown")).isEqualTo(KeywordCallType.TEARDOWN);
        assertThat(KeywordCallType.from("test for")).isEqualTo(KeywordCallType.FOR);
        assertThat(KeywordCallType.from("test foritem")).isEqualTo(KeywordCallType.FOR_ITERATION);
    }

    @Test
    public void defaultFixerJustReturnsGivenKeyword() {
        final KeywordsTypesFixer fixer = new KeywordsTypesFixer();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), null))
                .isEqualTo(keyword("kw", KeywordCallType.SETUP));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.SETUP));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), null))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), null))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${x} IN RANGE [0 | 1]", KeywordCallType.FOR), null))
                .isEqualTo(keyword("${x} IN RANGE [0 | 1]", KeywordCallType.FOR));
        assertThat(fixer.keywordStarted(keyword("${x} IN RANGE [0 | 1]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${x} IN RANGE [0 | 1]", KeywordCallType.FOR));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), null))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();
    }
    @Test
    public void preRf3FixerReturnsProperTypes_whenTopLevelKeywordIsCalled() {
        final StackFrameContext context = null;

        final KeywordsTypesForRf29Fixer fixer = new KeywordsTypesForRf29Fixer();

        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context))
                .isEqualTo(keyword("kw", KeywordCallType.SETUP));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.SETUP));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR), context))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        assertThat(fixer.keywordStarted(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        fixer.keywordEnded();
    }

    @Test
    public void preRf3FixerReturnsProperTypes_whenForLoopWasPreviouslyCalled() {
        final StackFrameContext context = null;

        final KeywordsTypesForRf29Fixer fixer = new KeywordsTypesForRf29Fixer();
        fixer.keywordStarting(keyword("${x} IN [0]", KeywordCallType.FOR), context);

        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context))
                .isEqualTo(keyword("kw", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context))
                .isEqualTo(keyword("kw", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context))
                .isEqualTo(keyword("kw", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR), context))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR_ITERATION));
        assertThat(fixer.keywordStarted(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR_ITERATION));
        fixer.keywordEnded();

        fixer.keywordEnded();
    }

    @Test
    public void preRf3FixerReturnsProperTypes_whenForLoopIterationWasPreviouslyCalled() {
        final StackFrameContext context = null;

        final KeywordsTypesForRf29Fixer fixer = new KeywordsTypesForRf29Fixer();
        fixer.keywordStarting(keyword("${x} IN [0]", KeywordCallType.FOR), context);
        fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context);


        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR), context))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        fixer.keywordEnded();
    }

    @Test
    public void preRf3FixerReturnsProperTypes_whenSetupWasPreviouslyCalled() {
        final StackFrameContext context = null;
        final ExecutableCallContext executableContext1 = mock(ExecutableCallContext.class);
        when(executableContext1.isOnLastExecutable()).thenReturn(false);
        final ExecutableCallContext executableContext2 = mock(ExecutableCallContext.class);
        when(executableContext2.isOnLastExecutable()).thenReturn(true);

        final KeywordsTypesForRf29Fixer fixer = new KeywordsTypesForRf29Fixer();
        fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context);

        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR), context))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        assertThat(fixer.keywordStarted(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), executableContext1))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), executableContext2))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        fixer.keywordEnded();

        fixer.keywordEnded();
    }

    @Test
    public void preRf3FixerReturnsProperTypes_whenTeardownWasPreviouslyCalled() {
        final StackFrameContext context = null;
        final ExecutableCallContext executableContext1 = mock(ExecutableCallContext.class);
        when(executableContext1.isOnLastExecutable()).thenReturn(false);
        final ExecutableCallContext executableContext2 = mock(ExecutableCallContext.class);
        when(executableContext2.isOnLastExecutable()).thenReturn(true);

        final KeywordsTypesForRf29Fixer fixer = new KeywordsTypesForRf29Fixer();
        fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context);

        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR), context))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        assertThat(fixer.keywordStarted(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), executableContext1))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), executableContext2))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        fixer.keywordEnded();

        fixer.keywordEnded();
    }

    @Test
    public void preRf3FixerReturnsProperTypes_whenNormalTypeWasPreviouslyCalled() {
        final StackFrameContext context = null;
        final ExecutableCallContext executableContext1 = mock(ExecutableCallContext.class);
        when(executableContext1.isOnLastExecutable()).thenReturn(false);
        final ExecutableCallContext executableContext2 = mock(ExecutableCallContext.class);
        when(executableContext2.isOnLastExecutable()).thenReturn(true);

        final KeywordsTypesForRf29Fixer fixer = new KeywordsTypesForRf29Fixer();
        fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context);

        assertThat(fixer.keywordStarting(keyword("${x} = 0", KeywordCallType.FOR_ITERATION), context))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("${x} = 0", KeywordCallType.FOR_ITERATION)))
                .isEqualTo(keyword("${x} = 0", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.NORMAL_CALL), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.NORMAL_CALL)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.SETUP), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.SETUP)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), context))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR), context))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        assertThat(fixer.keywordStarted(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR)))
                .isEqualTo(keyword("${y} IN RANGE [0 | 2]", KeywordCallType.FOR));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), executableContext1))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.NORMAL_CALL));
        fixer.keywordEnded();

        assertThat(fixer.keywordStarting(keyword("kw", KeywordCallType.TEARDOWN), executableContext2))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        assertThat(fixer.keywordStarted(keyword("kw", KeywordCallType.TEARDOWN)))
                .isEqualTo(keyword("kw", KeywordCallType.TEARDOWN));
        fixer.keywordEnded();

        fixer.keywordEnded();
    }

    private static RunningKeyword keyword(final String name, final KeywordCallType type) {
        return new RunningKeyword("", name, type);
    }
}
