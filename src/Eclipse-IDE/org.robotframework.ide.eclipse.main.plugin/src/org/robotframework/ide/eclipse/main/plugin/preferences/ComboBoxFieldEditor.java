/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Custom combo field editor capable of showing line between label and combo
 * (similarly like field editors on Java -> Compiler -> Error/Warnings
 * preference page.
 * 
 * @{author Michal Anglart
 */
public class ComboBoxFieldEditor extends FieldEditor {

    private final int labelIndent;

    private Combo combo;

    private final String tooltip;

    private String value;

    private final String[][] entryNamesAndValues;

    public ComboBoxFieldEditor(final String name, final String labelText, final String tooltip,
            final String[][] entryNamesAndValues, final Composite parent) {
        this(name, labelText, tooltip, 0, entryNamesAndValues, parent);
    }

    /**
     * Create the combo box field editor.
     * 
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param entryNamesAndValues
     *            the names (labels) and underlying values to populate the combo
     *            widget. These should be arranged as: { {name1, value1},
     *            {name2, value2}, ...}
     * @param parent
     *            the parent composite
     * @param labelIndent
     */
    public ComboBoxFieldEditor(final String name, final String labelText, final String tooltip, final int labelIndent,
            final String[][] entryNamesAndValues, final Composite parent) {
        init(name, labelText);
        Assert.isTrue(checkArray(entryNamesAndValues));
        this.tooltip = tooltip;
        this.entryNamesAndValues = entryNamesAndValues;
        this.labelIndent = labelIndent;
        createControl(parent);
    }

    /**
     * Checks whether given <code>String[][]</code> is of "type"
     * <code>String[][2]</code>.
     *
     * @return <code>true</code> if it is ok, and <code>false</code> otherwise
     */
    private boolean checkArray(final String[][] table) {
        if (table == null) {
            return false;
        }
        for (int i = 0; i < table.length; i++) {
            final String[] array = table[i];
            if (array == null || array.length != 2) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void adjustForNumColumns(final int numColumns) {
        if (numColumns > 1) {
            final Label label = getLabelControl();
            int left = numColumns;
            if (label != null) {
                label.setToolTipText(tooltip);
                final GridData labelGridData = (GridData) label.getLayoutData();
                labelGridData.horizontalSpan = 1;
                labelGridData.grabExcessHorizontalSpace = true;
                labelGridData.horizontalIndent = labelIndent;
                left = left - 1;
            }
            ((GridData) combo.getLayoutData()).horizontalSpan = left;
        } else {
            final Label label = getLabelControl();
            if (label != null) {
                label.setToolTipText(tooltip);
                final GridData labelGridData = (GridData) label.getLayoutData();
                labelGridData.horizontalSpan = 1;
                labelGridData.horizontalIndent = labelIndent;
            }
            ((GridData) combo.getLayoutData()).horizontalSpan = 1;
        }
        combo.setToolTipText(tooltip);
    }

    @Override
    protected void doFillIntoGrid(final Composite parent, final int numColumns) {
        final int comboSpan = numColumns <= 1 ? 1 : numColumns - 1;

        final Label label = getLabelControl(parent);
        GridDataFactory.swtDefaults().applyTo(label);

        final Combo comboBox = getComboBoxControl(parent);
        GridDataFactory.swtDefaults().span(comboSpan, 1).align(GridData.FILL, GridData.CENTER).applyTo(comboBox);

        installLineLinkingCapability(parent, label, comboBox);
    }

    private void installLineLinkingCapability(final Composite parent, final Label label, final Combo comboBox) {
        final AtomicBoolean shouldShowLine = new AtomicBoolean(false);
        final MouseTrackAdapter controlsMouseListener = new MouseTrackAdapter() {
            @Override
            public void mouseEnter(final MouseEvent e) {
                shouldShowLine.set(true);
                parent.redraw();
            }

            @Override
            public void mouseExit(final MouseEvent e) {
                shouldShowLine.set(false);
                parent.redraw();
            }
        };
        label.addMouseTrackListener(controlsMouseListener);
        comboBox.addMouseTrackListener(controlsMouseListener);

        parent.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(final PaintEvent e) {
                if (!shouldShowLine.get()) {
                    return;
                }
                final Display display = parent.getDisplay();
                final Point labelSize = label.getSize();
                final Point labelRightEndCenter = new Point(labelSize.x, labelSize.y / 2);
                final Point fromPoint = display.map(label, parent, labelRightEndCenter);

                final Point toPoint = display.map(comboBox, parent, 0, 0);

                final Color oldForground = e.gc.getForeground();
                e.gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
                e.gc.drawLine(fromPoint.x + 5, fromPoint.y, toPoint.x - 5, fromPoint.y);
                e.gc.drawLine(toPoint.x - 8, fromPoint.y - 3, toPoint.x - 5, fromPoint.y);
                e.gc.drawLine(toPoint.x - 8, fromPoint.y + 3, toPoint.x - 5, fromPoint.y);
                e.gc.setForeground(oldForground);
            }
        });
    }

    @Override
    protected void doLoad() {
        updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
    }

    @Override
    protected void doLoadDefault() {
        updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
    }

    @Override
    protected void doStore() {
        if (value == null) {
            getPreferenceStore().setToDefault(getPreferenceName());
            return;
        }
        getPreferenceStore().setValue(getPreferenceName(), value);
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    private Combo getComboBoxControl(final Composite parent) {
        if (combo == null) {
            combo = new Combo(parent, SWT.READ_ONLY);
            for (int i = 0; i < entryNamesAndValues.length; i++) {
                combo.add(entryNamesAndValues[i][0], i);
            }
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent evt) {
                    final String oldValue = value;
                    final String name = combo.getText();
                    value = getValueForName(name);
                    setPresentsDefaultValue(false);
                    fireValueChanged(VALUE, oldValue, value);
                }
            });
        }
        combo.setEnabled(entryNamesAndValues.length > 1);
        return combo;
    }

    private String getValueForName(final String name) {
        for (int i = 0; i < entryNamesAndValues.length; i++) {
            final String[] entry = entryNamesAndValues[i];
            if (name.equals(entry[0])) {
                return entry[1];
            }
        }
        return entryNamesAndValues[0][0];
    }

    private void updateComboForValue(final String value) {
        this.value = value;
        for (int i = 0; i < entryNamesAndValues.length; i++) {
            if (value.equals(entryNamesAndValues[i][1])) {
                combo.setText(entryNamesAndValues[i][0]);
                return;
            }
        }
        if (entryNamesAndValues.length > 0) {
            this.value = entryNamesAndValues[0][1];
            combo.setText(entryNamesAndValues[0][0]);
        }
    }

    @Override
    public void setEnabled(final boolean enabled, final Composite parent) {
        super.setEnabled(enabled, parent);
        getComboBoxControl(parent).setEnabled(enabled);
    }
}