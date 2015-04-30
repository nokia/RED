package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

class VariableNameLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotVariable) {
            final DisposeNeededStyler variableStyler = addDisposeNeededStyler(withForeground(200, 200, 200));

            final RobotVariable variable = (RobotVariable) element;
            final StyledString label = new StyledString();
            label.append(variable.getPrefix(), variableStyler);
            label.append(variable.getName());
            label.append(variable.getSuffix(), variableStyler);
            return label;
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotVariable) {
            final RobotVariable variable = (RobotVariable) element;
            return variable.getPrefix() + variable.getName() + variable.getSuffix();
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
