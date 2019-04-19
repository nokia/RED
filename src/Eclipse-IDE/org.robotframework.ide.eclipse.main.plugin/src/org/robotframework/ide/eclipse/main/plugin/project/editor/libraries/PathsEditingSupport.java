/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.red.jface.viewers.ActivationCharPreservingTextCellEditor;
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
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    RedPlugin.DETAILS_EDITING_CONTEXT_ID);
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

    static class SearchPathCreator implements Supplier<SearchPath> {

        private final Shell shell;

        private final Function<SearchPath, Boolean> elementAdder;

        private final Consumer<List<SearchPath>> successHandler;

        public SearchPathCreator(final Shell shell, final Function<SearchPath, Boolean> elementAdder,
                final Consumer<List<SearchPath>> successHandler) {
            this.shell = shell;
            this.elementAdder = elementAdder;
            this.successHandler = successHandler;
        }

        @Override
        public SearchPath get() {
            final PathEntryDialog dialog = new PathEntryDialog(shell);
            if (dialog.open() == Window.OK) {
                final List<SearchPath> paths = dialog.getSearchPath();
                if (paths.isEmpty()) {
                    return null;
                }
                boolean added = false;
                for (final SearchPath path : paths) {
                    added |= elementAdder.apply(path);
                }
                if (added) {
                    successHandler.accept(paths);
                }
                return paths.get(paths.size() - 1);
            }
            return null;
        }
    }
}
