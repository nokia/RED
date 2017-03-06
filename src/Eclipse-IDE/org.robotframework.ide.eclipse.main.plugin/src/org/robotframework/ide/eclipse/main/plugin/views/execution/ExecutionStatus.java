/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.util.List;

import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.rf.ide.core.execution.Status;

class ExecutionStatus {

    private final String name;

    private Status status;

    private final ExecutionElementType type;

    private final List<ExecutionStatus> children;

    private ExecutionStatus parent;

    private String message;

    private String elapsedTime;

    private String source;

    ExecutionStatus(final String name, final Status status, final ExecutionElementType type,
            final List<ExecutionStatus> children) {
        this.name = name;
        this.status = status;
        this.children = children;
        this.type = type;
    }

    String getName() {
        return name;
    }

    Status getStatus() {
        return status;
    }

    void setStatus(final Status status) {
        this.status = status;
    }

    List<ExecutionStatus> getChildren() {
        return children;
    }

    void addChildren(final ExecutionStatus child) {
        children.add(child);
    }

    ExecutionStatus getParent() {
        return parent;
    }

    void setParent(final ExecutionStatus parent) {
        this.parent = parent;
    }

    ExecutionElementType getType() {
        return type;
    }

    String getMessage() {
        return message;
    }

    void setMessage(final String message) {
        this.message = message;
    }

    String getElapsedTime() {
        return elapsedTime;
    }

    void setElapsedTime(final String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    String getSource() {
        return source;
    }

    void setSource(final String source) {
        this.source = source;
    }
}
