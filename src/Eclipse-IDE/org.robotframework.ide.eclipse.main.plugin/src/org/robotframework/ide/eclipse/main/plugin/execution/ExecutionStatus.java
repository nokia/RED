/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import java.util.List;

import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;

public class ExecutionStatus {

    public enum Status {
        PASS,
        FAIL,
        RUNNING
    }

    private final String name;

    private Status status;

    private final ExecutionElementType type;

    private final List<ExecutionStatus> children;

    private ExecutionStatus parent;

    private String message;

    private String elapsedTime;

    private String source;

    public ExecutionStatus(final String name, final Status status, final ExecutionElementType type, final List<ExecutionStatus> children) {
        this.name = name;
        this.status = status;
        this.children = children;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public List<ExecutionStatus> getChildren() {
        return children;
    }

    public void addChildren(final ExecutionStatus child) {
        this.children.add(child);
    }

    public ExecutionStatus getParent() {
        return parent;
    }

    public void setParent(final ExecutionStatus parent) {
        this.parent = parent;
    }

    public ExecutionElementType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(final String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

}
