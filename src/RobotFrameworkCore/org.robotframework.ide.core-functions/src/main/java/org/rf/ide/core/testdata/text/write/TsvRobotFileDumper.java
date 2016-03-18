/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.io.File;

import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public class TsvRobotFileDumper extends ARobotFileDumper {

    @Override
    public boolean canDumpFile(final File file) {
        boolean result = false;

        if (file != null && file.isFile()) {
            final String fileName = file.getName().toLowerCase();
            result = (fileName.endsWith(".tsv"));
        }

        return result;
    }

    @Override
    protected Separator getSeparatorDefault() {
        Separator sep = new Separator();
        sep.setRaw("\t");
        sep.setText("\t");
        sep.setType(SeparatorType.TABULATOR_OR_DOUBLE_SPACE);

        return sep;
    }
}
