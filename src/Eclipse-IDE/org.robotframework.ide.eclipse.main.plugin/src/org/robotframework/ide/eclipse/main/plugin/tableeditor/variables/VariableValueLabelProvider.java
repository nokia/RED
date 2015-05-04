package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

class VariableValueLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final String text = element instanceof RobotVariable ? ((RobotVariable) element).getValue() : "";
        return new StyledString(text);
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotVariable) {
            final String tooltipText = getText(element);
            return tooltipText.isEmpty() ? "<empty>" : tooltipText;
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
