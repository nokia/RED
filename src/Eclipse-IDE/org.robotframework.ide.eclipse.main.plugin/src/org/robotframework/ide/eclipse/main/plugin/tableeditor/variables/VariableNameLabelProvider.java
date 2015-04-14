package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.mixStylers;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.withFontStyle;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.DisposeNeededStyler;

public class VariableNameLabelProvider extends VariableLabelProvider {

    @Override
    public Image getImage(final Object element) {
        if (element instanceof AddVariableToken) {
            return RobotImages.getAddImage().createImage();
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
        } else if (element instanceof AddVariableToken) {
            final DisposeNeededStyler variableAdderStyler = addDisposeNeededStyler(mixStylers(
                    withFontStyle(SWT.ITALIC), withForeground(30, 127, 60)));

            return new StyledString("...add new variable", variableAdderStyler);
        }
        return new StyledString();
    }
}
