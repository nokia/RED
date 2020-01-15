/*
 * Copyright 2019 Nokia Solutions and Networks
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
public class RegexValidatedMultilineStringFieldEditorTest {

    @FreshShell
    Shell shell;

    @Test
    public void stateIsValid_whenGivenValueMatchesRegex() {
        final RegexValidatedMultilineStringFieldEditor editor = new RegexValidatedMultilineStringFieldEditor("foo",
                "label", 10, 5, MultiLineStringFieldEditor.VALIDATE_ON_KEY_STROKE, "[^\t]*", shell);

        editor.getTextControl().setText("abc");
        assertThat(editor.checkState()).isTrue();

        editor.getTextControl().setText("abc\ndef");
        assertThat(editor.checkState()).isTrue();

        editor.getTextControl().setText("abc\ndef\nghi");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsInvalid_whenGivenValueDoesNotMatchRegex() {
        final RegexValidatedMultilineStringFieldEditor editor = new RegexValidatedMultilineStringFieldEditor("foo",
                "label", 10, 5, MultiLineStringFieldEditor.VALIDATE_ON_KEY_STROKE, "[^\t]*", shell);

        editor.getTextControl().setText("\t\t\t");
        assertThat(editor.checkState()).isFalse();

        editor.getTextControl().setText("a\tb");
        assertThat(editor.checkState()).isFalse();

        editor.getTextControl().setText("a\nb\nc\td");
        assertThat(editor.checkState()).isFalse();
    }

}
