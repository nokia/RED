/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import java.io.File;

/**
 * @author mmarzec
 */
public class ExecutionElement {
    
    public enum ExecutionElementType {
        SUITE,
        TEST,
        OUTPUT_FILE
    }

    private final String name;
    
    private final ExecutionElementType type;
    
    private final File source;

    private final int elapsedTime;

    private final Status status;

    private final String message;

    ExecutionElement(final String name, final ExecutionElementType type, final File source, final int elapsedTime,
            final Status status, final String message) {
        this.name = name;
        this.type = type;
        this.source = source;
        this.elapsedTime = elapsedTime;
        this.status = status;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public ExecutionElementType getType() {
        return type;
    }

    public File getSource() {
        return source;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}
