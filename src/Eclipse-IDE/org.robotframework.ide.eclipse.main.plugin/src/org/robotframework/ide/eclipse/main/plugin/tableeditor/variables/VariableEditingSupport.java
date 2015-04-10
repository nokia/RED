package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.celleditor.AddVariableCellEditor;
import org.robotframework.ide.eclipse.main.plugin.tempmodel.cmd.CmdAddVariable;

abstract class VariableEditingSupport extends EditingSupport {

    VariableEditingSupport(final ColumnViewer viewer) {
        super(viewer);
    }

    @Override
    protected boolean canEdit(final Object element) {
        final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
        return !section.isReadOnly();
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof AddVariableToken) {
            final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
            return new AddVariableCellEditor(section, (Composite) getViewer().getControl());
        }
        return null;
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof AddVariableToken) {
            addNewVariable((RobotVariable) value);
        }
    }

    protected final void addNewVariable(final RobotVariable newVariable) {
        final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
        new CmdAddVariable(section, newVariable).execute();
        scheduleViewerRefreshAndEditorActivation(newVariable, getColumnIndex());
    }

    // refresh and cell editor activation has to be done in GUI thread but after
    // current cell editor was properly deactivated
    private void scheduleViewerRefreshAndEditorActivation(final RobotVariable value, final int cellColumnToActivate) {
        getViewer().getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                getViewer().refresh();
                getViewer().editElement(value, cellColumnToActivate);
            }
        });
    }

    protected abstract int getColumnIndex();
}