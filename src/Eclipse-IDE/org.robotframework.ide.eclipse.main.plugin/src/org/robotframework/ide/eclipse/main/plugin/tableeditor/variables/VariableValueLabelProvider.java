package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

class VariableValueLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public String getText(final Object element) {
        return element instanceof RobotVariable ? ((RobotVariable) element).getValue() : "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(getText(element));
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
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}
