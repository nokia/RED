/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.graphics.Point;
import org.junit.Test;

public class TableCellsStringsTest {

    @Test
    public void whenTableStringsIsCreated_itHasNoStringsCached() {
        final TableCellsStrings strings = new TableCellsStrings();
        assertThat(strings.getStringsMapping()).isEmpty();
    }

    @Test
    public void addedTextDataAreProperlyCached() {
        final TableCellsStrings strings = new TableCellsStrings();

        strings.put(0, 0, new TableCellStringData("abc", new Point(50, 60), new Point(15, 20)));
        strings.put(1, 0, new TableCellStringData("xyz", new Point(100, 60), new Point(15, 20)));

        assertThat(strings.getStringsMapping()).hasSize(2);
        assertThat(strings.get(0, 0)).isEqualTo(new TableCellStringData("abc", new Point(50, 60), new Point(15, 20)));
        assertThat(strings.get(1, 0)).isEqualTo(new TableCellStringData("xyz", new Point(100, 60), new Point(15, 20)));
    }

    @Test
    public void whenThereIsAlreadyDataCached_theDataIsNotExchangedButRewritten() {
        final TableCellsStrings strings = new TableCellsStrings();

        strings.put(0, 0, new TableCellStringData("abc", new Point(50, 60), new Point(15, 20)));

        assertThat(strings.get(0, 0)).isEqualTo(new TableCellStringData("abc", new Point(50, 60), new Point(15, 20)));
        final TableCellStringData oldData = strings.get(0, 0);

        strings.put(0, 0, new TableCellStringData("abcdef", new Point(55, 65), new Point(17, 40)));
        
        assertThat(strings.getStringsMapping()).hasSize(1);
        assertThat(strings.get(0, 0))
                .isEqualTo(new TableCellStringData("abcdef", new Point(55, 65), new Point(17, 40)));
        assertThat(strings.get(0, 0)).isSameAs(oldData);
    }
}
