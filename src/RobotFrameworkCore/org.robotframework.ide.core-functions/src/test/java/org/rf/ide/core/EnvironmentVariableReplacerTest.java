/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentVariableReplacerTest {

    @Mock
    private SystemVariableAccessor variableAccessor;

    @Mock
    private Consumer<String> problemHandler;

    @Test
    public void testPathsWithoutEnvironmentVariables() throws Exception {
        final EnvironmentVariableReplacer replacer = new EnvironmentVariableReplacer();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("a/b/c/file.txt")).isFalse();
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("%/b/c/file.txt")).isFalse();
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("%{a/b/c/file.txt")).isFalse();
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("%a}/b/c/file.txt")).isFalse();
        });
    }

    @Test
    public void testPathsWithEnvironmentVariables() throws Exception {
        final EnvironmentVariableReplacer replacer = new EnvironmentVariableReplacer(variableAccessor);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("%{a}")).isTrue();
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("%{a}/%{b}/%{c}/file.txt")).isTrue();
            softly.assertThat(replacer.hasUnknownEnvironmentVariables("%{a%{b%{c}}}/file.txt")).isTrue();
        });
        verifyZeroInteractions(variableAccessor);
    }

    @Test
    public void allKnownVariablesAreReplaced() throws Exception {
        when(variableAccessor.getValue("VAR_1")).thenReturn(Optional.of("a"));
        when(variableAccessor.getValue("VAR_2")).thenReturn(Optional.of("b"));
        when(variableAccessor.getValue("VAR_3")).thenReturn(Optional.of("c"));

        final EnvironmentVariableReplacer replacer = new EnvironmentVariableReplacer(variableAccessor, problemHandler);

        assertThat(replacer.replaceKnownEnvironmentVariables("%{VAR_1}/%{VAR_2}/%{VAR_3}/file.txt"))
                .isEqualTo("a/b/c/file.txt");
        verifyZeroInteractions(problemHandler);
    }

    @Test
    public void nestedVariablesAreReplaced() throws Exception {
        when(variableAccessor.getValue("A")).thenReturn(Optional.of("A_VAR"));
        when(variableAccessor.getValue("A_VAR")).thenReturn(Optional.of("xyz"));
        when(variableAccessor.getValue("B")).thenReturn(Optional.of("B_VAR"));
        when(variableAccessor.getValue("X_B_VAR_Y")).thenReturn(Optional.of("C_VAR"));
        when(variableAccessor.getValue("C_VAR")).thenReturn(Optional.of("abc"));

        final EnvironmentVariableReplacer replacer = new EnvironmentVariableReplacer(variableAccessor, problemHandler);

        assertThat(replacer.replaceKnownEnvironmentVariables("%{%{A}}/%{%{X_%{B}_Y}}/file.txt"))
                .isEqualTo("xyz/abc/file.txt");
        verifyZeroInteractions(problemHandler);
    }

    @Test
    public void onlyKnownVariablesAreReplaced() throws Exception {
        when(variableAccessor.getValue("A")).thenReturn(Optional.of("a-value"));
        when(variableAccessor.getValue("B")).thenReturn(Optional.of("b-value"));
        when(variableAccessor.getValue("VAR_1")).thenReturn(Optional.empty());
        when(variableAccessor.getValue("VAR_2")).thenReturn(Optional.empty());

        final EnvironmentVariableReplacer replacer = new EnvironmentVariableReplacer(variableAccessor, problemHandler);

        assertThat(replacer.replaceKnownEnvironmentVariables("%{A}/%{VAR_1}/%{B}/%{VAR_2}/file.txt"))
                .isEqualTo("a-value/%{VAR_1}/b-value/%{VAR_2}/file.txt");
        verify(problemHandler).accept("VAR_1");
        verify(problemHandler).accept("VAR_2");
        verifyNoMoreInteractions(problemHandler);
    }

    @Test
    public void unknownVariableIsReportedOnlyOnce() throws Exception {
        when(variableAccessor.getValue("A")).thenReturn(Optional.empty());
        when(variableAccessor.getValue("B")).thenReturn(Optional.empty());
        when(variableAccessor.getValue("C")).thenReturn(Optional.empty());

        final EnvironmentVariableReplacer replacer = new EnvironmentVariableReplacer(variableAccessor, problemHandler);

        assertThat(replacer.replaceKnownEnvironmentVariables("%{A}/%{B}/%{A}/%{A}/%{C}/%{B}/file.txt"))
                .isEqualTo("%{A}/%{B}/%{A}/%{A}/%{C}/%{B}/file.txt");
        verify(problemHandler).accept("A");
        verify(problemHandler).accept("B");
        verify(problemHandler).accept("C");
        verifyNoMoreInteractions(problemHandler);
    }

}
