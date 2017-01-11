/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author bembenek
 */
public class RobotDryRunKeywordSourceCollector {

    private final List<RobotDryRunKeywordSource> keywordSources = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public void collectFromMessageEvent(final String message) {
        try {
            final Map<String, Object> parsedMessage = mapper.readValue(message, Map.class);
            final Map<String, Object> keyword = (Map<String, Object>) parsedMessage.get("keyword");
            final String name = (String) keyword.get("name");
            final String libraryName = (String) keyword.get("library_name");
            final String filePath = (String) keyword.get("file_path");
            final int lineNumber = (int) keyword.get("line_number");
            keywordSources.add(new RobotDryRunKeywordSource(name, libraryName, filePath, lineNumber));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public List<RobotDryRunKeywordSource> getKeywordSources() {
        return keywordSources;
    }
}
