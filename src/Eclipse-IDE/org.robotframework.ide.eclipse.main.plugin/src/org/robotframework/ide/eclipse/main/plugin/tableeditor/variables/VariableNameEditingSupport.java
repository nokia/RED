package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.celleditor.ActivationCharPreservingTextCellEditor;

public class VariableNameEditingSupport extends VariableEditingSupport {

    public VariableNameEditingSupport(final ColumnViewer viewer) {
        super(viewer);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotVariable) {
            final String prefix = ((RobotVariable) element).getPrefix();
            final String suffix = ((RobotVariable) element).getSuffix();
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent, prefix,
                    suffix);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotVariable) {
            final RobotVariable variable = (RobotVariable) element;
            return variable.getPrefix() + variable.getName() + variable.getSuffix();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotVariable) {
            final String name = (String) value;

            // FIXME : should be done via command
            ((RobotVariable) element).setName(name.substring(2, name.length() - 1));

            getViewer().update(element, null);
        } else {
            super.setValue(element, value);
        }
    }

    @Override
    protected int getColumnIndex() {
        return 0;
    }
}
