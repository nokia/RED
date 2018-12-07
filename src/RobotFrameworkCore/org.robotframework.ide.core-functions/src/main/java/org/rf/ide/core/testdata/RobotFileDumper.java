/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.write.TsvRobotFileDumper;
import org.rf.ide.core.testdata.text.write.TxtRobotFileDumper;

public class RobotFileDumper {

    private static final List<IRobotFileDumper> AVAILABLE_FORMAT_DUMPERS = newArrayList(new TxtRobotFileDumper(),
            new TsvRobotFileDumper());

    public DumpedResult dump(final DumpContext context, final RobotFileOutput output) {
        final IRobotFileDumper dumper = getDumper(output);
        dumper.setContext(context);
        return dumper.dump(output.getFileModel());
    }

    private IRobotFileDumper getDumper(final RobotFileOutput output) {
        return AVAILABLE_FORMAT_DUMPERS.stream()
                .filter(dumper -> dumper.isApplicableFor(output.getFileFormat()))
                .findFirst()
                .orElseGet(() -> new TxtRobotFileDumper());
    }
}
