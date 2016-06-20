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
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.TsvRobotFileDumper;
import org.rf.ide.core.testdata.text.write.TxtRobotFileDumper;

public class RobotFileDumper {

    private static final List<IRobotFileDumper> AVAILABLE_FORMAT_DUMPERS = new ArrayList<>();

    private DumpContext ctx = new DumpContext();

    static {
        AVAILABLE_FORMAT_DUMPERS.add(new TxtRobotFileDumper());
        AVAILABLE_FORMAT_DUMPERS.add(new TsvRobotFileDumper());
    }

    public void setContext(final DumpContext ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void dump(final File file, final RobotFileOutput output) throws Exception {
        IRobotFileDumper dumperToUse = getDumper(file);
        dumperToUse.setContext(ctx);
        dumperToUse.dump(file, output.getFileModel());
    }

    public String dump(final RobotFileOutput output) {
        File fake = null;
        if (output.getFileFormat() == FileFormat.TSV) {
            fake = new File("fake.tsv");
        } else {
            fake = new File("fake.txt");
        }

        IRobotFileDumper dumper = getDumper(fake);
        if (dumper == null) {
            dumper = new TxtRobotFileDumper();
        }
        dumper.setContext(ctx);

        return dumper.dump(output.getFileModel());
    }

    private IRobotFileDumper getDumper(final File file) {
        IRobotFileDumper dumperToUse = null;
        for (final IRobotFileDumper dumper : AVAILABLE_FORMAT_DUMPERS) {
            if (dumper.canDumpFile(file)) {
                dumperToUse = dumper;
                break;
            }
        }
        return dumperToUse;
    }
}
