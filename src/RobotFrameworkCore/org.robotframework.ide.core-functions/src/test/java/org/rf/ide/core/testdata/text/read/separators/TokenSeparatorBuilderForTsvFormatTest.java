/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.separators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FileFormat;

public class TokenSeparatorBuilderForTsvFormatTest {

    private TokenSeparatorBuilder builder;

    @BeforeEach
    public void setUp() {
        this.builder = new TokenSeparatorBuilder(FileFormat.TSV);
    }

    @Test
    public void test_isPipeSeparated_withoutPipe_shouldReturn_everytimeFalse() {
        assertThat(builder.isPipeSeparated("  ")).isFalse();
    }

    @Test
    public void test_isPipeSeparated_withPipe_shouldReturn_everytimeFalse() {
        assertThat(builder.isPipeSeparated("| ")).isFalse();
    }
}
