package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CommentsLabelProvider;

class UserKeywordCommentLabelProvider extends CommentsLabelProvider {

    @Override
    protected String getComment(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            return ((RobotKeywordDefinition) element).getComment();
        } else if (element instanceof RobotKeywordCall) {
            return ((RobotKeywordCall) element).getComment();
        }
        return "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotKeywordDefinition || element instanceof RobotKeywordCall) {
            return super.getToolTipText(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotKeywordDefinition || element instanceof RobotKeywordCall) {
            return super.getToolTipImage(element);
        }
        return null;
    }
}