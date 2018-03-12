/*
* Copyright 2015 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.text.read;

import java.io.File;

import org.rf.ide.core.testdata.IRobotFileParser;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

@SuppressWarnings("PMD.GodClass")
public class TsvRobotFileParser extends ATextualRobotFileParser {

    public TsvRobotFileParser() {
        super(new TokenSeparatorBuilder(FileFormat.TSV));
    }

    @Override
    public boolean canParseFile(final File file, final boolean isFromStringContent) {
        boolean result = false;

        if (file != null && (file.isFile() || isFromStringContent)) {
            final String fileName = file.getName().toLowerCase();
            result = fileName.endsWith(".tsv");
        }

        return result;
    }

    @Override
    public IRobotFileParser newInstance() {
        return new TsvRobotFileParser();
    }
}
