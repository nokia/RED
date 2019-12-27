/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;

public class TidyFormatterTest {

    @Test
    public void tidyFormatterAsksEnvironmentToFormatContent() {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.convertRobotDataFile(any(File.class))).thenReturn("formatted content");

        final TidyFormatter formatter = new TidyFormatter(env);
        final String formatted = formatter.format("content to format");
        assertThat(formatted).isEqualTo("formatted content");
    }

    @Test
    public void originalContentIsReturned_whenExceptionIsThrown() {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.convertRobotDataFile(any(File.class))).thenThrow(RuntimeEnvironmentException.class);

        final TidyFormatter formatter = new TidyFormatter(env);
        final String formatted = formatter.format("content to format");
        assertThat(formatted).isEqualTo("content to format");
    }

}
