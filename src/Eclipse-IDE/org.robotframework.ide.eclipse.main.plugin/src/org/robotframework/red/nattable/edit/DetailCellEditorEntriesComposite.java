/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static com.google.common.collect.Lists.transform;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.robotframework.red.nattable.edit.DetailCellEditorEntriesControlsSwitcher.Mode;
import org.robotframework.red.nattable.edit.DetailEntriesCollection.DetailWithEntry;

/**
 * @author Michal Anglart
 *
 */
public class DetailCellEditorEntriesComposite<D> extends Composite {

    private final DetailCellEditorEditingSupport<D> editSupport;

    private final AssistanceSupport assistSupport;
    
    private int column;
    private int row;

    private final Runnable mainControlChooseCallback;

    private final Mode mode;

    private Composite entriesComposite;
    private final DetailEntriesCollection<D> entries = new DetailEntriesCollection<>();
    private final EntriesChangeListener<D> entriesChangesListener;

    private Color bgColor;
    private Color fgColor;

    public DetailCellEditorEntriesComposite(final Composite parent, final DetailCellEditorEditingSupport<D> editSupport,
            final AssistanceSupport assistSupport,
            final Mode mode, final EntriesChangeListener<D> entriesChangesListener,
            final Runnable mainControlChooseCallback) {
        super(parent, SWT.NONE);
        this.editSupport = editSupport;
        this.assistSupport = assistSupport;
        this.entriesChangesListener = entriesChangesListener;
        this.mainControlChooseCallback = mainControlChooseCallback;
        this.mode = mode;

        setBackground(getParent().getBackground());
        setForeground(getParent().getForeground());
        if (mode == Mode.INLINED) {
            addPaintListener(e -> {
                final Color oldForeground = e.gc.getForeground();

                if (fgColor != null) {
                    e.gc.setForeground(fgColor);
                }
                e.gc.drawLine(0, 0, e.width, 0);
                e.gc.drawLine(e.width - 1, 0, e.width - 1, e.height - 1);
                e.gc.drawLine(e.width - 1, e.height - 1, 0, e.height - 1);
                e.gc.drawLine(0, e.height - 1, 0, 0);

                e.gc.setForeground(oldForeground);
            });
            GridLayoutFactory.fillDefaults().spacing(1, 1).extendedMargins(1, 1, 1, 1).applyTo(this);
        } else {
            GridLayoutFactory.fillDefaults().spacing(1, 1).applyTo(this);
        }

        createEntriesComposite();
        createTooltipControls();
    }

    DetailEntriesCollection<D> getEntries() {
        return entries;
    }

    Color getBgColor() {
        return bgColor;
    }

    Color getFgColor() {
        return fgColor;
    }

    private void createEntriesComposite() {
        final ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
        scrolledComposite.setBackground(getParent().getBackground());
        scrolledComposite.setShowFocusedControl(true);
        scrolledComposite.setExpandHorizontal(true);
        final SelectionListener scrollingRefresher = SelectionListener
                .widgetSelectedAdapter(e -> entries.redrawEntries());
        scrolledComposite.getVerticalBar().addSelectionListener(scrollingRefresher);
        scrolledComposite.getVerticalBar().addDisposeListener(
                e -> scrolledComposite.getVerticalBar().removeSelectionListener(scrollingRefresher));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);

        entriesComposite = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(entriesComposite);
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(entriesComposite);
        entriesComposite.setSize(entriesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void createDetailEntryControls(final List<D> details) {
        for (final D detail : details) {
            final DetailCellEditorEntry<D> entry = editSupport.createDetailEntry(entriesComposite, column, row, detail,
                    assistSupport);
            GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 25).grab(true, false).applyTo(entry);
            entry.setBackground(bgColor);
            entry.setForeground(fgColor);
            entry.addKeyListener(new EntryKeyPressListener(entry));
            entry.addMouseListener(MouseListener.mouseUpAdapter(e -> {
                if (entry.isSelected() && mode == Mode.WINDOWED) {
                    entries.openEntryForEdit(entry);
                } else {
                    entry.select(e.stateMask == 0 || e.stateMask != SWT.CTRL);
                }
            }));
            entry.setEditorListener(value -> {
                editSupport.setNewValue(detail, value);
                entry.update(detail);
            });

            entries.add(new DetailWithEntry<>(detail, entry));
        }
        entriesComposite.setSize(entriesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        entriesChangesListener.entriesChanged(entries.getEntries());
    }

    private void createTooltipControls() {
        final Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, false).applyTo(sep);

        final Label label = new Label(this, SWT.NONE);
        label.setBackground(getBackground());
        label.setText("Edit details");
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(true, false).applyTo(label);
    }

    void setColors(final Color background, final Color foreground) {
        this.bgColor = background;
        this.fgColor = foreground;

        setBackground(background);
        entriesComposite.setBackground(fgColor);
        entriesComposite.getParent().setBackground(bgColor);

        setForeground(foreground);
    }

    void setInput(final int column, final int row) {
        setColumn(column);
        setRow(row);
        entries.disposeEntries();
        createDetailEntryControls(editSupport.getInput(column, row));
    }

    int getColumn() {
        return column;
    }

    void setColumn(final int column) {
        this.column = column;
    }

    int getRow() {
        return row;
    }

    void setRow(final int row) {
        this.row = row;
    }

    void refresh() {
        entries.disposeEntries();
        createDetailEntryControls(editSupport.getDetailElements());
    }

    void selectFirstEntry() {
        if (!entries.isEmpty()) {
            entries.selectOnlyEntry(0);
        }
    }

    @FunctionalInterface
    static interface EntriesChangeListener<D> {

        void entriesChanged(final List<DetailCellEditorEntry<D>> entries);
    }

    private class EntryKeyPressListener extends KeyAdapter {

        private final DetailCellEditorEntry<D> entry;

        private EntryKeyPressListener(final DetailCellEditorEntry<D> entry) {
            this.entry = entry;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            if (e.keyCode == SWT.HOME && e.stateMask == 0) {
                entries.selectOnlyFirstEntry();

            } else if (e.keyCode == SWT.HOME && e.stateMask == SWT.SHIFT) {
                entries.selectTillFirstEntry(entry);

            } else if (e.keyCode == SWT.ARROW_UP && e.stateMask == 0) {
                if (!entries.isFirst(entry)) {
                    entries.selectOnlyPreviousEntry(entry);
                } else {
                    entry.deselect();
                    mainControlChooseCallback.run();
                }
            } else if (e.keyCode == SWT.ARROW_UP && e.stateMask == SWT.SHIFT) {
                entries.selectPreviousEntry(entry);

            } else if (e.keyCode == SWT.ARROW_UP && e.stateMask == SWT.CTRL) {
                if (!entries.isFirstSelected()) {
                    final List<Integer> indexes = entries.getSelectedIndexes();

                    editSupport.moveLeft(entries.getSelectedDetails());
                    refresh();

                    entries.selectEntries(transform(indexes, n -> n - 1));
                }
            } else if (e.keyCode == SWT.END && e.stateMask == 0) {
                entries.selectOnlyLastEntry();

            } else if (e.keyCode == SWT.END && e.stateMask == SWT.SHIFT) {
                entries.selectTillLastEntry(entry);

            } else if (e.keyCode == SWT.PAGE_UP && e.stateMask == 0) {
                entries.selectOnlyPreviousEntryJumping(entry);

            } else if (e.keyCode == SWT.PAGE_UP && e.stateMask == SWT.SHIFT) {
                entries.selectPreviousEntryJumping(entry);

            } else if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == 0) {
                entries.selectOnlyNextEntry(entry);

            } else if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == SWT.SHIFT) {
                entries.selectNextEntry(entry);

            } else if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == SWT.CTRL) {
                if (!entries.isLastSelected()) {
                    final List<Integer> indexes = entries.getSelectedIndexes();

                    editSupport.moveRight(entries.getSelectedDetails());
                    refresh();

                    entries.selectEntries(transform(indexes, n -> n + 1));
                }
            } else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == 0) {
                entries.selectOnlyNextEntryJumping(entry);

            } else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == SWT.SHIFT) {
                entries.selectNextEntriesJumping(entry);

            } else if (e.keyCode == SWT.ESC) {
                entries.deselectAll();
                mainControlChooseCallback.run();

            } else if (e.keyCode == SWT.DEL) {
                final int index = entries.getEntryIndex(entry);
                editSupport.removeDetailElements(entries.getSelectedDetails());
                refresh();
                if (index < entries.size()) {
                    entries.selectOnlyEntry(index);
                } else if (!entries.isEmpty()) {
                    entries.selectOnlyLastEntry();
                }
            } else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                entries.openEntryForEdit(entry);

            } else if (e.keyCode == 'a' && e.stateMask == SWT.CTRL) {
                entries.selectAll();
            }
        }
    }
}
