/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;


public class SaveActionsPreferencePage extends RedFieldEditorPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.editor.save";

    private Consumer<Boolean> formatEditorContentsEnablementUpdater;

    private Consumer<Boolean> discoveringSummaryEnablementUpdater;

    public SaveActionsPreferencePage() {
        setDescription("Perform selected actions on save");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createCodeFormattingEditors(parent);
        createAutodiscoveringEditors(parent);
    }

    private void createCodeFormattingEditors(final Composite parent) {
        final Group codeFormattingGroup = new Group(parent, SWT.NONE);
        codeFormattingGroup.setText("Source code formatting");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(codeFormattingGroup);
        GridLayoutFactory.fillDefaults().applyTo(codeFormattingGroup);

        final BooleanFieldEditor codeFormattingEditor = new BooleanFieldEditor(
                RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, "Format editor contents", codeFormattingGroup);
        addField(codeFormattingEditor);
        final Button codeFormattingEditorButton = (Button) codeFormattingEditor
                .getDescriptionControl(codeFormattingGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(codeFormattingEditorButton);

        final BooleanFieldEditor changedLinesOnlyEditor = new BooleanFieldEditor(
                RedPreferences.SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED, "Apply only on changed lines",
                codeFormattingGroup);
        addField(changedLinesOnlyEditor);
        final Button changedLinesOnlyButton = (Button) changedLinesOnlyEditor
                .getDescriptionControl(codeFormattingGroup);
        GridDataFactory.fillDefaults().indent(25, 5).applyTo(changedLinesOnlyButton);

        final Link preferenceLink = new Link(codeFormattingGroup, SWT.NONE);
        final String text = "Configure formatter settings on <a href=\"" + CodeFormatterPreferencePage.ID
                + "\">Code Formatter</a> preference page.";
        preferenceLink.setText(text);
        preferenceLink.addSelectionListener(widgetSelectedAdapter(e -> {
            if (CodeFormatterPreferencePage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(preferenceLink);

        formatEditorContentsEnablementUpdater = value -> {
            changedLinesOnlyEditor.setEnabled(value, codeFormattingGroup);
            preferenceLink.setEnabled(value);
        };
        formatEditorContentsEnablementUpdater
                .accept(getPreferenceStore().getBoolean(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED));
    }

    private void createAutodiscoveringEditors(final Composite parent) {
        final Group discoveringGroup = new Group(parent, SWT.NONE);
        discoveringGroup.setText("Libraries autodiscovering");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(discoveringGroup);
        GridLayoutFactory.fillDefaults().applyTo(discoveringGroup);

        final BooleanFieldEditor discoveringEditor = new BooleanFieldEditor(
                RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, "Discover unknown libraries", discoveringGroup);
        addField(discoveringEditor);
        final Button discoveringButton = (Button) discoveringEditor.getDescriptionControl(discoveringGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(discoveringButton);

        final BooleanFieldEditor discoveringSummaryEditor = new BooleanFieldEditor(
                RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_SUMMARY_WINDOW_ENABLED, "Show discovering summary",
                discoveringGroup);
        addField(discoveringSummaryEditor);
        final Button discoveringSummaryButton = (Button) discoveringSummaryEditor
                .getDescriptionControl(discoveringGroup);
        GridDataFactory.fillDefaults().indent(25, 5).applyTo(discoveringSummaryButton);

        final Link preferenceLink = new Link(discoveringGroup, SWT.NONE);
        final String text = "Configure libraries settings on <a href=\"" + LibrariesPreferencesPage.ID
                + "\">Libraries</a> preference page.";
        preferenceLink.setText(text);
        preferenceLink.addSelectionListener(widgetSelectedAdapter(e -> {
            if (LibrariesPreferencesPage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(preferenceLink);

        discoveringSummaryEnablementUpdater = value -> {
            discoveringSummaryEditor.setEnabled(value, discoveringGroup);
            preferenceLink.setEnabled(value);
        };
        discoveringSummaryEnablementUpdater
                .accept(getPreferenceStore().getBoolean(RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED));
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED)) {
            formatEditorContentsEnablementUpdater.accept((Boolean) event.getNewValue());
        } else if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED)) {
            discoveringSummaryEnablementUpdater.accept((Boolean) event.getNewValue());
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        formatEditorContentsEnablementUpdater
                .accept(getPreferenceStore().getDefaultBoolean(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED));
        discoveringSummaryEnablementUpdater
                .accept(getPreferenceStore().getDefaultBoolean(RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED));
    }
}
