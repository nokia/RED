/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import org.rf.ide.core.execution.RobotDefaultAgentEventListener;

public class RobotDryRunEventListener extends RobotDefaultAgentEventListener {

    private final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    private final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    private final Consumer<String> startSuiteHandler;

    public RobotDryRunEventListener(final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector,
            final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector,
            final Consumer<String> startSuiteHandler) {
        this.dryRunLibraryImportCollector = dryRunLibraryImportCollector;
        this.dryRunLKeywordSourceCollector = dryRunLKeywordSourceCollector;
        this.startSuiteHandler = startSuiteHandler;
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        startSuiteHandler.accept(suiteName);
    }

    @Override
    public void handleLibraryImport(final String name, final String importer, final String source,
            final List<String> args) {
        dryRunLibraryImportCollector.collectFromLibraryImportEvent(name, importer, source, args);
    }

    @Override
    public void handleMessage(final String message, final String level) {
        if (level != null) {
            if (level.equalsIgnoreCase("FAIL")) {
                dryRunLibraryImportCollector.collectFromFailMessageEvent(message);
            } else if (level.equalsIgnoreCase("ERROR")) {
                dryRunLibraryImportCollector.collectFromErrorMessageEvent(message);
            } else if (level.equalsIgnoreCase("NONE")) {
                dryRunLKeywordSourceCollector.collectFromMessageEvent(message);
            }
        }
    }
}
