/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * @author Michal Anglart
 *
 */
class DetailEntriesCollection<D> {

    private static final int JUMP_STEP = 5;

    private final List<DetailWithEntry<D>> entries = new ArrayList<>();

    public List<DetailCellEditorEntry<D>> getEntries() {
        return entries.stream().map(detailWithEntry -> detailWithEntry.entry).collect(Collectors.toList());
    }

    boolean isEmpty() {
        return entries.isEmpty();
    }

    int size() {
        return entries.size();
    }

    void add(final DetailWithEntry<D> detailWithEntry) {
        entries.add(detailWithEntry);
    }

    private void selectEntries(final int from, final int howMany, final int direction) {
        Preconditions.checkArgument(direction == 1 || direction == -1);

        int selected = 0;
        int i = from + direction, j = 0;
        for (; 0 <= i && i < size() && j < howMany; i += direction, j++) {
            entries.get(i).entry.select(false);
            selected++;
        }
        if (i < 0 && selected < howMany) {
            entries.get(0).entry.select(false);
        }
        if (i >= size() && selected < howMany) {
            entries.get(size() - 1).entry.select(false);
        }
    }

    void selectOnlyEntry(final int index) {
        deselectAll();
        entries.get(index).entry.select(false);
    }

    void selectEntries(final List<Integer> indexes) {
        deselectAll();
        for (final int index : indexes) {
            entries.get(Math.max(0, Math.min(index, size() - 1))).entry.select(false);
        }
    }

    void selectOnlyPreviousEntry(final DetailCellEditorEntry<D> entry) {
        deselectAll();
        selectEntries(getEntryIndex(entry), 1, -1);
    }

    void selectPreviousEntry(final DetailCellEditorEntry<D> entry) {
        selectEntries(getEntryIndex(entry), 1, -1);
    }

    void selectOnlyPreviousEntryJumping(final DetailCellEditorEntry<D> entry) {
        deselectAll();
        selectEntries(getEntryIndex(entry) - JUMP_STEP, 1, -1);
    }

    void selectPreviousEntryJumping(final DetailCellEditorEntry<D> entry) {
        selectEntries(getEntryIndex(entry), JUMP_STEP, -1);
    }

    public void selectOnlyFirstEntry() {
        deselectAll();
        selectEntries(0, 1, -1);
    }

    public void selectTillFirstEntry(final DetailCellEditorEntry<D> entry) {
        selectEntries(getEntryIndex(entry), Integer.MAX_VALUE, -1);
    }

    void selectOnlyNextEntry(final DetailCellEditorEntry<D> entry) {
        deselectAll();
        selectEntries(getEntryIndex(entry), 1, 1);
    }

    void selectNextEntry(final DetailCellEditorEntry<D> entry) {
        selectEntries(getEntryIndex(entry), 1, 1);
    }

    void selectOnlyNextEntryJumping(final DetailCellEditorEntry<D> entry) {
        deselectAll();
        selectEntries(getEntryIndex(entry) + JUMP_STEP, 1, 1);
    }

    void selectNextEntriesJumping(final DetailCellEditorEntry<D> entry) {
        selectEntries(getEntryIndex(entry), JUMP_STEP, 1);
    }

    public void selectOnlyLastEntry() {
        deselectAll();
        selectEntries(size() - 1, 1, 1);
    }

    public void selectTillLastEntry(final DetailCellEditorEntry<D> entry) {
        selectEntries(getEntryIndex(entry), Integer.MAX_VALUE, 1);
    }

    void selectAll() {
        for (final DetailWithEntry<D> entry : entries) {
            if (!entry.entry.isSelected()) {
                entry.entry.select(false);
            }
        }
    }

    void deselectAll() {
        for (final DetailWithEntry<D> entry : entries) {
            if (entry.entry.isSelected()) {
                entry.entry.deselect();
            }
        }
    }

    void redrawEntries() {
        for (final DetailWithEntry<D> entry : entries) {
            entry.entry.redraw();
        }
    }

    boolean isFirst(final DetailCellEditorEntry<D> entry) {
        return !entries.isEmpty() && entries.get(0).entry == entry;
    }

    boolean isFirstSelected() {
        return !entries.isEmpty() && entries.get(0).entry.isSelected();
    }

    boolean isLast(final DetailCellEditorEntry<D> entry) {
        return !entries.isEmpty() && entries.get(entries.size() - 1).entry == entry;
    }

    public boolean isLastSelected() {
        return !entries.isEmpty() && entries.get(size() - 1).entry.isSelected();
    }

    int getEntryIndex(final DetailCellEditorEntry<D> entry) {
        int i = 0;
        for (final DetailWithEntry<D> detailEntry : entries) {
            if (detailEntry.entry == entry) {
                return i;
            }
            i++;
        }
        return -1;
    }

    List<D> getSelectedDetails() {
        final List<D> elements = newArrayList();
        for (final DetailWithEntry<D> entry : entries) {
            if (entry.entry.isSelected()) {
                elements.add(entry.detail);
            }
        }
        return elements;
    }

    List<Integer> getSelectedIndexes() {
        final List<Integer> indexes = newArrayList();
        int i = 0;
        for (final DetailWithEntry<D> entry : entries) {
            if (entry.entry.isSelected()) {
                indexes.add(i);
            }
            i++;
        }
        return indexes;
    }

    void disposeEntries() {
        for (final DetailWithEntry<D> entry : entries) {
            entry.entry.dispose();
        }
        entries.clear();
    }

    void openEntryForEdit(final DetailCellEditorEntry<D> entry) {
        for (final DetailWithEntry<D> detailEntry : entries) {
            detailEntry.entry.cancelEdit();
        }
        if (!entry.isEditorOpened()) {
            entry.openForEditing();
        }
    }

    static class DetailWithEntry<D> {

        private final D detail;

        private final DetailCellEditorEntry<D> entry;

        DetailWithEntry(final D detail, final DetailCellEditorEntry<D> entry) {
            this.detail = detail;
            this.entry = entry;
        }
    }
}
