/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;

import com.google.common.collect.ImmutableMap;

public class VariableMappingsResolverTest {

    @Test
    public void testSkippingIncorrectMappings() {
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("AbC", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("$AbC", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("$AbC}", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("${AbC", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("{AbC", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("{AbC}", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("AbC}", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("AbC", "x"), VariableMapping.create("${D}", "y"))))
                        .hasSize(1)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
    }

    @Test
    public void testSkippingNonScalarMappings() {
        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${a}", "x"),
                VariableMapping.create("@{b}", "x"), VariableMapping.create("&{c}", "x"),
                VariableMapping.create("${D}", "x"), VariableMapping.create("%{E}", "x")))).hasSize(2)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x", "${d}", "x"));
    }

    @Test
    public void testResolvingMappingsWithoutVariablesInValues() {
        assertThat(VariableMappingsResolver.resolve(newArrayList())).isEmpty();

        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${AbC}", "x/y"),
                VariableMapping.create("${dEf}", "y/z"), VariableMapping.create("${G_H I}", "a/b/c")))).hasSize(3)
                        .containsAllEntriesOf(ImmutableMap.of("${abc}", "x/y", "${def}", "y/z", "${ghi}", "a/b/c"));

        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${AbC}", "x\\y"),
                VariableMapping.create("${dEf}", "y\\z"), VariableMapping.create("${G_H I}", "a\\b\\c")))).hasSize(3)
                        .containsAllEntriesOf(ImmutableMap.of("${abc}", "x\\y", "${def}", "y\\z", "${ghi}", "a\\b\\c"));
    }

    @Test
    public void testResolvingMappingsWithVariablesInValues_whenAllVariablesAreKnown() {
        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${A}", "x/y"),
                VariableMapping.create("${B}", "${A}/z"), VariableMapping.create("${C}", "${A}/${A}")))).hasSize(3)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x/y", "${b}", "x/y/z", "${c}", "x/y/x/y"));

        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${A}", "x\\y"),
                VariableMapping.create("${B}", "${A}\\z"), VariableMapping.create("${C}", "${A}\\${A}")))).hasSize(3)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x\\y", "${b}", "x\\y\\z", "${c}", "x\\y\\x\\y"));

        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${Abc}", "x"),
                VariableMapping.create("${dEf}", "${A B C}y"), VariableMapping.create("${GHI}", "${D_ e_ F_}z"))))
                        .hasSize(3)
                        .containsAllEntriesOf(ImmutableMap.of("${abc}", "x", "${def}", "xy", "${ghi}", "xyz"));
    }

    @Test
    public void testResolvingMappingsWithVariablesInValues_whenSomeVariablesAreUnknown() {
        assertThat(VariableMappingsResolver
                .resolve(newArrayList(VariableMapping.create("${A}", "x/y"), VariableMapping.create("${B}", "xyz/${C}"),
                        VariableMapping.create("${C}", "${A}/${Z}"), VariableMapping.create("${D}", "${C}/${Z}"))))
                                .hasSize(4)
                                .containsAllEntriesOf(ImmutableMap.of("${a}", "x/y", "${b}", "xyz/${C}", "${c}",
                                        "x/y/${Z}", "${d}", "x/y/${Z}/${Z}"));

        assertThat(VariableMappingsResolver.resolve(
                newArrayList(VariableMapping.create("${A}", "x\\y"), VariableMapping.create("${B}", "xyz\\${C}"),
                        VariableMapping.create("${C}", "${A}\\${Z}"), VariableMapping.create("${D}", "${C}\\${Z}"))))
                                .hasSize(4)
                                .containsAllEntriesOf(ImmutableMap.of("${a}", "x\\y", "${b}", "xyz\\${C}", "${c}",
                                        "x\\y\\${Z}", "${d}", "x\\y\\${Z}\\${Z}"));

        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${A}", "${B}"),
                VariableMapping.create("${B}", "X"), VariableMapping.create("${C}", "${B}"),
                VariableMapping.create("${B}", "Y"), VariableMapping.create("${D}", "${B}")))).hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "${B}", "${b}", "Y", "${c}", "X", "${d}", "Y"));

        assertThat(VariableMappingsResolver.resolve(newArrayList(VariableMapping.create("${A}", "B"),
                VariableMapping.create("${B}", "X"), VariableMapping.create("${C}", "${${A}}")))).hasSize(3)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "B", "${b}", "X", "${c}", "${B}"));
    }
}
