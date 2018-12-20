/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class LibrariesPreferencesPage extends RedFieldEditorPreferencePage {

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createAutodiscoveringEditor(parent);
        createLibdocGenerationEditor(parent);
    }

    private void createAutodiscoveringEditor(final Composite parent) {
        final Group libGroup = new Group(parent, SWT.NONE);
        libGroup.setText("Libraries autodiscovering");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(libGroup);
        GridLayoutFactory.fillDefaults().applyTo(libGroup);

        final BooleanFieldEditor editor = new BooleanFieldEditor(
                RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED,
                "Add project modules recursively to PYTHONPATH/CLASSPATH during autodiscovering on virtualenv",
                libGroup);

        addField(editor);
        final Button button = (Button) editor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
    }

    private void createLibdocGenerationEditor(final Composite parent) {
        final Group libGroup = new Group(parent, SWT.NONE);
        libGroup.setText("Libdoc generating");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(libGroup);
        GridLayoutFactory.fillDefaults().applyTo(libGroup);

        final BooleanFieldEditor editor = new BooleanFieldEditor(
                RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED,
                "Generate Python libraries libdocs in separate process", libGroup);

        addField(editor);
        final Button button = (Button) editor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
    }
}
