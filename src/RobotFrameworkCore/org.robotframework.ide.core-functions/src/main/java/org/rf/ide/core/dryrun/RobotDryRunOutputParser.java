/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.rf.ide.core.executor.ILineHandler;

/**
 * @author mmarzec
 */
public class RobotDryRunOutputParser implements ILineHandler {

    private static final String MESSAGE_EVENT_NAME = "message";

    private static final String LIBRARY_IMPORT_EVENT_NAME = "library_import";
    
    private static final String START_SUITE_EVENT_NAME = "start_suite";

    private final ObjectMapper mapper;

    private Map<String, Object> parsedLine;

    private RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;
    
    private IDryRunStartSuiteHandler startSuiteHandler;

    public RobotDryRunOutputParser(final Set<String> standardLibrariesNames) {
        this.mapper = new ObjectMapper();
        this.parsedLine = new HashMap<String, Object>();
        dryRunLibraryImportCollector = new RobotDryRunLibraryImportCollector(standardLibrariesNames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            parsedLine = mapper.readValue(line, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (parsedLine.containsKey(LIBRARY_IMPORT_EVENT_NAME)) {
            final List<Object> libraryImportList = (List<Object>) parsedLine.get(LIBRARY_IMPORT_EVENT_NAME);
            final Map<String, Object> details = (Map<String, Object>) libraryImportList.get(1);
            final String libraryName = (String) libraryImportList.get(0);
            final String importer = (String) details.get("importer");
            final String source = (String) details.get("source");
            final List<String> args = (List<String>) details.get("args");

            dryRunLibraryImportCollector.collectFromLibraryImportEvent(libraryName, importer, source, args);
            
        } else if (parsedLine.containsKey(MESSAGE_EVENT_NAME)) {
            final List<Object> messageList = (List<Object>) parsedLine.get(MESSAGE_EVENT_NAME);
            final Map<String, String> details = (Map<String, String>) messageList.get(0);
            final String messageLevel = details.get("level");

            if (messageLevel != null && messageLevel.equalsIgnoreCase("FAIL")) {
                String failMessage = details.get("message");
                dryRunLibraryImportCollector.collectFromFailMessageEvent(failMessage);

            } else if (messageLevel != null && messageLevel.equalsIgnoreCase("ERROR")) {
                String errorMessage = details.get("message");
                dryRunLibraryImportCollector.collectFromErrorMessageEvent(errorMessage);
            }
            
        } else if (parsedLine.containsKey(START_SUITE_EVENT_NAME)) {
            final List<?> suiteList = (List<?>) parsedLine.get(START_SUITE_EVENT_NAME);
            final String suiteName = (String) suiteList.get(0);
            if (startSuiteHandler != null && suiteName != null) {
                startSuiteHandler.processStartSuiteEvent(suiteName);
            }
        }
    }

    public List<RobotDryRunLibraryImport> getImportedLibraries() {
        return dryRunLibraryImportCollector.getImportedLibraries();
    }

    public void setStartSuiteHandler(final IDryRunStartSuiteHandler startSuiteHandler) {
        this.startSuiteHandler = startSuiteHandler;
    }

}
