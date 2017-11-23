/*
 * Copyright 2017 Nokia Solutions and Networks
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class LaunchingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public LaunchingPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
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
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (launchingPageId.equals(e.text)) {
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
                } else if (runDebugPageId.equals(e.text)) {
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null,
                            "selectFont:org.robotframework.ide.textfont");
                }
            }
        });
    }

    private void createLaunchingGroup(final Composite parent) {
        final Group robotGroup = new Group(parent, SWT.NONE);
        robotGroup.setText("Robot");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(robotGroup);
        GridLayoutFactory.fillDefaults().applyTo(robotGroup);

        final BooleanFieldEditor agentFileEditor = new BooleanFieldEditor(RedPreferences.LAUNCH_USE_ARGUMENT_FILE,
                "Pass Robot arguments using arguments file", robotGroup);
        final Button button = (Button) agentFileEditor.getDescriptionControl(robotGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        addField(agentFileEditor);

        final BooleanFieldEditor singleFileDataSourceEditor = new BooleanFieldEditor(
                RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, "Run single suite using suite path", robotGroup);
        final Button singleFileDataSourceButton = (Button) singleFileDataSourceEditor.getDescriptionControl(robotGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(singleFileDataSourceButton);
        addField(singleFileDataSourceEditor);

        final Label singleFileDataSourceDescription = new Label(robotGroup, SWT.WRAP);
        singleFileDataSourceDescription.setText(
                "When single suite or test cases from one suite are run, path to test suite will be used instead of path to project. "
                        + "Robot __init__ files will not be used then.");
        GridDataFactory.fillDefaults()
                .hint(150, SWT.DEFAULT)
                .indent(5, 2)
                .grab(true, false)
                .applyTo(singleFileDataSourceDescription);
    }

    private void createExecutorGroup(final Composite parent) {
        final Group executableGroup = new Group(parent, SWT.NONE);
        executableGroup.setText("Executor");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(executableGroup);
        GridLayoutFactory.fillDefaults().applyTo(executableGroup);

        final BooleanFieldEditor editor = new BooleanFieldEditor(RedPreferences.LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT,
                "Use single argument to pass robot execution command line", executableGroup);
        final Button button = (Button) editor.getDescriptionControl(executableGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        addField(editor);
    }

    private void createViewsGroup(final Composite parent) {
        final Group viewsGroup = new Group(parent, SWT.NONE);
        viewsGroup.setText("Views");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(viewsGroup);
        GridLayoutFactory.fillDefaults().applyTo(viewsGroup);

        final BooleanFieldEditor editor = new BooleanFieldEditor(RedPreferences.LIMIT_MSG_LOG_OUTPUT,
                "Limit Message Log output", viewsGroup);
        final Button button = (Button) editor.getDescriptionControl(viewsGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        addField(editor);

        final IntegerFieldEditor limitEditor = new IntegerFieldEditor(RedPreferences.LIMIT_MSG_LOG_LENGTH,
                "Buffer size (characters)", viewsGroup, 7);
        button.addSelectionListener(SelectionListener
                .widgetSelectedAdapter(e -> limitEditor.setEnabled(button.getSelection(), viewsGroup)));
        final Label limitLabel = limitEditor.getLabelControl(viewsGroup);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(limitLabel);
        limitEditor.setValidRange(0, 9_999_999);
        limitEditor.setEnabled(getPreferenceStore().getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT), viewsGroup);
        addField(limitEditor);
    }
}
