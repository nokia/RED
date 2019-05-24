/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.rf.ide.core.testdata.formatter.RedFormatter.FormattingSeparatorType;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FormatterType;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;


public class CodeFormatterPreferencePage extends RedFieldEditorPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.editor.formatter";

    private final Map<String, FormatterType> formatterLabelsAndValues = new LinkedHashMap<>();
    {
        formatterLabelsAndValues.put("Robot Tidy formatter", FormatterType.TIDY);
        formatterLabelsAndValues.put("RED formatter", FormatterType.CUSTOM);
    }

    private Composite parent;

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
        this.parent = parent;

        final RadioGroupFieldEditor formatterTypeEditor = new RadioGroupFieldEditor(RedPreferences.FORMATTER_TYPE,
                "Choose formatter", 1, createFormatterTypeLabelsAndValues(), parent);
        addField(formatterTypeEditor);
        GridDataFactory.fillDefaults().indent(0, 15).applyTo(formatterTypeEditor.getLabelControl(parent));
        GridDataFactory.fillDefaults().indent(0, 5).applyTo(formatterTypeEditor.getRadioBoxControl(parent));

        final BooleanFieldEditor separatorAdjustmentEditor = new BooleanFieldEditor(
                RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, "Adjust separator lengths", parent);
        addField(separatorAdjustmentEditor);
        GridDataFactory.fillDefaults().indent(25, 5).applyTo(separatorAdjustmentEditor.getDescriptionControl(parent));

        final ComboBoxFieldEditor separatorTypeEditor = new ComboBoxFieldEditor(RedPreferences.FORMATTER_SEPARATOR_TYPE,
                "Separator type", "", 50, createFormattingSeparatorTypeLabelsAndValues(), parent);
        addField(separatorTypeEditor);

        final IntegerFieldEditor separatorLengthEditor = new IntegerFieldEditor(
                RedPreferences.FORMATTER_SEPARATOR_LENGTH, "Separator length", parent, 2);
        separatorLengthEditor.setValidRange(2, 10);
        addField(separatorLengthEditor);
        GridDataFactory.fillDefaults().indent(50, 0).applyTo(separatorLengthEditor.getLabelControl(parent));

        final BooleanFieldEditor ignoreLongCellsEditor = new BooleanFieldEditor(
                RedPreferences.FORMATTER_IGNORE_LONG_CELLS_ENABLED,
                "Ignore cells longer than limit when adjusting dynamically", parent);
        addField(ignoreLongCellsEditor);
        GridDataFactory.fillDefaults().indent(50, 0).applyTo(ignoreLongCellsEditor.getDescriptionControl(parent));

        final IntegerFieldEditor ignoredCellsLengthEditor = new IntegerFieldEditor(
                RedPreferences.FORMATTER_LONG_CELL_LENGTH_LIMIT, "Cell length limit ", parent, 3);
        ignoredCellsLengthEditor.setValidRange(0, 999);
        addField(ignoredCellsLengthEditor);
        GridDataFactory.fillDefaults().indent(75, 0).applyTo(ignoredCellsLengthEditor.getLabelControl(parent));

        final BooleanFieldEditor rightTrimEditor = new BooleanFieldEditor(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED,
                "Right trim lines", parent);
        addField(rightTrimEditor);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(rightTrimEditor.getDescriptionControl(parent));
    }

    private String[][] createFormatterTypeLabelsAndValues() {
        final String[][] labelsAndValues = new String[2][];
        int i = 0;
        for (final Entry<String, FormatterType> entry : formatterLabelsAndValues.entrySet()) {
            labelsAndValues[i] = new String[] { entry.getKey(), entry.getValue().name() };
            i++;
        }
        return labelsAndValues;
    }

    private String[][] createFormattingSeparatorTypeLabelsAndValues() {
        return new String[][] {
                new String[] { "use constant number of spaces", FormattingSeparatorType.CONSTANT.name() },
                new String[] { "pad spaces in columns dynamically", FormattingSeparatorType.DYNAMIC.name() } };
    }

    @Override
    protected void initialize() {
        super.initialize();

        refreshEditorsEnablement();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof FieldEditor) {
            final FieldEditor editor = (FieldEditor) event.getSource();

            if (editor.getPreferenceName().equals(RedPreferences.FORMATTER_TYPE)
                    || editor.getPreferenceName().equals(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED)
                    || editor.getPreferenceName().equals(RedPreferences.FORMATTER_SEPARATOR_TYPE)
                    || editor.getPreferenceName().equals(RedPreferences.FORMATTER_IGNORE_LONG_CELLS_ENABLED)) {
                refreshEditorsEnablement();
            }
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        refreshEditorsEnablement();
    }

    private void refreshEditorsEnablement() {
        final RadioGroupFieldEditor typeEditor = (RadioGroupFieldEditor) getFieldEditor(RedPreferences.FORMATTER_TYPE);
        final BooleanFieldEditor adjustmentEditor = (BooleanFieldEditor) getFieldEditor(
                RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED);
        final ComboBoxFieldEditor sepTypeEditor = (ComboBoxFieldEditor) getFieldEditor(
                RedPreferences.FORMATTER_SEPARATOR_TYPE);
        final IntegerFieldEditor sepLengthEditor = (IntegerFieldEditor) getFieldEditor(
                RedPreferences.FORMATTER_SEPARATOR_LENGTH);
        final BooleanFieldEditor cellIgnoreEditor = (BooleanFieldEditor) getFieldEditor(
                RedPreferences.FORMATTER_IGNORE_LONG_CELLS_ENABLED);
        final IntegerFieldEditor cellLengthEditor = (IntegerFieldEditor) getFieldEditor(
                RedPreferences.FORMATTER_LONG_CELL_LENGTH_LIMIT);
        final BooleanFieldEditor rightTrimEditor = (BooleanFieldEditor) getFieldEditor(
                RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED);

        final boolean customEditorIsChosen = Stream.of(typeEditor.getRadioBoxControl(parent).getChildren())
                .map(Button.class::cast)
                .filter(Button::getSelection)
                .map(Button::getText)
                .map(lbl -> formatterLabelsAndValues.get(lbl))
                .findFirst()
                .get() == FormatterType.CUSTOM;
        final boolean adjustingIsEnabled = adjustmentEditor.getBooleanValue();
        final FormattingSeparatorType separatorType = FormattingSeparatorType.valueOf(sepTypeEditor.getSelectedValue());
        final boolean limitIsEnabled = cellIgnoreEditor.getBooleanValue();

        adjustmentEditor.setEnabled(customEditorIsChosen, parent);
        sepTypeEditor.setEnabled(customEditorIsChosen && adjustingIsEnabled, parent);
        sepLengthEditor.setEnabled(customEditorIsChosen && adjustingIsEnabled, parent);
        cellIgnoreEditor.setEnabled(
                customEditorIsChosen && adjustingIsEnabled && separatorType == FormattingSeparatorType.DYNAMIC, parent);
        cellLengthEditor.setEnabled(customEditorIsChosen && adjustingIsEnabled
                && separatorType == FormattingSeparatorType.DYNAMIC && limitIsEnabled, parent);
        rightTrimEditor.setEnabled(customEditorIsChosen, parent);

        if (!customEditorIsChosen) {
            setMessage("Robot Tidy formatter is able to format whole source only", IMessageProvider.WARNING);
        } else {
            setMessage(null, IMessageProvider.WARNING);
        }
    }
}
