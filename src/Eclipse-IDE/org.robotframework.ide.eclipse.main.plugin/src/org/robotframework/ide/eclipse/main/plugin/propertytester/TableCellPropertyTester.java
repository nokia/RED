package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ViewerCell;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

import com.google.common.base.Preconditions;

public class TableCellPropertyTester extends PropertyTester {

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RobotFormEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RobotFormEditor.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((RobotFormEditor) receiver, property, ((Boolean) expectedValue).booleanValue());
        } else if (expectedValue instanceof Integer) {
            return testProperty((RobotFormEditor) receiver, property, ((Integer) expectedValue).intValue());
        }
        return false;
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final boolean expected) {
        final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
        if ("thereIsAFocusedCell".equals(property)) {
            return viewerAccessor.getFocusedCell() != null == expected;
        } else if ("focusedCellHasContent".equals(property)) {
            final ViewerCell cell = viewerAccessor.getFocusedCell();
            return (cell != null && !cell.getText().isEmpty()) == expected;
        }
        return false;
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final int expected) {
        final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
        if ("focusedCellHasIndex".equals(property)) {
            final ViewerCell focusedCell = viewerAccessor.getFocusedCell();
            return focusedCell != null && focusedCell.getColumnIndex() == expected;
        }
        return false;
    }
}
