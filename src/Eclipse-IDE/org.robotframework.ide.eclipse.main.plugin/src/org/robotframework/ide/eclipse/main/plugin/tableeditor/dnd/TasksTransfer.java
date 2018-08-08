/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;

import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;

public class TasksTransfer extends RedTransfer<RobotTask> {

    private static final String TYPE_NAME = "red-tasks-data-transfer-format";
    private static final TasksTransfer INSTANCE = new TasksTransfer(TYPE_NAME);

    public TasksTransfer(final String typeName) {
        super(typeName);
    }

    public static TasksTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean canHandleSerialization(final Object data) {
        return data instanceof RobotTask[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((RobotTask[]) data);
    }

    @Override
    protected RobotTask[] bytesToJava(final byte[] bytes) throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(RobotTask.class, bytes);
    }
}
