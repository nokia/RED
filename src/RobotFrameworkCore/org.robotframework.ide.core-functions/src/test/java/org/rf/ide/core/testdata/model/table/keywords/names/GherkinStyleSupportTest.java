/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import org.junit.Assert;
import org.junit.Test;

public class GherkinStyleSupportTest {

    @Test
    public void testEmptyName() {
        String originalName = "";
        Assert.assertEquals(originalName, GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals(originalName, GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }

    @Test
    public void testNameWithoutPrefixes_shouldReturnSameName() {
        String originalName = "Keyword";
        Assert.assertEquals(originalName, GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals(originalName, GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }

    @Test
    public void testNameWithOnlyPrefix_shouldReturnSameName() {
        String originalName = "And";
        Assert.assertEquals(originalName, GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals(originalName, GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }

    @Test
    public void testNameWithOnePrefix() {
        String originalName = "Given Suffix";
        Assert.assertEquals("Suffix", GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals("Suffix", GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }

    @Test
    public void testNameWithSeveralPrefixes() {
        String originalName = "When Then Assertion";
        Assert.assertEquals("Then Assertion", GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals("Assertion", GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }

    @Test
    public void testNameWithSeveralPrefixes_andCaseInsensitive() {
        String originalName = "when And But Condition";
        Assert.assertEquals("And But Condition", GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals("Condition", GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }

    @Test
    public void testNameWithPrefixWithoutSeparator() {
        String originalName = "WhenAction";
        Assert.assertEquals("WhenAction", GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(originalName));
        Assert.assertEquals("WhenAction", GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(originalName));
    }
}
