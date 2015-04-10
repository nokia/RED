package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.celleditor.ActivationCharPreservingTextCellEditor;

public class VariableValueEditingSupport extends VariableEditingSupport {

    public VariableValueEditingSupport(final ColumnViewer viewer) {
        super(viewer);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotVariable) {
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent);
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
            ((RobotVariable) element).setValue(val);

            getViewer().update(element, null);
        } else {
            super.setValue(element, value);
        }
    }

    @Override
    protected int getColumnIndex() {
        return 1;
    }
}
