/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.rf.ide.core.testdata.model.VariableMappingsResolver.resolve;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;

import com.google.common.collect.ImmutableMap;

public class VariableMappingsResolverTest {

    @Test
    public void testDefaultMappingsAreAdded_whenProjectPathIsNotGiven() {
        assertThat(resolve(new ArrayList<>(), null)).hasSize(3)
                .containsAllEntriesOf(ImmutableMap.of("${/}", File.separator, "${curdir}", ".", "${space}", " "));
    }

    @Test
    public void testDefaultMappingsAreAdded_whenProjectPathIsGiven() {
        final String location = new File("/path/to/project").getAbsolutePath();
        assertThat(resolve(new ArrayList<>(), new File("/path/to/project"))).hasSize(5)
                .containsAllEntriesOf(ImmutableMap.of("${/}", File.separator, "${curdir}", ".", "${space}", " ",
                        "${execdir}", location, "${outputdir}", location));
    }

    @Test
    public void testSkippingIncorrectMappings() {
        assertThat(resolve(newArrayList(VariableMapping.create("AbC", "x"), VariableMapping.create("${D}", "y")), null))
                .hasSize(4)
                .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("$AbC", "x"), VariableMapping.create("${D}", "y")), null))
                        .hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("$AbC}", "x"), VariableMapping.create("${D}", "y")), null))
                        .hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("${AbC", "x"), VariableMapping.create("${D}", "y")), null))
                        .hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("{AbC", "x"), VariableMapping.create("${D}", "y")), null))
                        .hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("{AbC}", "x"), VariableMapping.create("${D}", "y")), null))
                        .hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("AbC}", "x"), VariableMapping.create("${D}", "y")), null))
                        .hasSize(4)
                        .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
        assertThat(resolve(newArrayList(VariableMapping.create("AbC", "x"), VariableMapping.create("${D}", "y")), null))
                .hasSize(4)
                .containsAllEntriesOf(ImmutableMap.of("${d}", "y"));
    }

    @Test
    public void testSkippingNonScalarOrNonEnvironmentMappings() {
        final List<VariableMapping> mappings = newArrayList(VariableMapping.create("${a}", "x"),
                VariableMapping.create("@{b}", "x"), VariableMapping.create("&{c}", "x"),
                VariableMapping.create("${D}", "x"), VariableMapping.create("%{E}", "x"));
        assertThat(resolve(mappings, null)).hasSize(6)
                .containsAllEntriesOf(ImmutableMap.of("${a}", "x", "${d}", "x", "%{e}", "x"));
    }

    @Test
    public void testResolvingMappingsWithoutVariablesInValues() {
        assertThat(
                resolve(newArrayList(VariableMapping.create("${AbC}", "x/y"), VariableMapping.create("${dEf}", "y/z"),
                        VariableMapping.create("${G_H I}", "a/b/c"), VariableMapping.create("%{jkl}", "e/n/v")), null))
                                .hasSize(7)
                                .containsAllEntriesOf(ImmutableMap.of("${abc}", "x/y", "${def}", "y/z", "${ghi}",
                                        "a/b/c", "%{jkl}", "e/n/v"));

        assertThat(resolve(
                newArrayList(VariableMapping.create("${AbC}", "x\\y"), VariableMapping.create("${dEf}", "y\\z"),
                        VariableMapping.create("${G_H I}", "a\\b\\c"), VariableMapping.create("%{jkl}", "e\\n\\v")),
                null)).hasSize(7).containsAllEntriesOf(
                        ImmutableMap.of("${abc}", "x\\y", "${def}", "y\\z", "${ghi}", "a\\b\\c", "%{jkl}", "e\\n\\v"));
    }

    @Test
    public void testResolvingMappingsWithVariablesInValues_whenThoseVariablesAreDefault() {
        final String location = new File("/path/to/project").getAbsolutePath();

        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "x${/}y")), null)).hasSize(4)
                .containsAllEntriesOf(ImmutableMap.of("${a}", "x" + File.separator + "y"));
        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "x${space}y")), null)).hasSize(4)
                .containsAllEntriesOf(ImmutableMap.of("${a}", "x y"));
        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "${curdir}/x/y")), null)).hasSize(4)
                .containsAllEntriesOf(ImmutableMap.of("${a}", "./x/y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("${A}", "${execdir}/x/y")), new File("/path/to/project")))
                        .hasSize(6)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", location + "/x/y"));
        assertThat(
                resolve(newArrayList(VariableMapping.create("${A}", "${outputdir}/x/y")), new File("/path/to/project")))
                        .hasSize(6)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", location + "/x/y"));

    }

    @Test
    public void testResolvingMappingsWithVariablesInValues_whenAllVariablesAreKnown() {
        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "x/y"), VariableMapping.create("${B}", "${A}/z"),
                VariableMapping.create("${C}", "${A}/${A}"), VariableMapping.create("%{E}", "${C}"),
                VariableMapping.create("%{F}", "%{E}/%{E}")), null)).hasSize(8)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x/y", "${b}", "x/y/z", "${c}", "x/y/x/y", "%{e}",
                                "x/y/x/y", "%{f}", "x/y/x/y/x/y/x/y"));

        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "x\\y"),
                VariableMapping.create("${B}", "${A}\\z"), VariableMapping.create("${C}", "${A}\\${A}"),
                VariableMapping.create("%{E}", "${C}"), VariableMapping.create("%{F}", "%{E}\\%{E}")), null)).hasSize(8)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x\\y", "${b}", "x\\y\\z", "${c}", "x\\y\\x\\y",
                                "%{e}", "x\\y\\x\\y", "%{f}", "x\\y\\x\\y\\x\\y\\x\\y"));

        assertThat(resolve(newArrayList(VariableMapping.create("${Abc}", "x"),
                VariableMapping.create("${dEf}", "${A B C}y"), VariableMapping.create("${GHI}", "${D_ e_ F_}z"),
                VariableMapping.create("%{EnV}", "${Abc}"), VariableMapping.create("%{F}", "%{En_v}")), null))
                        .hasSize(8)
                        .containsAllEntriesOf(ImmutableMap.of("${abc}", "x", "${def}", "xy", "${ghi}", "xyz", "%{env}",
                                "x", "%{f}", "x"));
    }

    @Test
    public void testResolvingMappingsWithVariablesInValues_whenSomeVariablesAreUnknown() {
        assertThat(resolve(
                newArrayList(VariableMapping.create("${A}", "x/y"), VariableMapping.create("${B}", "xyz/${C}"),
                        VariableMapping.create("${C}", "${A}/${Z}"), VariableMapping.create("${D}", "${C}/${Z}")),
                null)).hasSize(7)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x/y", "${b}", "xyz/${C}", "${c}", "x/y/${Z}",
                                "${d}", "x/y/${Z}/${Z}"));

        assertThat(resolve(
                newArrayList(VariableMapping.create("${A}", "x\\y"), VariableMapping.create("${B}", "xyz\\${C}"),
                        VariableMapping.create("${C}", "${A}\\${Z}"), VariableMapping.create("${D}", "${C}\\${Z}")),
                null)).hasSize(7)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "x\\y", "${b}", "xyz\\${C}", "${c}", "x\\y\\${Z}",
                                "${d}", "x\\y\\${Z}\\${Z}"));

        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "${B}"), VariableMapping.create("${B}", "X"),
                VariableMapping.create("${C}", "${B}"), VariableMapping.create("${B}", "Y"),
                VariableMapping.create("${D}", "${B}")), null)).hasSize(7)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "${B}", "${b}", "Y", "${c}", "X", "${d}", "Y"));

        assertThat(resolve(newArrayList(VariableMapping.create("${A}", "B"), VariableMapping.create("${B}", "X"),
                VariableMapping.create("${C}", "${${A}}")), null)).hasSize(6)
                        .containsAllEntriesOf(ImmutableMap.of("${a}", "B", "${b}", "X", "${c}", "${B}"));
    }
}
