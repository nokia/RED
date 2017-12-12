/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.robotframework.red.swt.Listeners.mouseDownAdapter;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.red.nattable.edit.DetailCellEditorDialog.DialogContentCreator;

/**
 * @author Michal Anglart
 *
 */
class DetailCellEditorEntriesControlsSwitcher<D> {

    private final Composite parent;

    private final DetailCellEditorEditingSupport<D> editSupport;

    private final AssistanceSupport assistSupport;

    private final Runnable mainControlChooseCallback;

    private Mode mode = Mode.INLINED;

    private DetailCellEditorDialog dialog;

    private DetailCellEditorEntriesComposite<D> panel;


    DetailCellEditorEntriesControlsSwitcher(final DetailCellEditorComposite<D> parent,
            final DetailCellEditorEditingSupport<D> editSupport, final AssistanceSupport assistSupport,
            final Runnable mainControlChooseCallback) {
        this.parent = parent;
        this.editSupport = editSupport;
        this.assistSupport = assistSupport;
        this.mainControlChooseCallback = mainControlChooseCallback;
    }

    DetailCellEditorEntriesComposite<D> createEntriesPanel() {
        panel = new DetailCellEditorEntriesComposite<>(parent, editSupport, assistSupport, mode,
                entries -> {
                    for (final Control entry : entries) {
                        installControlListeners(entry);
                    }
                }, mainControlChooseCallback);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        installListeners(panel);
        return panel;
    }

    private void installListeners(final Composite composite) {
        for (final Control child : composite.getChildren()) {
            installControlListeners(child);

            if (child instanceof Composite) {
                installListeners((Composite) child);
            }
        }
    }

    private void installControlListeners(final Control child) {
        child.addMouseListener(mouseDownAdapter(e -> switchToWindowingMode()));
    }

    private void switchToWindowingMode() {
        if (mode == Mode.WINDOWED || parent.isDisposed()) {
            return;
        }
        mode = Mode.WINDOWED;

        final Rectangle panelBounds = panel.getBounds();
        final Point absoluteLocation = parent.getDisplay().map(panel.getParent(), null,
                new Point(panelBounds.x, panelBounds.y));

        final List<Integer> selection = panel.getEntries().getSelectedIndexes();

        final Point panelSize = panel.getSize();
        final Color bgColor = panel.getBgColor();
        final Color fgColor = panel.getFgColor();
        panel.dispose();

        parent.setSize(panelSize.x, 21);

        final DialogContentCreator contentCreator = new DialogContentCreator() {
            @Override
            public Control create(final Composite parent) {
                final int column = panel.getColumn();
                final int row = panel.getRow();
                panel = new DetailCellEditorEntriesComposite<>(parent, editSupport, assistSupport, Mode.WINDOWED,
                        entries -> { }, mainControlChooseCallback);
                panel.setColumn(column);
                panel.setRow(row);
                panel.setColors(bgColor, fgColor);
                panel.addDisposeListener(e -> switchToInlineMode());
                return panel;
            }
        };
        final Rectangle dialogInitialBounds = new Rectangle(absoluteLocation.x - 7, absoluteLocation.y,
                panelSize.x + 14, 200);
        dialog = new DetailCellEditorDialog(parent.getShell(), dialogInitialBounds, contentCreator);
        dialog.open();

        panel.refresh();
        panel.getEntries().selectEntries(selection);
    }

    private void switchToInlineMode() {
        if (mode == Mode.INLINED || parent.isDisposed()) {
            return;
        }
        mode = Mode.INLINED;

        final Color bgColor = panel.getBgColor();
        final Color fgColor = panel.getFgColor();

        dialog.close();
        dialog = null;

        panel = createEntriesPanel();
        panel.setColors(bgColor, fgColor);
        panel.refresh();

        final Point realSize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        parent.setSize(parent.getSize().x, Math.min(200, realSize.y));
    }

    void selectFirstEntry() {
        if (mode == Mode.INLINED && !panel.getEntries().isEmpty()) {
            switchToWindowingMode();
        }
        panel.selectFirstEntry();
    }

    void refreshEntriesPanel() {
        if (mode == Mode.WINDOWED) {
            panel.refresh();
        } else {
            panel.refresh();

            final Point realSize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            parent.setSize(parent.getSize().x, Math.min(200, realSize.y));

            final List<DetailCellEditorEntry<D>> entries = panel.getEntries().getEntries();
            final DetailCellEditorEntry<D> lastEntry = entries.get(entries.size() - 1);
            getScrolledAscendant(lastEntry).showControl(lastEntry);
        }
    }

    private ScrolledComposite getScrolledAscendant(final Control control) {
        Composite parent = control.getParent();
        while (parent != null) {
            if (parent instanceof ScrolledComposite) {
                return (ScrolledComposite) parent;
            }

            parent = parent.getParent();
        }
        return null;
    }

    void setPanelInput(final int column, final int row) {
        panel.setInput(column, row);
    }

    void setColors(final Color background, final Color foreground) {
        panel.setColors(background, foreground);
    }

    enum Mode {
        INLINED,
        WINDOWED
    }
}
