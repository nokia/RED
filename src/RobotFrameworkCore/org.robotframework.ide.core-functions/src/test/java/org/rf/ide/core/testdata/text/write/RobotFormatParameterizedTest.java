/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

@RunWith(Parameterized.class)
public abstract class RobotFormatParameterizedTest {

    private final String extension;

    private final FileFormat format;

    @Parameters(name = "${0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "txt", FileFormat.TXT_OR_ROBOT }, { "tsv", FileFormat.TSV } });
    }

    public RobotFormatParameterizedTest(final String extension, final FileFormat format) {
        this.extension = extension;
        this.format = format;
    }

    protected String getExtension() {
        return extension;
    }

    protected FileFormat getFormat() {
        return format;
    }

}
