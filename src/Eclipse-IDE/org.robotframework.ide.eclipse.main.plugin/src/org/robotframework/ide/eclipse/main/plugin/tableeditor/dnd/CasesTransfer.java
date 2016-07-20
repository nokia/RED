/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;

public class CasesTransfer extends RedTransfer<RobotCase> {

    private static final String TYPE_NAME = "red-cases-data-transfer-format";
    private static final CasesTransfer INSTANCE = new CasesTransfer(TYPE_NAME);

    public CasesTransfer(final String typeName) {
        super(typeName);
    }

    public static CasesTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean canHandleSerialization(final Object data) {
        return data instanceof RobotCase[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((RobotCase[]) data);
    }

    @Override
    protected RobotCase[] bytesToJava(final byte[] bytes) throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(RobotCase.class, bytes);
    }
}
