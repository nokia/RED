/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.context.RobotDebugExecutionContext;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;


public class RemoteMessagesConsoleWriter extends RobotDefaultAgentEventListener {

    private final RobotDebugExecutionContext executionContext;

    private final RobotConsoleFacade consoleFacade;

    public RemoteMessagesConsoleWriter(final RobotDebugExecutionContext executionContext,
            final RobotConsoleFacade consoleFacade) {
        this.executionContext = executionContext;
        this.consoleFacade = consoleFacade;
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        final IPath suitePath = Path.fromOSString(suiteFilePath.getAbsolutePath());
        try {
            if (suitePath != null && suitePath.getFileExtension() != null) {
                consoleFacade.writeLine("Debugging test suite: " + suitePath.toOSString());
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to write messages into the console", e);
        }
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        if (!executionContext.isInTest()) {
            try {
                consoleFacade.writeLine("Test case \"" + testCaseName + "\" not available. Check the files content!");
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to write messages into the console", e);
            }
        }
    }
}
