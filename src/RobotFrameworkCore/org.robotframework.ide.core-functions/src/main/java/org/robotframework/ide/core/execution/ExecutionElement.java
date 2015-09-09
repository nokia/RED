/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution;

/**
 * @author mmarzec
 */
public class ExecutionElement {
    
    public enum ExecutionElementType {
        SUITE,
        TEST,
        OUTPUT_FILE
    }

    private String name;
    
    private ExecutionElementType type;
    
    private String status;

    private String source;

    private String message;

    private int elapsedTime;

    public ExecutionElement(String name, ExecutionElementType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExecutionElementType getType() {
        return type;
    }

}
