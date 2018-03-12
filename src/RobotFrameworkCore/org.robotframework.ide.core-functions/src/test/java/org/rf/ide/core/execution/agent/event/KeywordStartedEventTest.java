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
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RunningKeyword;

import com.google.common.collect.ImmutableMap;

public class KeywordStartedEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType", "libname", "libName")));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword", newArrayList());
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword", newArrayList(new Object()));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword", newArrayList(ImmutableMap.of()));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "libname", "libName")));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("type", "kwType", "libname", "libName")));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType")));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_9() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre3() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword", newArrayList());
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre4() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword", newArrayList(new Object()));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre5() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword", newArrayList(ImmutableMap.of()));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre6() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "libname", "libName")));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre7() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("type", "kwType", "libname", "libName")));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre8() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType")));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre9() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        KeywordStartedEvent.fromPre(eventMap);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetRunningKeywordOfUnknownType() {
        final KeywordStartedEvent event = new KeywordStartedEvent("kw", "unknown", "lib");

        event.getRunningKeyword();
    }

    @Test
    public void eventForPreStartedIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "keyword", "libname", "libName")));
        final KeywordStartedEvent event = KeywordStartedEvent.fromPre(eventMap);

        assertThat(event.getName()).isEqualTo("kwName");
        assertThat(event.getKeywordType()).isEqualTo("keyword");
        assertThat(event.getLibraryName()).isEqualTo("libName");
        assertThat(event.getRunningKeyword())
                .isEqualTo(new RunningKeyword("libName", "kwName", KeywordCallType.NORMAL_CALL));
    }

    @Test
    public void eventForStartedIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType", "libname", "libName")));
        final KeywordStartedEvent event = KeywordStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("kwName");
        assertThat(event.getKeywordType()).isEqualTo("kwType");
        assertThat(event.getLibraryName()).isEqualTo("libName");
    }

    @Test
    public void equalsTests() {
        assertThat(new KeywordStartedEvent("kw", "type", "lib"))
                .isEqualTo(new KeywordStartedEvent("kw", "type", "lib"));

        assertThat(new KeywordStartedEvent("kw", "type", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw1", "type", "lib"));
        assertThat(new KeywordStartedEvent("kw1", "type", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw", "type", "lib"));
        assertThat(new KeywordStartedEvent("kw", "type", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw", "type1", "lib"));
        assertThat(new KeywordStartedEvent("kw", "type1", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw", "type", "lib"));
        assertThat(new KeywordStartedEvent("kw", "type", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw1", "type1", "lib"));
        assertThat(new KeywordStartedEvent("kw1", "type1", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw", "type", "lib"));
        assertThat(new KeywordStartedEvent("kw", "type", "lib"))
                .isNotEqualTo(new KeywordStartedEvent("kw", "type", "lib1"));
        assertThat(new KeywordStartedEvent("kw", "type", "lib")).isNotEqualTo(new Object());
        assertThat(new KeywordStartedEvent("kw", "type", "lib")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new KeywordStartedEvent("kw", "type", "lib").hashCode())
                .isEqualTo(new KeywordStartedEvent("kw", "type", "lib").hashCode());
    }
}
