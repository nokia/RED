/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class LibrariesPreferencesPage extends RedFieldEditorPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.libraries";

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createAutodiscoveringEditors(parent);
        createLibdocGenerationEditors(parent);
    }

    private void createAutodiscoveringEditors(final Composite parent) {
        final Group discoveringGroup = new Group(parent, SWT.NONE);
        discoveringGroup.setText("Libraries autodiscovering");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(discoveringGroup);
        GridLayoutFactory.fillDefaults().applyTo(discoveringGroup);

        final BooleanFieldEditor recursiveAdditionEditor = new BooleanFieldEditor(
                RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED,
                "Add project modules recursively to PYTHONPATH/CLASSPATH during autodiscovering on virtualenv",
                discoveringGroup);
        final Button recursiveAdditionButton = (Button) recursiveAdditionEditor.getDescriptionControl(discoveringGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(recursiveAdditionButton);
        addField(recursiveAdditionEditor);

        final BooleanFieldEditor geventSupportEditor = new BooleanFieldEditor(
                RedPreferences.AUTODISCOVERY_GEVENT_SUPPORT, "Support Gevent during autodiscovery",
                discoveringGroup);
        final Button geventSupportButton = (Button) geventSupportEditor.getDescriptionControl(discoveringGroup);
        geventSupportButton.setToolTipText(
                "When this option is on the autodiscovery will support libraries using Gevent");
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(geventSupportButton);
        addField(geventSupportEditor);

        final Link link = new Link(discoveringGroup, SWT.NONE);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(link);
        final String text = "See <a href=\"" + SaveActionsPreferencePage.ID
                + "\">'Save Actions'</a> to trigger library autodiscovering on save.";
        link.setText(text);
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (SaveActionsPreferencePage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
    }

    private void createLibdocGenerationEditors(final Composite parent) {
        final Group libGroup = new Group(parent, SWT.NONE);
        libGroup.setText("Libdoc generating");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(libGroup);
        GridLayoutFactory.fillDefaults().applyTo(libGroup);

        final BooleanFieldEditor libdocGenerationEditor = new BooleanFieldEditor(
                RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED,
                "Generate Python libraries libdocs in separate process", libGroup);
        final Button libdocGenerationButton = (Button) libdocGenerationEditor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(libdocGenerationButton);
        addField(libdocGenerationEditor);

        final BooleanFieldEditor libdocReloadEditor = new BooleanFieldEditor(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED,
                "Automatically reload changed libraries", libGroup);
        final Button libdocReloadButton = (Button) libdocReloadEditor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(libdocReloadButton);
        addField(libdocReloadEditor);
    }
}
