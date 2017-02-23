/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import java.io.File;
import java.io.IOException;

import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.model.RobotFile;

public interface IRobotFileDumper {

    boolean canDumpFile(final File file);

    void dump(final File robotFile, final RobotFile model) throws IOException;

    String dump(final RobotFile model);

    DumpedResult dumpToResultObject(final RobotFile model);

    void setContext(final DumpContext ctx);
}
