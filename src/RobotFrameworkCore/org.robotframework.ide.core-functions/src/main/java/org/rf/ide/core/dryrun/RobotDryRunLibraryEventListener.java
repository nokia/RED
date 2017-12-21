/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.util.function.Consumer;

import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;

public class RobotDryRunLibraryEventListener extends RobotDefaultAgentEventListener {

    private final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    private final Consumer<String> libNameHandler;

    public RobotDryRunLibraryEventListener(final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector,
            final Consumer<String> libNameHandler) {
        this.dryRunLibraryImportCollector = dryRunLibraryImportCollector;
        this.libNameHandler = libNameHandler;
    }

    @Override
    public void handleLibraryImport(final LibraryImportEvent event) {
        dryRunLibraryImportCollector.collectFromLibraryImportEvent(event);
        libNameHandler.accept(event.getName());
    }

    @Override
    public void handleMessage(final MessageEvent event) {
        if (event.getLevel() == LogLevel.NONE) {
            dryRunLibraryImportCollector.collectFromMessageEvent(event);
        }
    }
}
