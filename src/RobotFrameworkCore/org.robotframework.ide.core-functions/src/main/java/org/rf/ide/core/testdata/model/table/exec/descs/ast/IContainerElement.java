/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast;

import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;


public interface IContainerElement {

    boolean isComplex();


    ContainerElementType getType();


    String prettyPrint(int deepLevel);


    TextPosition getPosition();
}
