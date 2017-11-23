/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

class FieldEditorPreferencePageHelper {

    @SuppressWarnings("unchecked")
    static List<FieldEditor> getEditors(final FieldEditorPreferencePage page) throws Exception {
        // there is no other way unless we override addField method and declare own editors
        // collection, but I prefer this small reflection than influencing production code this way
        // just for the purpose of testing
        final Field field = FieldEditorPreferencePage.class.getDeclaredField("fields");
        field.setAccessible(true);
        return (List<FieldEditor>) field.get(page);
    }

    static <T extends FieldEditor> List<T> getEditorsOfType(final FieldEditorPreferencePage page,
            final Class<T> editorType) throws Exception {
        // there is no other way unless we override addField method and declare own editors
        // collection, but I prefer this small reflection than influencing production code this way
        // just for the purpose of testing
        final Field field = FieldEditorPreferencePage.class.getDeclaredField("fields");
        field.setAccessible(true);
        return ((List<?>) field.get(page)).stream()
                .filter(editor -> editorType.isInstance(editor))
                .map(editor -> editorType.cast(editor))
                .collect(toList());
    }
}
