/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast;

public interface IContainerElement {

    boolean isComplex();


    ContainerElementType getType();


    String prettyPrint(int deepLevel);
}
