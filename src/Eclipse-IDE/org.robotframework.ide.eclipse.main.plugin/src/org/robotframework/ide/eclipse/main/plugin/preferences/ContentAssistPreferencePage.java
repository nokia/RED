/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class ContentAssistPreferencePage extends RedFieldEditorPreferencePage {

    private Text charsTextControl;

    private Text delayTextControl;

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createLink(parent);
        createAutoActivationEditors(parent);
        createKeywordPrefixAutoAdditionEditor(parent);
        createLibraryImportAutoAdditionEditor(parent);
    }

    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .hint(150, SWT.DEFAULT)
                .span(2, 1)
                .grab(true, false)
                .applyTo(link);

        final String keysPageId = "org.eclipse.ui.preferencePages.Keys";

        final String text = "Robot content assistant preferences. See <a href=\"" + keysPageId
                + "\">'Keys'</a> to configure activation key binding.";
        link.setText(text);
        link.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            if (keysPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
    }

    private void createAutoActivationEditors(final Composite parent) {
        final Group autoActivationGroup = new Group(parent, SWT.NONE);
        autoActivationGroup.setText("Auto activation");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(autoActivationGroup);
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

        button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            delayTextControl.setEnabled(button.getSelection());
            charsTextControl.setEnabled(button.getSelection());
        }));

        final Label autoActivationDescription = new Label(autoActivationGroup, SWT.WRAP);
        autoActivationDescription.setText(
                "Completion can be triggered by user request or can be automatically triggered when one of specified characters is typed.");
        GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).indent(5, 2).span(2, 1).grab(true, false).applyTo(
                autoActivationDescription);
    }

    private void createKeywordPrefixAutoAdditionEditor(final Composite parent) {
        final Group keywordsGroup = new Group(parent, SWT.NONE);
        keywordsGroup.setText("Keywords");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(keywordsGroup);
        GridLayoutFactory.fillDefaults().applyTo(keywordsGroup);
        final BooleanFieldEditor automaticKeywordPrefixingEditor = new BooleanFieldEditor(
                RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED,
                "Automatically add library or resource name to keyword proposal insertion", keywordsGroup);
        addField(automaticKeywordPrefixingEditor);
        final Button button = (Button) automaticKeywordPrefixingEditor.getDescriptionControl(keywordsGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
    }

    private void createLibraryImportAutoAdditionEditor(final Composite parent) {
        final Group librariesGroup = new Group(parent, SWT.NONE);
        librariesGroup.setText("Libraries");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(librariesGroup);
        GridLayoutFactory.fillDefaults().applyTo(librariesGroup);
        final BooleanFieldEditor keywordLibraryImportEditor = new BooleanFieldEditor(
                RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED,
                "Include keywords from not imported libraries", librariesGroup);
        addField(keywordLibraryImportEditor);
        final Button button = (Button) keywordLibraryImportEditor.getDescriptionControl(librariesGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);

        final Label notImportedLibrariesDescription = new Label(librariesGroup, SWT.WRAP);
        notImportedLibrariesDescription.setText("When libraries are added to red.xml but not imported in robot file, "
                + "keywords from such libraries will be included in propositions.");
        GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).indent(5, 2).grab(true, false).applyTo(
                notImportedLibrariesDescription);
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
