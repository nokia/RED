/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.junit.jupiter.api.Test;

public class RedNattableDataProvidersFactoryTest {

    @Test
    public void rowHeadersContainsOnlyEmptyString() {
        final RedNattableDataProvidersFactory factory = new RedNattableDataProvidersFactory();
        final int[] indexes = new int[] { 0, 1, 2, 5, 10, 100, 1000 };

        final IDataProvider provider = factory.createRowHeaderDataProvider(mock(IDataProvider.class));

        for (int columnIndex = 0; columnIndex < indexes.length; columnIndex++) {
            for (int rowIndex = 0; rowIndex < indexes.length; rowIndex++) {
                assertThat(provider.getDataValue(indexes[columnIndex], indexes[rowIndex])).isEqualTo("");
            }
        }
    }

    @Test
    public void columnHeadersContainsGivenLabels() {
        final RedNattableDataProvidersFactory factory = new RedNattableDataProvidersFactory();
        final String[][] headerLabels = new String[][] { new String[] { "a", "b" }, new String[] { "a", "", "", "c" },
                new String[] { "a" }, };

        for (final String[] labels : headerLabels) {
            final IDataProvider provider = factory.createColumnHeaderDataProvider(labels);

            for (int columnIndex = 0; columnIndex < labels.length; columnIndex++) {
                assertThat(provider.getDataValue(columnIndex, 0)).isEqualTo(labels[columnIndex]);
            }
        }
    }
}
