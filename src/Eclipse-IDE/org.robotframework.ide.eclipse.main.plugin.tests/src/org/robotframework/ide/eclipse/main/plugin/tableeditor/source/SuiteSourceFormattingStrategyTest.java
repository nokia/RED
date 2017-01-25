/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceFormattingStrategy.FormatterProperties;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceFormattingStrategy.SeparatorType;

import com.google.common.base.Joiner;

public class SuiteSourceFormattingStrategyTest {

    private final SuiteSourceFormattingStrategy formatter = new SuiteSourceFormattingStrategy();


    @Test
    public void allTabsAreReplacedWithSpaces() {
        final String beforeFormatting = createContent("\tKeyword\t  123\t");
        final String expected = createContent("    Keyword    123");

        assertThat(formatConstant(beforeFormatting)).isEqualTo(expected);
    }

    @Test
    public void trailingSpacesAreRemoved() {
        final String beforeFormatting = createContent("Keyword    ");
        final String expected = createContent("Keyword");

        assertThat(formatConstant(beforeFormatting)).isEqualTo(expected);
    }

    private String formatConstant(final String content) {
        return formatter.format(content, "\n", new FormatterProperties(SeparatorType.CONSTANT, 4));
    }

    private static String createContent(final String... lines) {
        return Joiner.on("\n").join(lines) + "\n";
    }
}
