package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

public class CasesNameEditingSupport extends RobotElementEditingSupport {

    public CasesNameEditingSupport(final RowExposingTableViewer viewer, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator creator) {
        super(viewer, 0, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotCase) {
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            return testCase.getName();
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotCase) {
            final String name = (String) value;
            commandsStack.execute(new SetCaseNameCommand((RobotCase) element, name));
        } else {
            super.setValue(element, value);
        }
    }

}
