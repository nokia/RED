/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;

enum TasksAdderState implements TokenState {
    TASK("task"),
    CALL("call");

    private final String name;

    private TasksAdderState(final String name) {
        this.name = name;
    }

    @Override
    public String getNewObjectTypeName() {
        return name;
    }
}
