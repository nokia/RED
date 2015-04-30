package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;

public abstract class RobotElementEditingSupport extends EditingSupport {

    // Id of context which should be activated when cell editor is activated
    protected static final String DETAILS_EDITING_CONTEXT_ID = "org.robotframework.ide.eclipse.details.context";

    protected final RobotEditorCommandsStack commandsStack;

    private final int indexToActivate;

    public RobotElementEditingSupport(final ColumnViewer viewer, final int index,
            final RobotEditorCommandsStack commandsStack) {
        super(viewer);
        this.commandsStack = commandsStack;
        this.indexToActivate = index;
    }

    @Override
    protected boolean canEdit(final Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof ElementAddingToken) {
            return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
        }
        return null;
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof ElementAddingToken) {
            final RobotElement newElement = createNewElement();
            scheduleViewerRefreshAndEditorActivation(newElement, indexToActivate);
        }
    }

    protected abstract RobotElement createNewElement();

    // refresh and cell editor activation has to be done in GUI thread but after
    // current cell editor was properly deactivated
    private void scheduleViewerRefreshAndEditorActivation(final RobotElement value, final int cellColumnToActivate) {
        getViewer().getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                getViewer().refresh();
                getViewer().editElement(value, cellColumnToActivate);
            }
        });
    }
}