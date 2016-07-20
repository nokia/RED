/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

public class KeywordDefinitionsTransfer extends RedTransfer<RobotKeywordDefinition> {

    private static final String TYPE_NAME = "red-keywords-defs-data-transfer-format";
    private static final KeywordDefinitionsTransfer INSTANCE = new KeywordDefinitionsTransfer(TYPE_NAME);

    public KeywordDefinitionsTransfer(final String typeName) {
        super(typeName);
    }

    public static KeywordDefinitionsTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean canHandleSerialization(final Object data) {
        return data instanceof RobotKeywordDefinition[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((RobotKeywordDefinition[]) data);
    }

    @Override
    protected RobotKeywordDefinition[] bytesToJava(final byte[] bytes) throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(RobotKeywordDefinition.class, bytes);
    }
}
