/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.write.TxtRobotFileDumper;


public class RobotFileDumper {

    private static final List<IRobotFileDumper> availableFormatDumpers = new ArrayList<>();
    static {
        availableFormatDumpers.add(new TxtRobotFileDumper());
    }


    public void dump(final File file, final RobotFileOutput output)
            throws Exception {
        IRobotFileDumper dumperToUse = null;
        for (final IRobotFileDumper dumper : availableFormatDumpers) {
            if (dumper.canDumpFile(file)) {
                dumperToUse = dumper;
                break;
            }
        }

        if (dumperToUse != null) {
            dumperToUse.dump(file, output.getFileModel());
        }
    }


    public String dump(final RobotFileOutput output) {
        return new TxtRobotFileDumper().dump(output.getFileModel());
    }
}
