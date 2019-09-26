/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LibraryPrefixStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LinkedModeStrategy;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;

public class ContentAssistPreferencePage extends RedFieldEditorPreferencePage {

    private Consumer<Boolean> autoActivationEnablementUpdater;

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench()
                .getHelpSystem()
                .setHelp(getControl(), RedPlugin.PLUGIN_ID + ".assist_preferences_page_context");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createLink(parent);
        createAutoInsertEditor(parent);
        createAutoActivationEditors(parent);
        createKeywordPrefixAutoAdditionEditor(parent);
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
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (keysPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
    }

    private void createAutoInsertEditor(final Composite parent) {
        final Group autoInsertGroup = new Group(parent, SWT.NONE);
        autoInsertGroup.setText("Auto insert");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(autoInsertGroup);
        GridLayoutFactory.fillDefaults().applyTo(autoInsertGroup);

        final BooleanFieldEditor autoInsertEnabled = new BooleanFieldEditor(
                RedPreferences.ASSISTANT_AUTO_INSERT_ENABLED, "Insert single proposals automatically", autoInsertGroup);
        addField(autoInsertEnabled);
        final Button button = (Button) autoInsertEnabled.getDescriptionControl(autoInsertGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
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

        final IntegerFieldEditor autoActivationDelay = new IntegerFieldEditor(
                RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY, "Auto activation delay (ms)", autoActivationGroup, 3);
        addField(autoActivationDelay);
        GridDataFactory.fillDefaults().indent(25, 2).applyTo(autoActivationDelay.getLabelControl(autoActivationGroup));

        final StringFieldEditor autoActivationChars = new StringFieldEditor(
                RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS, "Auto activation triggers", autoActivationGroup);
        addField(autoActivationChars);
        GridDataFactory.fillDefaults().indent(25, 2).applyTo(autoActivationChars.getLabelControl(autoActivationGroup));

        final Label autoActivationDescription = new Label(autoActivationGroup, SWT.WRAP);
        autoActivationDescription.setText(
                "Completion can be triggered by user request or can be automatically triggered when one of specified characters is typed.");
        GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).indent(5, 2).span(2, 1).grab(true, false).applyTo(
                autoActivationDescription);

        autoActivationEnablementUpdater = value -> {
            autoActivationDelay.setEnabled(value, autoActivationGroup);
            autoActivationChars.setEnabled(value, autoActivationGroup);
        };
        autoActivationEnablementUpdater
                .accept(getPreferenceStore().getBoolean(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED));
    }

    private void createKeywordPrefixAutoAdditionEditor(final Composite parent) {
        final Group keywordsGroup = new Group(parent, SWT.NONE);
        keywordsGroup.setText("Keywords");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(keywordsGroup);
        GridLayoutFactory.fillDefaults().applyTo(keywordsGroup);

        final BooleanFieldEditor keywordLibraryImportEditor = new BooleanFieldEditor(
                RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED,
                "Include keywords from red.xml libraries not imported in robot file", keywordsGroup);
        addField(keywordLibraryImportEditor);
        final Button keywordLibraryImportButton = (Button) keywordLibraryImportEditor
                .getDescriptionControl(keywordsGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(keywordLibraryImportButton);

        final ComboBoxFieldEditor automaticKeywordPrefixingEditor = new ComboBoxFieldEditor(
                RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION,
                "Add library or resource name to keyword proposal insertion", "", 5,
                createLibraryPrefixLabelsAndValues(), keywordsGroup);
        addField(automaticKeywordPrefixingEditor);

        final ComboBoxFieldEditor argumentsLinkedModeEditor = new ComboBoxFieldEditor(
                RedPreferences.ASSISTANT_LINKED_ARGUMENTS_MODE, "After pressing Tab in arguments edition mode", "", 5,
                createArgumentsLinkedModeLabelsAndValues(), keywordsGroup);
        addField(argumentsLinkedModeEditor);
    }

    private String[][] createLibraryPrefixLabelsAndValues() {
        return new String[][] { new String[] { "always", LibraryPrefixStrategy.ALWAYS.name() },
                new String[] { "only when conflict exists", LibraryPrefixStrategy.AUTOMATIC.name() },
                new String[] { "never", LibraryPrefixStrategy.NEVER.name() } };
    }

    private String[][] createArgumentsLinkedModeLabelsAndValues() {
        return new String[][] { new String[] { "cycle between arguments", LinkedModeStrategy.CYCLE.name() },
                new String[] { "exit on last argument", LinkedModeStrategy.EXIT_ON_LAST.name() } };
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED)) {
            autoActivationEnablementUpdater.accept((Boolean) event.getNewValue());
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        final boolean isAutoActivationEnabled = getPreferenceStore()
                .getDefaultBoolean(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED);
        autoActivationEnablementUpdater.accept(isAutoActivationEnabled);
    }
}
