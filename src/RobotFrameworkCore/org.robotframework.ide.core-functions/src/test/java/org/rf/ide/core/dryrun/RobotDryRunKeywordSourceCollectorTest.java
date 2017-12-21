/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.MessageEvent;

import com.google.common.collect.ImmutableMap;

public class RobotDryRunKeywordSourceCollectorTest {

    @Test
    public void keywordSourcesAreCollectedFromMessages() throws Exception {
        final RobotDryRunKeywordSourceCollector kwSourceCollector = new RobotDryRunKeywordSourceCollector();

        kwSourceCollector.collectFromMessageEvent(createKwSourceMessageEvent("kw1", "lib1", "lib1.py", 3, 5, 7));
        kwSourceCollector.collectFromMessageEvent(createKwSourceMessageEvent("kw2", "lib1", "lib1.py", 5, 6, 4));
        kwSourceCollector.collectFromMessageEvent(createKwSourceMessageEvent("other_kw", "lib2", "lib2.py", 2, 4, 6));

        final RobotDryRunKeywordSource kw1 = new RobotDryRunKeywordSource();
        kw1.setName("kw1");
        kw1.setLibraryName("lib1");
        kw1.setFilePath("lib1.py");
        kw1.setLine(3);
        kw1.setOffset(5);
        kw1.setLength(7);

        final RobotDryRunKeywordSource kw2 = new RobotDryRunKeywordSource();
        kw2.setName("kw2");
        kw2.setLibraryName("lib1");
        kw2.setFilePath("lib1.py");
        kw2.setLine(5);
        kw2.setOffset(6);
        kw2.setLength(4);

        final RobotDryRunKeywordSource kw3 = new RobotDryRunKeywordSource();
        kw3.setName("other_kw");
        kw3.setLibraryName("lib2");
        kw3.setFilePath("lib2.py");
        kw3.setLine(2);
        kw3.setOffset(4);
        kw3.setLength(6);

        assertThat(kwSourceCollector.getKeywordSources()).hasSize(3);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(0), kw1);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(1), kw2);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(2), kw3);
    }

    @Test
    public void keywordSourceEventMappingExceptionIsHandled() throws Exception {
        final RobotDryRunKeywordSourceCollector kwSourceCollector = new RobotDryRunKeywordSourceCollector();

        final MessageEvent event = new MessageEvent("{\"keyword_source\":\"incorrect\"}", LogLevel.NONE, null);

        assertThatExceptionOfType(JsonMessageMapper.JsonMessageMapperException.class)
                .isThrownBy(() -> kwSourceCollector.collectFromMessageEvent(event))
                .withMessage("Problem with mapping message for key 'keyword_source'")
                .withCauseInstanceOf(JsonMappingException.class);
    }

    private static MessageEvent createKwSourceMessageEvent(final String name, final String libraryName,
            final String path, final int line, final int offset, final int length) throws Exception {
        final Object kwAttributes = ImmutableMap.builder()
                .put("filePath", path)
                .put("length", length)
                .put("libraryName", libraryName)
                .put("line", line)
                .put("name", name)
                .put("offset", offset)
                .build();
        final String message = new ObjectMapper().writeValueAsString((ImmutableMap.of("keyword_source", kwAttributes)));
        return new MessageEvent(message, LogLevel.NONE, null);
    }

    private static void assertCollectedKeywordSource(final RobotDryRunKeywordSource actual,
            final RobotDryRunKeywordSource expected) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getLibraryName()).isEqualTo(expected.getLibraryName());
        assertThat(actual.getFilePath()).isEqualTo(expected.getFilePath());
        assertThat(actual.getLine()).isEqualTo(expected.getLine());
        assertThat(actual.getOffset()).isEqualTo(expected.getOffset());
        assertThat(actual.getLength()).isEqualTo(expected.getLength());
    }
}
