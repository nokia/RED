/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class LibrariesPreferencesPage extends RedFieldEditorPreferencePage {

    private Consumer<Boolean> discoveringSummaryEnablementUpdater;

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

        final BooleanFieldEditor discoveringEditor = new BooleanFieldEditor(RedPreferences.AUTO_DISCOVERING_ENABLED,
                "Auto discover libraries after test suite save action", discoveringGroup);

        addField(discoveringEditor);
        final Button discoveringButton = (Button) discoveringEditor.getDescriptionControl(discoveringGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(discoveringButton);

        final BooleanFieldEditor discoveringSummaryEditor = new BooleanFieldEditor(
                RedPreferences.AUTO_DISCOVERING_SUMMARY_WINDOW_ENABLED,
                "Show discovering summary after test suite save action", discoveringGroup);

        addField(discoveringSummaryEditor);
        final Button discoveringSummaryButton = (Button) discoveringSummaryEditor
                .getDescriptionControl(discoveringGroup);
        GridDataFactory.fillDefaults().indent(25, 5).applyTo(discoveringSummaryButton);

        final BooleanFieldEditor recursiveAdditionEditor = new BooleanFieldEditor(
                RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED,
                "Add project modules recursively to PYTHONPATH/CLASSPATH during autodiscovering on virtualenv",
                discoveringGroup);

        addField(recursiveAdditionEditor);
        final Button recursiveAdditionButton = (Button) recursiveAdditionEditor.getDescriptionControl(discoveringGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(recursiveAdditionButton);

        discoveringSummaryEnablementUpdater = value -> discoveringSummaryEditor.setEnabled(value, discoveringGroup);
        discoveringSummaryEnablementUpdater
                .accept(getPreferenceStore().getBoolean(RedPreferences.AUTO_DISCOVERING_ENABLED));
    }

    private void createLibdocGenerationEditors(final Composite parent) {
        final Group libGroup = new Group(parent, SWT.NONE);
        libGroup.setText("Libdoc generating");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(libGroup);
        GridLayoutFactory.fillDefaults().applyTo(libGroup);

        final BooleanFieldEditor libdocGenerationEditor = new BooleanFieldEditor(
                RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED,
                "Generate Python libraries libdocs in separate process", libGroup);

        addField(libdocGenerationEditor);
        final Button libdocGenerationButton = (Button) libdocGenerationEditor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(libdocGenerationButton);

        final BooleanFieldEditor libdocReloadEditor = new BooleanFieldEditor(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED,
                "Automatically reload changed libraries", libGroup);

        addField(libdocReloadEditor);
        final Button libdocReloadButton = (Button) libdocReloadEditor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(libdocReloadButton);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.AUTO_DISCOVERING_ENABLED)) {
            discoveringSummaryEnablementUpdater.accept((Boolean) event.getNewValue());
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        discoveringSummaryEnablementUpdater
                .accept(getPreferenceStore().getDefaultBoolean(RedPreferences.AUTO_DISCOVERING_ENABLED));
    }
}
