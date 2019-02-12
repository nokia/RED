/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class RegexStringFieldEditorTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void stateIsValid_whenGivenValueIsCorrectRegexPattern() {
        final RegexStringFieldEditor editor = new RegexStringFieldEditor("foo", "label", shellProvider.getShell());

        for (final String pattern : newHashSet("abc", "\\w+", "[\\w\\$@&{}]+")) {
            editor.getTextControl().setText(pattern);
            assertThat(editor.checkState()).isTrue();
        }
    }

    @Test
    public void stateIsInvalid_whenGivenValueIsIncorrectRegexPattern() {
        final RegexStringFieldEditor editor = new RegexStringFieldEditor("foo", "label", shellProvider.getShell());

        for (final String pattern : newHashSet("abc{", "{}", "+w")) {
            editor.getTextControl().setText(pattern);
            assertThat(editor.checkState()).isFalse();
        }
    }

    @Test
    public void stateIsValid_whenGivenValueIsEmpty_andEmptyStringsAreAllowed() {
        final RegexStringFieldEditor editor = new RegexStringFieldEditor("foo", "label", shellProvider.getShell());
        editor.setEmptyStringAllowed(true);

        editor.getTextControl().setText("");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsInvalid_whenGivenValueIsEmpty_andEmptyStringsAreNotAllowed() {
        final RegexStringFieldEditor editor = new RegexStringFieldEditor("foo", "label", shellProvider.getShell());
        editor.setEmptyStringAllowed(false);

        editor.getTextControl().setText("");
        assertThat(editor.checkState()).isFalse();
    }

}
