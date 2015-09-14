/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read;

import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;


public interface IRobotLineElement {

    public static final int NOT_SET = -1;


    int getLineNumber();


    int getStartColumn();


    int getEndColumn();


    int getStartOffset();


    FilePosition getFilePosition();


    StringBuilder getText();


    StringBuilder getRaw();


    List<IRobotTokenType> getTypes();


    boolean isDirty();
}
