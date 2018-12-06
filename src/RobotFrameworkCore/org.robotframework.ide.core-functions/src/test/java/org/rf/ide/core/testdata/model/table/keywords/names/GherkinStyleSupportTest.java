/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GherkinStyleSupportTest {

    @Test
    public void testEmptyName() {
        final String originalName = "";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo(originalName);
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo(originalName);
    }

    @Test
    public void testNameWithoutPrefixes_shouldReturnSameName() {
        final String originalName = "Keyword";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo(originalName);
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo(originalName);
    }

    @Test
    public void testNameWithOnlyPrefix_shouldReturnSameName() {
        final String originalName = "And";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo(originalName);
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo(originalName);
    }

    @Test
    public void testNameWithOnePrefix() {
        final String originalName = "Given Suffix";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo("Suffix");
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo("Suffix");
    }

    @Test
    public void testNameWithSeveralPrefixes() {
        final String originalName = "When Then Assertion";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo("Then Assertion");
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo("Assertion");
    }

    @Test
    public void testNameWithSeveralPrefixes_andCaseInsensitive() {
        final String originalName = "when And But Condition";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo("And But Condition");
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo("Condition");
    }

    @Test
    public void testNameWithPrefixWithoutSeparator() {
        final String originalName = "WhenAction";
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName)).isEqualTo(originalName);
        assertThat(GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName)).isEqualTo(originalName);
    }
}
