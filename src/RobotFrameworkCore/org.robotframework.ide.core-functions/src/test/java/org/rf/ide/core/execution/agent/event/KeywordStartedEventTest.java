/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RunningKeyword;

import com.google.common.collect.ImmutableMap;

public class KeywordStartedEventTest {

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType", "libname", "libName")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword", newArrayList());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword", newArrayList(new Object()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword", newArrayList(ImmutableMap.of()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "libname", "libName")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("type", "kwType", "libname", "libName")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_9() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre3() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword", newArrayList());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre4() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword", newArrayList(new Object()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre5() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword", newArrayList(ImmutableMap.of()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre6() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "libname", "libName")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre7() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("type", "kwType", "libname", "libName")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre8() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_start_keyword",
                newArrayList("foo", ImmutableMap.of("kwname", "kwName", "type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre9() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordStartedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetRunningKeywordOfUnknownType() {
        final KeywordStartedEvent event = new KeywordStartedEvent("kw", "unknown", "lib");

        assertThatIllegalStateException().isThrownBy(event::getRunningKeyword);
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
