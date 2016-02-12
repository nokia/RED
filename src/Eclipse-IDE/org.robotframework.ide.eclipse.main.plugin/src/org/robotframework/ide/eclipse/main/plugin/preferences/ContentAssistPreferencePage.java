/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;


public class ContentAssistPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private Text charsTextControl;
    private Text delayTextControl;

    public ContentAssistPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
        setDescription("Configure preferences for content asisstant in Red Source Editor");
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createAutoActivationEditors(parent);
        createAcceptanceModeEditors(parent);
        createKeywordPrefixAutoAdditionEditor(parent);
    }

    protected void createAutoActivationEditors(final Composite parent) {
        final Group autoActivationGroup = new Group(parent, SWT.NONE);
        autoActivationGroup.setText("Auto activation");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(autoActivationGroup);
        GridLayoutFactory.fillDefaults().applyTo(autoActivationGroup);

        final BooleanFieldEditor autoActivationEnabled = new BooleanFieldEditor(
                RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED, "Auto activation enabled", autoActivationGroup);
        addField(autoActivationEnabled);
        final Button button = (Button) autoActivationEnabled.getDescriptionControl(autoActivationGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
        final boolean isAutoActivationEnabled = RedPlugin.getDefault()
                .getPreferences()
                .isAssistantAutoActivationEnabled();

        final IntegerFieldEditor autoActivationDelay = new IntegerFieldEditor(
                RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY, "Auto activation delay (ms)", autoActivationGroup, 3);
        addField(autoActivationDelay);
        delayTextControl = autoActivationDelay.getTextControl(autoActivationGroup);
        delayTextControl.setEnabled(isAutoActivationEnabled);
        GridDataFactory.fillDefaults().indent(25, 2).applyTo(autoActivationDelay.getLabelControl(autoActivationGroup));
        
        final StringFieldEditor autoActivationChars = new StringFieldEditor(
                RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS, "Auto activation triggers", autoActivationGroup);
        addField(autoActivationChars);
        charsTextControl = autoActivationChars.getTextControl(autoActivationGroup);
        charsTextControl.setEnabled(isAutoActivationEnabled);
        GridDataFactory.fillDefaults().indent(25, 2).applyTo(autoActivationChars.getLabelControl(autoActivationGroup));

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                delayTextControl.setEnabled(button.getSelection());
                charsTextControl.setEnabled(button.getSelection());
            }
        });

    }

    private void createAcceptanceModeEditors(final Composite parent) {
        final RadioGroupFieldEditor insertionModeEditor = new RadioGroupFieldEditor(
                RedPreferences.ASSISTANT_COMPLETION_MODE, "Proposals", 2, createModes(), parent, true);
        addField(insertionModeEditor);
    }

    private String[][] createModes() {
        return new String[][] { new String[] { "Completion inserts", AcceptanceMode.INSERT.name() },
                new String[] { "Completion overrides", AcceptanceMode.SUBSTITUTE.name() } };
    }
    
    private void createKeywordPrefixAutoAdditionEditor(final Composite parent) {
        final Group keywordsGroup = new Group(parent, SWT.NONE);
        keywordsGroup.setText("Keywords");
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(keywordsGroup);
        GridLayoutFactory.fillDefaults().applyTo(keywordsGroup);
        final BooleanFieldEditor automaticKeywordPrefixingEditor = new BooleanFieldEditor(
                RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED,
                "Automatically add library or resource name to keyword proposal insertion", keywordsGroup);
        addField(automaticKeywordPrefixingEditor);
        final Button button = (Button) automaticKeywordPrefixingEditor.getDescriptionControl(keywordsGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        final boolean isAutoActivationEnabled = getPreferenceStore()
                .getDefaultBoolean(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED);
        delayTextControl.setEnabled(isAutoActivationEnabled);
        charsTextControl.setEnabled(isAutoActivationEnabled);
    }
}
