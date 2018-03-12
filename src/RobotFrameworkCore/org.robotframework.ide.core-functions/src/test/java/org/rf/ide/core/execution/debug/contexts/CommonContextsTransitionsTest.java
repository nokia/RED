/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class CommonContextsTransitionsTest {

    // Only minor tests; the class itslef is tested through junits for class which uses it

    @Test
    public void exceptionIsThrown_whenTryingToMoveToGlobalTestSetupOrTeardownButKeywordIsOfOtherType() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CommonContextsTransitions.moveToTestSetupOrTeardown(newArrayList(),
                        new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), null,
                        mock(RobotBreakpointSupplier.class)));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CommonContextsTransitions.moveToTestSetupOrTeardown(newArrayList(),
                        new RunningKeyword("lib", "kw", KeywordCallType.FOR), null,
                        mock(RobotBreakpointSupplier.class)));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CommonContextsTransitions.moveToTestSetupOrTeardown(newArrayList(),
                        new RunningKeyword("lib", "kw", KeywordCallType.FOR_ITERATION), null,
                        mock(RobotBreakpointSupplier.class)));
    }

    @Test
    public void exceptionIsThrown_whenTryingToMoveToLocalTestSetupOrTeardownButKeywordIsOfOtherType() {
        final RobotFile model = ModelBuilder.modelForFile().withTestCasesTable().withTestCase("test").build();
        final TestCase testCase = model.getTestCaseTable().getTestCases().get(0);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> CommonContextsTransitions.moveToTestSetupOrTeardown(testCase, newArrayList(),
                        new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL), null,
                        mock(RobotBreakpointSupplier.class)));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CommonContextsTransitions.moveToTestSetupOrTeardown(testCase, newArrayList(),
                        new RunningKeyword("lib", "kw", KeywordCallType.FOR), null,
                        mock(RobotBreakpointSupplier.class)));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CommonContextsTransitions.moveToTestSetupOrTeardown(testCase, newArrayList(),
                        new RunningKeyword("lib", "kw", KeywordCallType.FOR_ITERATION), null,
                        mock(RobotBreakpointSupplier.class)));
    }
}
