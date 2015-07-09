package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class CodeNamesEditingSupport extends RobotElementEditingSupport {

    CodeNamesEditingSupport(final RowExposingTreeViewer viewer,
            final RobotEditorCommandsStack commandsStack, final NewElementsCreator creator) {
        super(viewer, 0, commandsStack, creator);
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
