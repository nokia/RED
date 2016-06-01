/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.red.nattable.edit.DetailCellEditorDialog.DialogContentCreator;
import org.robotframework.red.nattable.edit.DetailCellEditorEntriesComposite.EntriesChangeListener;
import org.robotframework.red.nattable.edit.DetailCellEditorEntriesComposite.MainControlChooser;

/**
 * @author Michal Anglart
 *
 */
class DetailCellEditorEntriesControlsSwitcher<D> {

    private final Composite parent;

    private final DetailCellEditorEditingSupport<D> editSupport;

    private final MainControlChooser mainControlChooseCallback;

    private Mode mode = Mode.INLINED;

    private DetailCellEditorDialog dialog;

    private DetailCellEditorEntriesComposite<D> panel;

    DetailCellEditorEntriesControlsSwitcher(final DetailCellEditorComposite<D> parent,
            final DetailCellEditorEditingSupport<D> editSupport, final MainControlChooser mainControlChooseCallback) {
        this.parent = parent;
        this.editSupport = editSupport;
        this.mainControlChooseCallback = mainControlChooseCallback;
    }

    DetailCellEditorEntriesComposite<D> createEntriesPanel() {
        panel = new DetailCellEditorEntriesComposite<>(parent, editSupport,
                mode, new DetailCellEditorEntriesComposite.EntriesChangeListener<D>() {
                    @Override
                    public void entriesChanged(final List<DetailCellEditorEntry<D>> entries) {
                        for (final Control entry : entries) {
                            installControlListeners(entry);
                        }
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
        child.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(final MouseEvent e) {
                switchToWindowingMode();
            }
        });
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
        panel.dispose();

        parent.setSize(panelSize.x, 21);

        final DialogContentCreator contentCreator = new DialogContentCreator() {
            @Override
            public Control create(final Composite parent) {
                panel = new DetailCellEditorEntriesComposite<>(parent, editSupport, Mode.WINDOWED,
                        new EntriesChangeListener<D>(), mainControlChooseCallback);
                panel.addDisposeListener(new DisposeListener() {
                    @Override
                    public void widgetDisposed(final DisposeEvent e) {
                        switchToInlineMode();
                    }
                });
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

        dialog.close();
        dialog = null;

        panel = createEntriesPanel();
        panel.refresh();

        final Point realSize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        parent.setSize(parent.getSize().x, Math.min(200, realSize.y));
    }

    void selectFirstEntry() {
        if (mode == Mode.INLINED) {
            switchToWindowingMode();
        }
        panel.selectFirstEntry();
    }

    void refreshEntriesPanel() {
        panel.refresh();
    }

    void setPanelInput(final int column, final int row) {
        panel.setInput(column, row);
    }

    enum Mode {
        INLINED,
        WINDOWED
    }
}
