/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import java.io.File;
import java.io.IOException;

import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class TidyFormatter implements RobotSourceFormatter {

    private final IRuntimeEnvironment env;

    public TidyFormatter(final IRuntimeEnvironment env) {
        this.env = env;
    }

    @Override
    public String format(final String content) {
        String formatted = null;
        File file = null;
        try {
            file = File.createTempFile("tmp", ".robot",
                    RedTemporaryDirectory.createTemporaryDirectoryIfNotExists().toFile());
            Files.asCharSink(file, Charsets.UTF_8).write(content);

            formatted = env.convertRobotDataFile(file);

        } catch (final IOException | RuntimeEnvironmentException e) {
            // original content will be returned in that case

        } finally {
            if (file != null) {
                file.delete();
            }
        }
        return formatted == null ? content : formatted;
    }

}
