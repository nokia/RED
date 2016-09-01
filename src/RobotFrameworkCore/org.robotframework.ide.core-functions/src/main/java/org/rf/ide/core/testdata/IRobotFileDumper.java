/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import java.io.File;

import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.model.RobotFile;

public interface IRobotFileDumper {

    boolean canDumpFile(final File file);

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void dump(final File robotFile, final RobotFile model) throws Exception;

    String dump(final RobotFile model);

    DumpedResult dumpToResultObject(final RobotFile model);

    void setContext(final DumpContext ctx);
}
