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

public class TokenSeparatorBuilderForTxtFormatTest {

    @ForClean
    private TokenSeparatorBuilder builder;

    @Test
    public void test_isPipeSeparated_withoutPipe_shouldReturn_False() {
        assertThat(builder.isPipeSeparated("  ")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_with_TAB_Pipe_shouldReturn_False() {
        assertThat(builder.isPipeSeparated("\t| ")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_with_DOUBLE_SPACE_Pipe_shouldReturn_False() {
        assertThat(builder.isPipeSeparated("  | ")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_with_SPACE_Pipe_SPACE_shouldReturn_False() {
        assertThat(builder.isPipeSeparated(" | ")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_with_SPACE_Pipe_TAB_shouldReturn_False() {
        assertThat(builder.isPipeSeparated(" |\t")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_withPipe_SPACE_shouldReturn_True() {
        assertThat(builder.isPipeSeparated("| ")).isTrue();
    }

    @Before
    public void setUp() {
        this.builder = new TokenSeparatorBuilder(FileFormat.TXT_OR_ROBOT);
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
