/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lwlodarc
 *
 */
public class SettingsCommentsLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private SettingsCommentsLabelAccumulator labelAccumulator;

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new SettingsCommentsLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forNonCommentedCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("I'm not a comment!");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forCommentedCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("# <- this means I'm a comment");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }
}
