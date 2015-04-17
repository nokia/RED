package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.celleditor.AlwaysDeactivatingCellEditor;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

abstract class VariableEditingSupport extends EditingSupport {

    protected final RobotEditorCommandsStack commandsStack;

    VariableEditingSupport(final ColumnViewer viewer, final RobotEditorCommandsStack commandsStack) {
        super(viewer);
        this.commandsStack = commandsStack;
    }

    @Override
    protected boolean canEdit(final Object element) {
        final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
        return !section.isReadOnly();
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof AddVariableToken) {
            return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
        }
        return null;
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof AddVariableToken) {
            addNewVariable();
        }
    }

    private void addNewVariable() {
        final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
        commandsStack.execute(new CreateFreshVariableCommand(section, true));

        final RobotVariable newVariable = (RobotVariable) section.getChildren().get(section.getChildren().size() - 1);
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