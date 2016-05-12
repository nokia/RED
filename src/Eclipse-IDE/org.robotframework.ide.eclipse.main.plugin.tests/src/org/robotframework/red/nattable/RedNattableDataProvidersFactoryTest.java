/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class RedNattableDataProvidersFactoryTest {

    @DataPoints
    public static int[] indexes = new int[] { 0, 1, 2, 5, 10, 100, 1000 };

    @DataPoints
    public static String[][] labels = new String[][] { new String[] { "a", "b" }, new String[] { "a", "", "", "c" },
            new String[] { "a" }, };

    @Theory
    public void rowHeadersContainsOnlyEmptyString(final int column, final int row) {
        final RedNattableDataProvidersFactory factory = new RedNattableDataProvidersFactory();

        final IDataProvider provider = factory.createRowHeaderDataProvider(mock(IDataProvider.class));
        assertThat(provider.getDataValue(column, row)).isEqualTo("");
    }

    @Theory
    public void columnHeadersContainsGivenLabels(final String[] labels) {
        final RedNattableDataProvidersFactory factory = new RedNattableDataProvidersFactory();

        final IDataProvider provider = factory.createColumnHeaderDataProvider(labels);

        int column = 0;
        for (final String label : labels) {
            assertThat(provider.getDataValue(column, 0)).isEqualTo(label);
            column++;
        }
    }
}
