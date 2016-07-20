/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.assist.KeywordsContentProposingSupport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class CodeNamesEditingSupport extends RobotElementEditingSupport {

    private KeywordsContentProposingSupport contentAssistSupport;

    CodeNamesEditingSupport(final RowExposingTreeViewer viewer, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator<RobotElement> creator) {
        super(viewer, 0, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RobotKeywordCall) {

            final Composite parent = (Composite) getViewer().getControl();
            final ActivationCharPreservingTextCellEditor editor = new ActivationCharPreservingTextCellEditor(
                    getViewer().getColumnViewerEditor(), parent, DETAILS_EDITING_CONTEXT_ID);
            if (contentAssistSupport == null) {
                final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
                contentAssistSupport = new KeywordsContentProposingSupport(keywordCall.getSuiteFile());
            }
            editor.addContentProposalsSupport(contentAssistSupport);
            final ControlDecoration decoration = new ControlDecoration(editor.getControl(), SWT.RIGHT | SWT.TOP);
            decoration.setDescriptionText("Press Ctrl+Space for content assist");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());
            editor.getControl().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    decoration.dispose();
                }
            });

            return editor;
        } else if (element instanceof RobotElement) {

            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getName();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotKeywordDefinition) {
            final String name = (String) value;
            commandsStack.execute(new SetKeywordDefinitionNameCommand((RobotKeywordDefinition) element, name));
        } else if (element instanceof RobotCase) {
            final String name = (String) value;
            commandsStack.execute(new SetCaseNameCommand((RobotCase) element, name));
        } else if (element instanceof RobotKeywordCall) {
            final String name = (String) value;
            commandsStack.execute(new SetKeywordCallNameCommand((RobotKeywordCall) element, name));
        } else {
            super.setValue(element, value);
        }
    }
}
