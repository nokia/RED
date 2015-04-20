package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.celleditor.ActivationCharPreservingTextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class VariableCommentEditingSupport extends VariableEditingSupport {

    public VariableCommentEditingSupport(final ColumnViewer viewer, final RobotEditorCommandsStack commandsStack) {
        super(viewer, commandsStack);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotVariable) {
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent, CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotVariable) {
            return ((RobotVariable) element).getComment();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotVariable) {
            final String comment = (String) value;
            commandsStack.execute(new SetVariableCommentCommand((RobotVariable) element, comment));
        } else {
            super.setValue(element, value);
        }
    }

    @Override
    protected int getColumnIndex() {
        return 2;
    }
}
