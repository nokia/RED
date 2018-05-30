/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;

public interface IRobotLineElement {

    public static final int NOT_SET = -1;

    int getLineNumber();

    int getStartColumn();

    int getEndColumn();

    int getStartOffset();

    FilePosition getFilePosition();

    String getText();

    List<IRobotTokenType> getTypes();

    boolean isDirty();

    VersionAvailabilityInfo getVersionInformation();

    IRobotLineElement copyWithoutPosition();

    IRobotLineElement copy();
}
