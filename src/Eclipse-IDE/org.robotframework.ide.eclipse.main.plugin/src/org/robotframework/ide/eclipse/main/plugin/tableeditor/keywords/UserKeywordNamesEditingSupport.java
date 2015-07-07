package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

public class UserKeywordNamesEditingSupport extends RobotElementEditingSupport {

    public UserKeywordNamesEditingSupport(final RowExposingTreeViewer viewer,
            final RobotEditorCommandsStack commandsStack, final NewElementsCreator creator) {
        super(viewer, 0, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RobotKeywordDefinition || element instanceof RobotKeywordCall) {
            final Composite parent = (Composite) getViewer().getControl();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            return ((RobotKeywordDefinition) element).getComment();
        } else if (element instanceof RobotKeywordCall) {
            return ((RobotKeywordCall) element).getComment();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotKeywordDefinition) {
            final String comment = (String) value;
            commandsStack.execute(new SetKeywordDefinitionNameCommand((RobotKeywordDefinition) element, comment));
        } else if (element instanceof RobotKeywordCall) {
            final String comment = (String) value;
            commandsStack.execute(new SetKeywordCallNameCommand((RobotKeywordCall) element, comment));
        } else {
            super.setValue(element, value);
        }
    }
}
