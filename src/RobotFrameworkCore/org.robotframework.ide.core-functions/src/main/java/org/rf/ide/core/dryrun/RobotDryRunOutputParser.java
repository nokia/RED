/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.executor.ILineHandler;

/**
 * @author mmarzec
 */
public class RobotDryRunOutputParser implements ILineHandler {

    private static final String MESSAGE_EVENT_NAME = "message";

    private static final String LIBRARY_IMPORT_EVENT_NAME = "library_import";

    private static final String START_SUITE_EVENT_NAME = "start_suite";

    private final ObjectMapper mapper;

    private Map<String, List<?>> eventMap;

    private RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    private final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    private IDryRunStartSuiteHandler startSuiteHandler;

    public RobotDryRunOutputParser() {
        this.mapper = new ObjectMapper();
        this.eventMap = new HashMap<String, List<?>>();
        this.dryRunLKeywordSourceCollector = new RobotDryRunKeywordSourceCollector();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            eventMap = mapper.readValue(line, new TypeReference<Map<String, List<?>>>() {
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (eventMap.containsKey(LIBRARY_IMPORT_EVENT_NAME)) {
            final List<?> libraryImportList = eventMap.get(LIBRARY_IMPORT_EVENT_NAME);
            final Map<String, Object> details = (Map<String, Object>) libraryImportList.get(1);
            String libraryName = (String) libraryImportList.get(0);
            final String originalName = (String) details.get("originalname");
            if (originalName != null && !originalName.isEmpty() && !originalName.equals(libraryName)) {
                libraryName = originalName;
            }
            final String importer = (String) details.get("importer");
            final String source = (String) details.get("source");
            final List<String> args = (List<String>) details.get("args");

            if (dryRunLibraryImportCollector != null) {
                dryRunLibraryImportCollector.collectFromLibraryImportEvent(libraryName, importer, source, args);
            }

        } else if (eventMap.containsKey(MESSAGE_EVENT_NAME)) {
            final List<?> messageList = eventMap.get(MESSAGE_EVENT_NAME);
            final Map<String, String> details = (Map<String, String>) messageList.get(0);
            final String messageLevel = details.get("level");
            final String message = details.get("message");

            if (messageLevel != null) {
                if (dryRunLibraryImportCollector != null) {
                    if (messageLevel.equalsIgnoreCase("FAIL")) {
                        dryRunLibraryImportCollector.collectFromFailMessageEvent(message);
                    } else if (messageLevel.equalsIgnoreCase("ERROR")) {
                        dryRunLibraryImportCollector.collectFromErrorMessageEvent(message);
                    }
                }
                if (messageLevel.equalsIgnoreCase("NONE")) {
                    dryRunLKeywordSourceCollector.collectFromMessageEvent(message);
                }
            }

        } else if (eventMap.containsKey(START_SUITE_EVENT_NAME)) {
            final List<?> suiteList = eventMap.get(START_SUITE_EVENT_NAME);
            final String suiteName = (String) suiteList.get(0);
            if (startSuiteHandler != null && suiteName != null) {
                startSuiteHandler.processStartSuiteEvent(suiteName);
            }
        }
    }

    public List<RobotDryRunLibraryImport> getImportedLibraries() {
        return dryRunLibraryImportCollector != null ? dryRunLibraryImportCollector.getImportedLibraries()
                : new ArrayList<RobotDryRunLibraryImport>();
    }

    public void setStartSuiteHandler(final IDryRunStartSuiteHandler startSuiteHandler) {
        this.startSuiteHandler = startSuiteHandler;
    }

    public void setupRobotDryRunLibraryImportCollector(final Set<String> standardLibrariesNames) {
        dryRunLibraryImportCollector = new RobotDryRunLibraryImportCollector(standardLibrariesNames);
    }

    public List<RobotDryRunKeywordSource> getKeywordSources() {
        return dryRunLKeywordSourceCollector.getKeywordSources();
    }
}
