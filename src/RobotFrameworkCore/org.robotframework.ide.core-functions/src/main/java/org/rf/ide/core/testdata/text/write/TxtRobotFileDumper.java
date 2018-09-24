/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class TxtRobotFileDumper extends ARobotFileDumper {

    @Override
    public boolean isApplicableFor(final FileFormat format) {
        return format == FileFormat.TXT_OR_ROBOT;
    }

    @Override
    protected Separator getSeparatorDefault() {
        final Separator sep = new Separator();
        sep.setRaw("\t");
        sep.setText("\t");
        sep.setType(SeparatorType.TABULATOR_OR_DOUBLE_SPACE);

        return sep;
    }

    @Override
    protected boolean isAcceptableForDefault(final Separator separator) {
        return (separator.getTypes().contains(SeparatorType.PIPE)
                || separator.getTypes().contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE));
    }

    @Override
    protected boolean canBeSeparatorAddBeforeExecutableUnitName(final Separator separator) {
        return (separator.getTypes().contains(SeparatorType.PIPE));
    }
}
