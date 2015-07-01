package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CommentsLabelProvider;

class UserKeywordCommentLabelProvider extends CommentsLabelProvider {

    @Override
    protected String getComment(final Object element) {
        return element instanceof RobotKeywordDefinition ? ((RobotKeywordDefinition) element).getComment() : "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            return super.getToolTipText(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            return super.getToolTipImage(element);
        }
        return null;
    }
}