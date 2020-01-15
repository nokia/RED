/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class RegexValidatedStringFieldEditorTest {

    @FreshShell
    Shell shell;

    @Test
    public void stateIsValid_whenGivenValueMatchesRegex() {
        final RegexValidatedStringFieldEditor editor = new RegexValidatedStringFieldEditor("foo", "label", "^a+bc$",
                shell);

        editor.getTextControl().setText("abc");
        assertThat(editor.checkState()).isTrue();

        editor.getTextControl().setText("aabc");
        assertThat(editor.checkState()).isTrue();

        editor.getTextControl().setText("aaabc");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsInvalid_whenGivenValueDoesNotMatchRegex() {
        final RegexValidatedStringFieldEditor editor = new RegexValidatedStringFieldEditor("foo", "label", "^a+bc$",
                shell);

        editor.getTextControl().setText("bc");
        assertThat(editor.checkState()).isFalse();

        editor.getTextControl().setText("ac");
        assertThat(editor.checkState()).isFalse();

        editor.getTextControl().setText("abcc");
        assertThat(editor.checkState()).isFalse();
    }

}
