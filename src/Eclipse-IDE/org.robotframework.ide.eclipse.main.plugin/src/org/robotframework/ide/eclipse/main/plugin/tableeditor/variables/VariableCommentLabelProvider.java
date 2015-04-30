package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CommentsLabelProvider;

class VariableCommentLabelProvider extends CommentsLabelProvider {

    @Override
    protected String getComment(final Object element) {
        return element instanceof RobotVariable ? ((RobotVariable) element).getComment() : "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotVariable) {
            return super.getToolTipText(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotVariable) {
            return super.getToolTipImage(element);
        }
        return null;
    }
}