/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.Map;

/**
 * @author mmarzec
 *
 */
public class KeywordContext {

    private final String fileName;

    private final int lineNumber;

    private Map<String, Object> variables;

    public KeywordContext() {
        this(null, 0, null);
    }

    public KeywordContext(final String fileName, final int lineNumber, final Map<String, Object> variables) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(final Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
