/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class KeywordCallsTransfer extends RedTransfer<RobotKeywordCall> {

    private static final String TYPE_NAME = "red-keywords-calls-data-transfer-format";
    private static final KeywordCallsTransfer INSTANCE = new KeywordCallsTransfer(TYPE_NAME);

    public KeywordCallsTransfer(final String typeName) {
        super(typeName);
    }

    public static KeywordCallsTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean canHandleSerialization(final Object data) {
        return data instanceof RobotKeywordCall[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((RobotKeywordCall[]) data);
    }

    @Override
    protected RobotKeywordCall[] bytesToJava(final byte[] bytes) throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(RobotKeywordCall.class, bytes);
    }
}
