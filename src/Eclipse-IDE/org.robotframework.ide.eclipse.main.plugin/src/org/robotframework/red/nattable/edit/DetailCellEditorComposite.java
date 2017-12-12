/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.robotframework.red.swt.Listeners.keyPressedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @author Michal Anglart
 *
 */
class DetailCellEditorComposite<D> extends Composite {

    private final DetailCellEditorEditingSupport<D> editSupport;

    private final Text text;

    private final DetailCellEditorEntriesControlsSwitcher<D> switcher;

    private final CellEditorValueValidationJobScheduler<String> validationScheduler;

    private final AssistanceSupport assistSupport;

    DetailCellEditorComposite(final Composite parent, final DetailCellEditorEditingSupport<D> editSupport,
            final AssistanceSupport assistSupport,
            final CellEditorValueValidationJobScheduler<String> validationScheduler) {
        super(parent, SWT.NONE);
        this.editSupport = editSupport;
        this.assistSupport = assistSupport;
        this.validationScheduler = validationScheduler;

        setBackground(parent.getBackground());
        setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(this);

        this.text = createText();
        this.switcher = new DetailCellEditorEntriesControlsSwitcher<>(this, editSupport, assistSupport,
                () -> text.setFocus());
        this.switcher.createEntriesPanel();
    }

    private Text createText() {
        final Text text = new Text(this, SWT.SINGLE);
        text.addKeyListener(keyPressedAdapter(e -> {
            if (assistSupport.areContentProposalsShown()) {
                return;
            }
            if (e.keyCode == SWT.ARROW_DOWN) {

                switcher.selectFirstEntry();
            } else if ((e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) && e.stateMask == 0
                    && !text.getText().isEmpty() && validationScheduler.canCloseCellEditor()) {
                editSupport.addNewDetailElement(text.getText());

                text.setText("");
                switcher.refreshEntriesPanel();
            }
        }));
        text.addPaintListener(e -> {
            if (text.getText().isEmpty() && !text.isFocusControl()) {
                final Color current = e.gc.getForeground();
                e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
                e.gc.drawString("new entry", 3, 1);
                e.gc.setForeground(current);
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
        return text;
    }

    Text getText() {
        return text;
    }

    void setInput(final int column, final int row) {
        switcher.setPanelInput(column, row);
    }

    void setColors(final Color background, final Color foreground) {
        text.setBackground(background);
        text.setForeground(foreground);

        switcher.setColors(background, foreground);
    }
}