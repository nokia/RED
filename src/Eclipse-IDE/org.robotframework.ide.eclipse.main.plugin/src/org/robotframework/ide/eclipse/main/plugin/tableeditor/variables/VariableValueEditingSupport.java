package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetVariableValueCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class VariableValueEditingSupport extends RobotElementEditingSupport {

    VariableValueEditingSupport(final ColumnViewer viewer, final RobotEditorCommandsStack commandsStack) {
        super(viewer, 1, commandsStack);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotVariable) {
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
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

    @Override
    protected RobotElement createNewElement() {
        final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
        commandsStack.execute(new CreateFreshVariableCommand(section, true));

        return section.getChildren().get(section.getChildren().size() - 1);
    }
}
