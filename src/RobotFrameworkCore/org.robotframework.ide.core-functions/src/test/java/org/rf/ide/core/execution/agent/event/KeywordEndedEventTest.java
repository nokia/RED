/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

import com.google.common.collect.ImmutableMap;

public class KeywordEndedEventTest {

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword", newArrayList());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword", newArrayList(new Object()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword", newArrayList(ImmutableMap.of()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList(1, ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", 1)));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_9() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("kwName", ImmutableMap.of("kwname", "kwName")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre3() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword", newArrayList());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre4() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword", newArrayList(new Object()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre5() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword", newArrayList(ImmutableMap.of()));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre6() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList(1, ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre7() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", 1)));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_pre8() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("kwName", ImmutableMap.of("type", "kwType")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> KeywordEndedEvent.fromPre(eventMap));
    }

    @Test
    public void eventForPreEndedIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("pre_end_keyword",
                newArrayList("foo",
                        ImmutableMap.of("kwname", "kwName", "libname", "lib", "type", "kwType", "status", "PASS")));
        final KeywordEndedEvent event = KeywordEndedEvent.fromPre(eventMap);

        assertThat(event.getLibraryName()).isEqualTo("lib");
        assertThat(event.getName()).isEqualTo("kwName");
        assertThat(event.getQualifiedName()).isEqualTo(QualifiedKeywordName.create("kwName", "lib"));
        assertThat(event.getKeywordType()).isEqualTo("kwType");
        assertThat(event.getStatus()).isEqualTo(Status.PASS);
    }

    @Test
    public void eventForEndedIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("end_keyword",
                newArrayList("foo",
                        ImmutableMap.of("kwname", "kwName", "libname", "lib", "type", "kwType", "status", "FAIL")));
        final KeywordEndedEvent event = KeywordEndedEvent.from(eventMap);

        assertThat(event.getLibraryName()).isEqualTo("lib");
        assertThat(event.getName()).isEqualTo("kwName");
        assertThat(event.getQualifiedName()).isEqualTo(QualifiedKeywordName.create("kwName", "lib"));
        assertThat(event.getKeywordType()).isEqualTo("kwType");
        assertThat(event.getStatus()).isEqualTo(Status.FAIL);
    }

    @Test
    public void equalsTests() {
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS))
                .isEqualTo(new KeywordEndedEvent("lib", "kw", "type", Status.PASS));

        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw1", "type", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw1", "type", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw", "type", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw", "type1", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw", "type1", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw", "type", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw1", "type1", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw1", "type1", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw", "type", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib", "kw", "type", Status.FAIL));
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS))
                .isNotEqualTo(new KeywordEndedEvent("lib1", "kw", "type", Status.PASS));
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS)).isNotEqualTo(new Object());
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS)).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new KeywordEndedEvent("lib", "kw", "type", Status.PASS).hashCode())
                .isEqualTo(new KeywordEndedEvent("lib", "kw", "type", Status.PASS).hashCode());
    }
}
