/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;

public class CodeElementsColumnHeaderDataProviderTest {

    @Test
    public void emptyColumnNamesAreReturned_whenThereAreNoColumnHeaders() {
        final RobotSuiteFileSection section = createSection("Test Cases");
        
        final CodeElementsColumnHeaderDataProvider<RobotSuiteFileSection> provider = new CodeElementsColumnHeaderDataProvider<>(
                () -> 5, section);
        
        assertThat(IntStream.range(0, 10).mapToObj(col -> provider.getDataValue(col, 0))).containsOnly("");
    }

    @Test
    public void properColumnNamesAreReturned_whenThereAreIsAColumnHeaders() {
        final RobotSuiteFileSection section = createSection("Test Cases", "col1", "col2", "col3");

        final CodeElementsColumnHeaderDataProvider<RobotSuiteFileSection> provider = new CodeElementsColumnHeaderDataProvider<>(
                () -> 5, section);

        assertThat(IntStream.range(0, 10).mapToObj(col -> provider.getDataValue(col, 0))).containsExactly("col1",
                "col2", "col3", "", "", "", "", "", "", "");
    }

    @Test
    public void columnNamesAreReturnedFromOtherSection_ifInputWasReplaced() {
        final RobotSuiteFileSection section1 = createSection("Test Cases", "col1", "col2", "col3");
        final RobotSuiteFileSection section2 = createSection("Test Cases", "col4", "col5", "col6");

        final CodeElementsColumnHeaderDataProvider<RobotSuiteFileSection> provider = new CodeElementsColumnHeaderDataProvider<>(
                () -> 5, section1);

        assertThat(IntStream.range(0, 3).mapToObj(col -> provider.getDataValue(col, 0))).containsExactly("col1", "col2",
                "col3");

        provider.setInput(section2);

        assertThat(IntStream.range(0, 3).mapToObj(col -> provider.getDataValue(col, 0))).containsExactly("col4", "col5",
                "col6");

    }

    private static RobotSuiteFileSection createSection(final String sectionName, final String... header) {
        return new RobotSuiteFileCreator().appendLine("***" + sectionName + "***  " + String.join("  ", header))
                .build()
                .getSections()
                .get(0);
    }

}
