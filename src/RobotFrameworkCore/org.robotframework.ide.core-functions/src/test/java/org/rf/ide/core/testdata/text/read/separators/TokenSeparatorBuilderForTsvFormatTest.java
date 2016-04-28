/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.separators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class TokenSeparatorBuilderForTsvFormatTest {

    @ForClean
    private TokenSeparatorBuilder builder;

    @Test
    public void test_isPipeSeparated_withoutPipe_shouldReturn_everytimeFalse() {
        assertThat(builder.isPipeSeparated("  ")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_withPipe_shouldReturn_everytimeFalse() {
        assertThat(builder.isPipeSeparated("| ")).isFalse();
    }

    @Before
    public void setUp() {
        this.builder = new TokenSeparatorBuilder(FileFormat.TSV);
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
