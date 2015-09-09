/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.io.File;
import java.util.Map;


/**
 * 
 * @author wypych
 * @author marzec
 */
public class KeywordDeclarationDebugFinder {

    /**
     * 
     * @author marzec api designer
     * @param suiteMap
     * @param currentVariables
     * @return
     */
    public KeywordPosition findKeyword(
            final Map<RobotSuiteExecutionContext, ExecutionStatus> suiteMap,
            final Map<String, Object> currentVariables) {
        KeywordPosition position = null;

        return (position == null) ? KeywordPosition
                .createForNotFound() : position;
    }

    public static class KeywordPosition {

        public static final int LINE_POSITION_NOT_FOUND = -1;

        private final File file;
        private int lineNumber = LINE_POSITION_NOT_FOUND;


        public KeywordPosition(final File file, final int lineNumber) {
            this.file = file;
            this.lineNumber = lineNumber;
        }


        public static KeywordPosition createForNotFound() {
            return new KeywordPosition(null, LINE_POSITION_NOT_FOUND);
        }


        public File getFile() {
            return file;
        }


        public int getLineNumber() {
            return lineNumber;
        }


        public boolean wasLineFound() {
            return (this.lineNumber > LINE_POSITION_NOT_FOUND && this.file != null);
        }
    }
}
