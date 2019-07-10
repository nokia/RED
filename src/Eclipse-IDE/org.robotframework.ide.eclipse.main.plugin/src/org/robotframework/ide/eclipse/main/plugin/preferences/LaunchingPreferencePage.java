/*
 * Copyright 2017 Nokia Solutions and Networks
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

public class LaunchingPreferencePage extends RedFieldEditorPreferencePage {

    private Consumer<Boolean> messageLogLimitEnablementUpdater;

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench()
                .getHelpSystem()
                .setHelp(getControl(), RedPlugin.PLUGIN_ID + ".launching_preferences_page_context");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createLink(parent);
        createLaunchingGroup(parent);
        createExecutorGroup(parent);
        createViewsGroup(parent);
    }

    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .hint(150, SWT.DEFAULT)
                .span(2, 1)
                .grab(true, false)
                .applyTo(link);

        final String launchingPageId = "org.eclipse.debug.ui.LaunchingPreferencePage";
        final String runDebugPageId = "org.eclipse.debug.ui.DebugPreferencePage";

        final String text = "Robot tests launching preferences. See <a href=\"" + launchingPageId
                + "\">'Launching'</a> for general launching preferences " + "or <a href=\"" + runDebugPageId
                + "\">'Run/Debug'</a> for other related preferences.";
        link.setText(text);
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (launchingPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            } else if (runDebugPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null,
                        "selectFont:org.robotframework.ide.textfont");
            }
        }));
    }

    private void createLaunchingGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Robot");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(group);
        GridLayoutFactory.fillDefaults().applyTo(group);

        final BooleanFieldEditor agentFileEditor = new BooleanFieldEditor(RedPreferences.LAUNCH_USE_ARGUMENT_FILE,
                "Pass Robot arguments using arguments file", group);
        final Button button = (Button) agentFileEditor.getDescriptionControl(group);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        addField(agentFileEditor);

        final BooleanFieldEditor singleFileDataSourceEditor = new BooleanFieldEditor(
                RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, "Pass selected suite as data source", group);
        final Button singleFileDataSourceButton = (Button) singleFileDataSourceEditor.getDescriptionControl(group);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(singleFileDataSourceButton);
        addField(singleFileDataSourceEditor);

        final Label singleFileDataSourceDescription = new Label(group, SWT.WRAP);
        singleFileDataSourceDescription.setText(
                "When single suite or test cases from one suite are run, path to suite instead of path to project will be used as data source. "
                        + "Robot __init__ files from outside that data source will not be run.");
        GridDataFactory.fillDefaults()
                .hint(150, SWT.DEFAULT)
                .indent(5, 2)
                .grab(true, false)
                .applyTo(singleFileDataSourceDescription);
    }

    private void createExecutorGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Executor");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(group);
        GridLayoutFactory.fillDefaults().applyTo(group);

        final BooleanFieldEditor singleCommandLineArgumentEditor = new BooleanFieldEditor(
                RedPreferences.LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT,
                "Pass Robot execution command line as single argument", group);
        final Button button = (Button) singleCommandLineArgumentEditor.getDescriptionControl(group);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        addField(singleCommandLineArgumentEditor);

        final Label singleCommandLineArgumentDescription = new Label(group, SWT.WRAP);
        singleCommandLineArgumentDescription.setText(
                "When tests are launched using custom executable file the actual command line call will be passed to it as a single argument.");
        GridDataFactory.fillDefaults()
                .hint(150, SWT.DEFAULT)
                .indent(5, 2)
                .grab(true, false)
                .applyTo(singleCommandLineArgumentDescription);
    }

    private void createViewsGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Views");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(group);
        GridLayoutFactory.fillDefaults().applyTo(group);

        final BooleanFieldEditor limitEditor = new BooleanFieldEditor(RedPreferences.LIMIT_MSG_LOG_OUTPUT,
                "Limit Message Log output", group);
        final Button button = (Button) limitEditor.getDescriptionControl(group);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        addField(limitEditor);

        final IntegerFieldEditor limitLengthEditor = new IntegerFieldEditor(RedPreferences.LIMIT_MSG_LOG_LENGTH,
                "Buffer size (characters)", group, 7);
        final Label limitLabel = limitLengthEditor.getLabelControl(group);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(limitLabel);
        limitLengthEditor.setValidRange(0, 9_999_999);
        limitLengthEditor.setEnabled(getPreferenceStore().getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT), group);
        addField(limitLengthEditor);

        messageLogLimitEnablementUpdater = value -> limitLengthEditor.setEnabled(value, group);
        messageLogLimitEnablementUpdater.accept(getPreferenceStore().getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT));
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.LIMIT_MSG_LOG_OUTPUT)) {
            messageLogLimitEnablementUpdater.accept((Boolean) event.getNewValue());
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        messageLogLimitEnablementUpdater
                .accept(getPreferenceStore().getDefaultBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT));
    }
}
