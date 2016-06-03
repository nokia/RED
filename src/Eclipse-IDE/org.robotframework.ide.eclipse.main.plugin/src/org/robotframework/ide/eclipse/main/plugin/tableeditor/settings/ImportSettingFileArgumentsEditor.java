/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;

public class ImportSettingFileArgumentsEditor {

    private RowExposingTableViewer argumentsViewer;

    private List<String> arguments;

    public ImportSettingFileArgumentsEditor() {
    }

    public Composite createArgumentsEditor(final Composite parent, final List<String> currentArguments) {
        final Composite composite = new Composite(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(composite);

        argumentsViewer = new RowExposingTableViewer(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(argumentsViewer, RowTabbingStrategy.MOVE_TO_NEXT);

        arguments = newArrayList();
        if (currentArguments != null) {
            arguments.addAll(currentArguments);
        }

        createTable();
        argumentsViewer.setContentProvider(new ArgumentsContentProvider());
        argumentsViewer.setInput(arguments);

        return composite;
    }
    
    public List<String> getArguments() {
        final List<String> newArgs = newArrayList();
        for (final String arg : arguments) {
            if (!arg.equals("")) {
                newArgs.add(arg);
            }
        }
        return newArgs;
    }
    
    private void createTable() {
        ViewerColumnsFactory.newColumn("Argument")
                .withWidth(350)
                .labelsProvidedBy(new ArgumentsLabelProvider())
                .editingSupportedBy(new ArgumentsEditingSupport(argumentsViewer))
                .editingEnabledOnlyWhen(true)
                .createFor(argumentsViewer);
        final Table table = argumentsViewer.getTable();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
    }

    private class ArgumentsEditingSupport extends EditingSupport {

        private final CellEditor editor;

        public ArgumentsEditingSupport(final TableViewer viewer) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof ElementAddingToken) {
                return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
            }
            return editor;
        }

        @Override
        protected boolean canEdit(final Object element) {
            return true;
        }

        @Override
        protected Object getValue(final Object element) {
            return element.toString();
        }

        @Override
        protected void setValue(final Object element, final Object userInputValue) {
            if (element instanceof String) {
                final int ind = arguments.indexOf(element);
                if (ind >= 0) {
                    arguments.set(ind, userInputValue.toString());
                    getViewer().refresh();
                }
            } else if (element instanceof ElementAddingToken) {
                arguments.add("");
                getViewer().getControl().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        getViewer().refresh();
                        if (element != null) {
                            getViewer().editElement(arguments.get(arguments.size() - 1), 0);
                        }
                    }
                });
            }
        }
    }
    
    private class ArgumentsContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        }

        @Override
        public Object[] getElements(final Object inputElement) {

            final Object[] elements = ((List<?>) inputElement).toArray();
            final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
            newElements[elements.length] = new ElementAddingToken("argument", true);
            return newElements;
        }

    }
    
    private class ArgumentsLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ((ElementAddingToken) element).getImage();
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof String) {
                return new StyledString(element.toString());
            } else if (element instanceof ElementAddingToken) {
                return ((ElementAddingToken) element).getStyledText();
            }
            return new StyledString();
        }
    }
}
