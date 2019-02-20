/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;


/**
 * @author Michal Anglart
 *
 */
class PathsEditingSupport extends ElementsAddingEditingSupport {

    private final Consumer<SearchPath> successHandler;

    PathsEditingSupport(final ColumnViewer viewer, final Supplier<SearchPath> elementsCreator,
            final Consumer<SearchPath> successHandler) {
        super(viewer, 0, elementsCreator);
        this.successHandler = successHandler;
    }

    @Override
    protected int getColumnShift() {
        return 1;
    }

    @Override
    protected boolean canEdit(final Object element) {
        return !(element instanceof SearchPath && ((SearchPath) element).isSystem());
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof SearchPath) {
            return new TextCellEditor((Composite) getViewer().getControl());
        } else {
            return super.getCellEditor(element);
        }
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof SearchPath) {
            return ((SearchPath) element).getLocation();
        } else {
            return null;
        }
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof SearchPath) {
            final SearchPath path = (SearchPath) element;
            final String oldValue = (String) getValue(element);
            final String newValue = (String) value;

            if (!newValue.equals(oldValue)) {
                path.setLocation(newValue);
                successHandler.accept(path);
            }
        } else {
            super.setValue(element, value);
        }
    }
}
