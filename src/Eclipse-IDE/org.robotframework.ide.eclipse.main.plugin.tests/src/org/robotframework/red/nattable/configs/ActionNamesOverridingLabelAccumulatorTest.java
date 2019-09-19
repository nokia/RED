/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;

public class ActionNamesOverridingLabelAccumulatorTest {

    @Test
    public void nothingChanges_whenThereIsNoActionNameLabel() {
        final LabelStack stack = new LabelStack();

        final KeywordUsagesFinder kwUsageFinder = mock(KeywordUsagesFinder.class);
        when(kwUsageFinder.isLibraryKeyword("kw")).thenReturn(true);

        final Function<Integer, Object> rowProvider = i -> new RobotKeywordCall(null, row("kw", "1", "2"));

        final ActionNamesOverridingLabelAccumulator accumulator = new ActionNamesOverridingLabelAccumulator(rowProvider,
                kwUsageFinder);

        accumulator.accumulateConfigLabels(stack, 0, 1);

        assertThat(stack.getLabels()).isEmpty();
    }

    @Test
    public void nothingChanges_whenProvidedRowObjectIsNotACall() {
        final LabelStack stack = new LabelStack(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final KeywordUsagesFinder kwUsageFinder = mock(KeywordUsagesFinder.class);
        when(kwUsageFinder.isLibraryKeyword("kw")).thenReturn(true);

        final Function<Integer, Object> rowProvider = i -> new Object();

        final ActionNamesOverridingLabelAccumulator accumulator = new ActionNamesOverridingLabelAccumulator(rowProvider,
                kwUsageFinder);

        accumulator.accumulateConfigLabels(stack, 0, 1);

        assertThat(stack.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void nothingChanges_whenColumnPositionIsOutsideOfTokensList() {
        final LabelStack stack = new LabelStack(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final KeywordUsagesFinder kwUsageFinder = mock(KeywordUsagesFinder.class);
        when(kwUsageFinder.isLibraryKeyword("kw")).thenReturn(true);

        final Function<Integer, Object> rowProvider = i -> new RobotKeywordCall(null, row("kw", "1", "2"));

        final ActionNamesOverridingLabelAccumulator accumulator = new ActionNamesOverridingLabelAccumulator(rowProvider,
                kwUsageFinder);

        accumulator.accumulateConfigLabels(stack, 5, 1);

        assertThat(stack.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void nothingChanges_whenGivenKeywordIsNotALibraryKeyword() {
        final LabelStack stack = new LabelStack(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final KeywordUsagesFinder kwUsageFinder = mock(KeywordUsagesFinder.class);
        when(kwUsageFinder.isLibraryKeyword("kw")).thenReturn(false);

        final Function<Integer, Object> rowProvider = i -> new RobotKeywordCall(null, row("kw", "1", "2"));

        final ActionNamesOverridingLabelAccumulator accumulator = new ActionNamesOverridingLabelAccumulator(rowProvider,
                kwUsageFinder);

        accumulator.accumulateConfigLabels(stack, 0, 1);

        assertThat(stack.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void actionNameLabelIsReplaced_whenGivenKeywordIsALibraryKeyword() {
        final LabelStack stack = new LabelStack(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final KeywordUsagesFinder kwUsageFinder = mock(KeywordUsagesFinder.class);
        when(kwUsageFinder.isLibraryKeyword("kw")).thenReturn(true);

        final Function<Integer, Object> rowProvider = i -> new RobotKeywordCall(null, row("kw", "1", "2"));

        final ActionNamesOverridingLabelAccumulator accumulator = new ActionNamesOverridingLabelAccumulator(rowProvider,
                kwUsageFinder);

        accumulator.accumulateConfigLabels(stack, 0, 1);

        assertThat(stack.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_FROM_LIB_NAME_CONFIG_LABEL);
    }

    private static RobotExecutableRow<Object> row(final String... tokens) {
        final RobotExecutableRow<Object> row = new RobotExecutableRow<>();
        for (int i = 0; i < tokens.length; i++) {
            if (i == 0) {
                row.setAction(RobotToken.create(tokens[i]));
            } else {
                row.addArgument(RobotToken.create(tokens[i]));
            }
        }
        return row;
    }

}
