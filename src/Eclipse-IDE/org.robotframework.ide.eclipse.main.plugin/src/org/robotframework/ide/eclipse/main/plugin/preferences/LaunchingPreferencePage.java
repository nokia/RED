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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
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

        final Group generalGroup = new Group(parent, SWT.NONE);
        generalGroup.setText("General");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(generalGroup);
        GridLayoutFactory.fillDefaults().applyTo(generalGroup);

        final BooleanFieldEditor editor = new BooleanFieldEditor(RedPreferences.LAUNCH_USE_ARGUMENT_FILE,
                "Pass Robot arguments using arguments file", generalGroup);
        final Button button = (Button) editor.getDescriptionControl(generalGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
        addField(editor);
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
}
