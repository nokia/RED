/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class KeywordEndedEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword", newArrayList());
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword", newArrayList(new Object()));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword", newArrayList(ImmutableMap.of()));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList(1, ImmutableMap.of("type", "kwType")));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", 1)));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_9() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("kwName", ImmutableMap.of("kwname", "kwName")));
        KeywordEndedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre3() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword", newArrayList());
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre4() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword", newArrayList(new Object()));
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre5() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword", newArrayList(ImmutableMap.of()));
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre6() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList(1, ImmutableMap.of("type", "kwType")));
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre7() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", 1)));
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre8() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordEndedEvent.fromPre(eventMap);
    }

    @Test
    public void eventForPreEndedIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType")));
        final KeywordEndedEvent event = KeywordEndedEvent.fromPre(eventMap);
        
        assertThat(event.getName()).isEqualTo("kwName");
        assertThat(event.getKeywordType()).isEqualTo("kwType");
    }

    @Test
    public void eventForEndedIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType")));
        final KeywordEndedEvent event = KeywordEndedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("kwName");
        assertThat(event.getKeywordType()).isEqualTo("kwType");
    }

    @Test
    public void equalsTests() {
        assertThat(new KeywordEndedEvent("kw", "type")).isEqualTo(new KeywordEndedEvent("kw", "type"));

        assertThat(new KeywordEndedEvent("kw", "type")).isNotEqualTo(new KeywordEndedEvent("kw1", "type"));
        assertThat(new KeywordEndedEvent("kw1", "type")).isNotEqualTo(new KeywordEndedEvent("kw", "type"));
        assertThat(new KeywordEndedEvent("kw", "type")).isNotEqualTo(new KeywordEndedEvent("kw", "type1"));
        assertThat(new KeywordEndedEvent("kw", "type1")).isNotEqualTo(new KeywordEndedEvent("kw", "type"));
        assertThat(new KeywordEndedEvent("kw", "type")).isNotEqualTo(new KeywordEndedEvent("kw1", "type1"));
        assertThat(new KeywordEndedEvent("kw1", "type1")).isNotEqualTo(new KeywordEndedEvent("kw", "type"));
        assertThat(new KeywordEndedEvent("kw", "type")).isNotEqualTo(new Object());
        assertThat(new KeywordEndedEvent("kw", "type")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new KeywordEndedEvent("kw", "type").hashCode())
                .isEqualTo(new KeywordEndedEvent("kw", "type").hashCode());
    }
}
