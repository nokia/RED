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

public class AutoDiscoveringPreferencePage extends RedFieldEditorPreferencePage {

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();
        final Group libGroup = new Group(parent, SWT.NONE);
        libGroup.setText("Libraries Autodiscovering");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(libGroup);
        GridLayoutFactory.fillDefaults().applyTo(libGroup);

        final BooleanFieldEditor editor = new BooleanFieldEditor(
                RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED,
                "Add project modules recursively to PYTHONPATH/CLASSPATH during Autodiscovering on virtualenv",
                libGroup);
        addField(editor);
        final Button button = (Button) editor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
    }
}
