/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;

import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

public class VariablesTransfer extends RedTransfer<RobotVariable> {

    private static final String TYPE_NAME = "red-variables-data-transfer-format";
    private static final VariablesTransfer INSTANCE = new VariablesTransfer(TYPE_NAME);

    public VariablesTransfer(final String typeName) {
        super(typeName);
    }

    public static VariablesTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean canHandleSerialization(final Object data) {
        return data instanceof RobotVariable[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((RobotVariable[]) data);
    }

    @Override
    protected RobotVariable[] bytesToJava(final byte[] bytes) throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(RobotVariable.class, bytes);
    }
}
