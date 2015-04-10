package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.StyledString;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

public class VariableValueLabelProvider extends VariableLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotVariable) {
            return new StyledString(((RobotVariable) element).getValue());
        }
        return new StyledString();
    }
}
