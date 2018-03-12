/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;

public class RedXmlEditorPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RedXmlEditorPropertyTester tester = new RedXmlEditorPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotRedXmlEditor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + RedProjectEditor.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(mock(RedProjectEditor.class),
                RedXmlEditorPropertyTester.RED_XML_IS_EDITABLE, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        assertThat(tester.test(mock(RedProjectEditor.class), "unknown_property", null, true)).isFalse();
        assertThat(tester.test(mock(RedProjectEditor.class), "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testRedXmlIsEditableProperty() {
        final RedProjectEditor editorWithEditableFile = mock(RedProjectEditor.class);
        final RedProjectEditor editorWithNonEditableFile = mock(RedProjectEditor.class);

        final RedProjectEditorInput editableInput = new RedProjectEditorInput(Optional.<IFile> empty(), true, null);
        final RedProjectEditorInput nonEditableInput = new RedProjectEditorInput(Optional.<IFile> empty(), false,
                null);

        when(editorWithEditableFile.getRedProjectEditorInput()).thenReturn(editableInput);
        when(editorWithNonEditableFile.getRedProjectEditorInput()).thenReturn(nonEditableInput);

        assertThat(redXmlIsEditable(editorWithEditableFile, true)).isTrue();
        assertThat(redXmlIsEditable(editorWithEditableFile, false)).isFalse();

        assertThat(redXmlIsEditable(editorWithNonEditableFile, true)).isFalse();
        assertThat(redXmlIsEditable(editorWithNonEditableFile, false)).isTrue();
    }

    private boolean redXmlIsEditable(final RedProjectEditor editor, final boolean expected) {
        return tester.test(editor, RedXmlEditorPropertyTester.RED_XML_IS_EDITABLE, null, expected);
    }
}
