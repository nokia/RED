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
import org.codehaus.jackson.type.TypeReference;

/**
 * @author bembenek
 */
public class RobotDryRunKeywordSourceCollector {

    private static final String KEYWORD_KEY = "keyword";

    private final List<RobotDryRunKeywordSource> keywordSources = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public void collectFromMessageEvent(final String message) {
        try {
            final Map<String, RobotDryRunKeywordSource> keywordEntry = mapper.readValue(message,
                    new TypeReference<Map<String, RobotDryRunKeywordSource>>() {
                    });
            if (keywordEntry.containsKey(KEYWORD_KEY)) {
                keywordSources.add(keywordEntry.get(KEYWORD_KEY));
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public List<RobotDryRunKeywordSource> getKeywordSources() {
        return keywordSources;
    }
}
