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

import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.agent.event.MessageEvent;

/**
 * @author bembenek
 */
public class RobotDryRunKeywordSourceCollector {

    private static final TypeReference<Map<String, RobotDryRunKeywordSource>> MESSAGE_TYPE = new TypeReference<Map<String, RobotDryRunKeywordSource>>() {
    };

    private final List<RobotDryRunKeywordSource> keywordSources = new ArrayList<>();

    public void collectFromMessageEvent(final MessageEvent event) throws IOException {
        JsonMessageMapper.readValue(event, "keyword", MESSAGE_TYPE).ifPresent(keywordSources::add);
    }

    public List<RobotDryRunKeywordSource> getKeywordSources() {
        return keywordSources;
    }
}
