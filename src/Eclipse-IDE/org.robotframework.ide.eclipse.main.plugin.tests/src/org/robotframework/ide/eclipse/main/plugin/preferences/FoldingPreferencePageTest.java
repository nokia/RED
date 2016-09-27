/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

public class FoldingPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final FoldingPreferencePage page = new FoldingPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void thereAreBooleanEditorsForEachFoldableElementAndIntegerEditorForLinespan() throws Exception {
        final FoldingPreferencePage page = new FoldingPreferencePage();
        page.createControl(shellProvider.getShell());

        // there is no other way unless we override addField method and declare own editors
        // collection, but I prefer this small reflection than influencing production code this way
        // just for the purpose of testing
        final Field field = FieldEditorPreferencePage.class.getDeclaredField("fields");
        field.setAccessible(true);
        final List<?> editors = (List<?>) field.get(page);

        final List<String> booleanPrefNames = newArrayList(RedPreferences.FOLDABLE_SECTIONS,
                RedPreferences.FOLDABLE_CASES, RedPreferences.FOLDABLE_KEYWORDS, RedPreferences.FOLDABLE_DOCUMENTATION);

        assertThat(editors).hasSize(5);
        for (final Object ed : editors) {
            final FieldEditor editor = (FieldEditor) ed;

            if (editor instanceof BooleanFieldEditor) {
                booleanPrefNames.remove(editor.getPreferenceName());
            } else if (editor instanceof IntegerFieldEditor) {
                final IntegerFieldEditor lineLimitEditor = (IntegerFieldEditor) editor;
                assertThat(lineLimitEditor.getPreferenceName()).isEqualTo(RedPreferences.FOLDING_LINE_LIMIT);
            }
        }
        assertThat(booleanPrefNames).isEmpty();
    }
}
