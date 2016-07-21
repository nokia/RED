/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class CodeCommentEditingSupport extends RobotElementEditingSupport {

    CodeCommentEditingSupport(final ColumnViewer viewer, final int index,
            final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator<RobotElement> creator) {
        super(viewer, index, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RobotElement) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotFileInternalElement) {
            return ((RobotFileInternalElement) element).getComment();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotKeywordDefinition) {
            final String comment = (String) value;
            commandsStack.execute(new SetKeywordDefinitionCommentCommand((RobotKeywordDefinition) element, comment));
        } else if (element instanceof RobotCase) {
            final String comment = (String) value;
            commandsStack.execute(new SetCaseCommentCommand((RobotCase) element, comment));
        } else if (element instanceof RobotKeywordCall) {
            final String comment = (String) value;
            commandsStack.execute(new SetKeywordCallCommentCommand((RobotKeywordCall) element, comment));
        } else {
            super.setValue(element, value);
        }
    }
}
