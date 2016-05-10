/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class RowHeaderDataProviderTest {

    @DataPoints
    public static int[] indexes = new int[] { 0, 1, 2, 5, 10, 100, 1000 };

    @Theory
    public void rowHeadersContainsOnlyEmptyString(final int column, final int row) {
        final RowHeaderDataProvider provider = new RowHeaderDataProvider(mock(IDataProvider.class));
        assertThat(provider.getDataValue(column, row)).isEqualTo("");
    }
}
