/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import java.io.File;

import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;

public class ExecutionElementsFactory {

    public static ExecutionElement createStartTestExecutionElement(final String name) {
        return new ExecutionElement(name, ExecutionElementType.TEST, null, -1, null, null);
    }

    public static ExecutionElement createEndTestExecutionElement(final String name, final int elapsedTime,
            final String message, final Status status) {
        return new ExecutionElement(name, ExecutionElementType.TEST, null, elapsedTime, status, message);
    }

    public static ExecutionElement createStartSuiteExecutionElement(final String name, final File suiteFilePath) {
        return new ExecutionElement(name, ExecutionElementType.SUITE, suiteFilePath, -1, null, null);
    }

    public static ExecutionElement createEndSuiteExecutionElement(final String name, final int elapsedTime,
            final String message, final Status status) {
        return new ExecutionElement(name, ExecutionElementType.SUITE, null, elapsedTime, status, message);
    }

    public static ExecutionElement createOutputFileExecutionElement(final File outputFilepath) {
        return new ExecutionElement(outputFilepath.getAbsolutePath(), ExecutionElementType.OUTPUT_FILE, null, -1, null,
                null);
    }
}
