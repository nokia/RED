/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VersionsEvent.VersionsEventResponder;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ProtocolVersion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class VersionsEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        VersionsEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("version", new Object());
        VersionsEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("version", newArrayList());
        VersionsEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("version", newArrayList("foo"));
        VersionsEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final ImmutableMap<String, ? extends Object> template = ImmutableMap.of("cmd_line", "python RF", "python",
                "3.6", "robot", "3.0", "protocol", 1);

        // from all combinations remove this which consist of all keys
        final Set<Set<String>> allKeysCombinations = newHashSet(
                Sets.powerSet(newHashSet("cmd_line", "python", "robot", "protocol")));
        allKeysCombinations.remove(newHashSet("cmd_line", "python", "robot", "protocol"));

        for (final Set<String> combination : allKeysCombinations) {
            final Map<String, Object> attributes = new HashMap<>();
            for (final String key : combination) {
                attributes.put(key, template.get(key));
            }
            final Map<String, Object> eventMap = ImmutableMap.of("version", newArrayList(attributes));

            assertThatIllegalArgumentException().isThrownBy(() -> VersionsEvent.from(mock(AgentClient.class), eventMap))
                    .withMessage("Versions event should have command line, versions of python, robot and protocol")
                    .withNoCause();
        }
    }

    @Test
    public void eventIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("version",
                newArrayList(ImmutableMap.of("cmd_line", "python RF", "python", "3.6", "robot", "3.0", "protocol", 1)));
        final VersionsEvent event = VersionsEvent.from(mock(AgentClient.class), eventMap);

        assertThat(event.getCommandLine()).isEqualTo("python RF");
        assertThat(event.getPythonVersion()).isEqualTo("3.6");
        assertThat(event.getRobotVersion()).isEqualTo("3.0");
        assertThat(event.getProtocolVersion()).isEqualTo(1);
    }

    @Test
    public void equalsTests() {
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2))
                .isEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2));

        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.5", "3.0", 2));
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.5", "3.0", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2));

        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd1", "3.6", "3.0", 2));
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd1", "3.6", "3.0", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2));

        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.1", 2));
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.1", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2));

        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 3));
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 3))
                .isNotEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2));

        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2))
                .isNotEqualTo(new Object());
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2)).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2).hashCode())
                .isEqualTo(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2).hashCode());
        assertThat(new VersionsEvent(mock(VersionsEventResponder.class), "cmd", "3.6", "3.0", 2).hashCode())
                .isEqualTo(new VersionsEvent(null, "cmd", "3.6", "3.0", 2).hashCode());
    }

    @Test
    public void versionsResponsesTests() {
        final Map<String, Object> eventMap = ImmutableMap.of("version",
                newArrayList(ImmutableMap.of("cmd_line", "pyhton RF", "python", "3.6", "robot", "3.0", "protocol", 1)));

        final AgentClient client = mock(AgentClient.class);
        final VersionsEvent event = VersionsEvent.from(client, eventMap);

        event.responder().versionsCorrect();
        event.responder().versionsError("error");

        verify(client, times(2)).send(any(ProtocolVersion.class));
        verifyNoMoreInteractions(client);
    }
}
