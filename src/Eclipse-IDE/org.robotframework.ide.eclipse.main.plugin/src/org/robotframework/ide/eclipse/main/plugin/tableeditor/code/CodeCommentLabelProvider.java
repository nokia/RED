package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CommentsLabelProvider;

class CodeCommentLabelProvider extends CommentsLabelProvider {

    @Override
    protected String getComment(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getComment();
        }
        return "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotElement) {
            return super.getToolTipText(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotElement) {
            return super.getToolTipImage(element);
        }
        return null;
    }
}