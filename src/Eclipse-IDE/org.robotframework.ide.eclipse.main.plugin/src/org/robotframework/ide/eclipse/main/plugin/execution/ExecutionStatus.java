/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import java.util.List;

import org.robotframework.ide.core.execution.ExecutionElement.ExecutionElementType;

public class ExecutionStatus {

    public enum Status {
        PASS,
        FAIL,
        RUNNING
    }

    private String name;

    private Status status;

    private ExecutionElementType type;

    private List<ExecutionStatus> children;

    private ExecutionStatus parent;

    private String message;

    private String elapsedTime;

    private String source;

    public ExecutionStatus(String name, Status status, ExecutionElementType type, List<ExecutionStatus> children) {
        this.name = name;
        this.status = status;
        this.children = children;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<ExecutionStatus> getChildren() {
        return children;
    }

    public void addChildren(ExecutionStatus child) {
        this.children.add(child);
    }

    public ExecutionStatus getParent() {
        return parent;
    }

    public void setParent(ExecutionStatus parent) {
        this.parent = parent;
    }

    public ExecutionElementType getType() {
        return type;
    }

    public void setType(ExecutionElementType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
