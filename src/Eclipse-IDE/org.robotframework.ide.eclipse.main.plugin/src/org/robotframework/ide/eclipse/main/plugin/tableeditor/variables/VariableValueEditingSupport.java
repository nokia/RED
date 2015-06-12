package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorSite;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetVariableValueCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class VariableValueEditingSupport extends RobotElementEditingSupport {

    private final IEditorSite site;
    
    VariableValueEditingSupport(final ColumnViewer viewer, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator creator, final IEditorSite site) {
        super(viewer, 1, commandsStack, creator);
        this.site = site;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotVariable) {
//            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
//                    DETAILS_EDITING_CONTEXT_ID);
            return new VariableDialogCellEditor(site, parent, element);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotVariable) {
            return ((RobotVariable) element).getValue();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotVariable) {
            final String val = (String) value;
            commandsStack.execute(new SetVariableValueCommand((RobotVariable) element, val));
        } else {
            super.setValue(element, value);
        }
    }
}
