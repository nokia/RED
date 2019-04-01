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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorFormatter.FormattingSeparatorType;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;


public class CodeFormatterPreferencePage extends RedFieldEditorPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.editor.formatter";

    private Consumer<Boolean> separatorAdjustmentEnablementUpdater;

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createLink(parent);

        createFormatterEditors(parent);
    }

    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .hint(150, SWT.DEFAULT)
                .span(2, 1)
                .grab(true, false)
                .applyTo(link);

        final String text = "See <a href=\"" + SaveActionsPreferencePage.ID
                + "\">'Save Actions'</a> to automatically format code on save.";
        link.setText(text);
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (SaveActionsPreferencePage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
    }

    private void createFormatterEditors(final Composite parent) {
        final Composite buttonsParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(buttonsParent);
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(buttonsParent);

        final BooleanFieldEditor separatorAdjustmentEditor = new BooleanFieldEditor(
                RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, "Adjust separator lengths", parent);
        addField(separatorAdjustmentEditor);

        final ComboBoxFieldEditor separatorTypeEditor = new ComboBoxFieldEditor(RedPreferences.FORMATTER_SEPARATOR_TYPE,
                "Separator type", "", 25, createFormattingSeparatorTypeLabelsAndValues(), parent);
        addField(separatorTypeEditor);

        final IntegerFieldEditor separatorLength = new IntegerFieldEditor(RedPreferences.FORMATTER_SEPARATOR_LENGTH,
                "Separator length", parent, 2);
        separatorLength.setValidRange(2, 10);
        addField(separatorLength);
        GridDataFactory.fillDefaults().indent(25, 2).applyTo(separatorLength.getLabelControl(parent));

        final BooleanFieldEditor rightTrimEditor = new BooleanFieldEditor(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED,
                "Right trim lines", parent);
        addField(rightTrimEditor);

        separatorAdjustmentEnablementUpdater = value -> {
            separatorTypeEditor.setEnabled(value, parent);
            separatorLength.setEnabled(value, parent);
        };
        separatorAdjustmentEnablementUpdater
                .accept(getPreferenceStore().getBoolean(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED));
    }

    private String[][] createFormattingSeparatorTypeLabelsAndValues() {
        return new String[][] {
                new String[] { "use constant number of spaces", FormattingSeparatorType.CONSTANT.name() },
                new String[] { "pad spaces in columns dynamically",
                        FormattingSeparatorType.DYNAMIC.name() } };
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED)) {
            separatorAdjustmentEnablementUpdater.accept((Boolean) event.getNewValue());
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        separatorAdjustmentEnablementUpdater
                .accept(getPreferenceStore().getDefaultBoolean(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED));
    }

}
