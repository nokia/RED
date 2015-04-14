package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.mixStylers;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.withFontStyle;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.Stylers.DisposeNeededStyler;

public class VariableCommentLabelProvider extends VariableLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotVariable) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(mixStylers(
                    withForeground(150, 150, 150),
                    withFontStyle(SWT.ITALIC)));

            final RobotVariable variable = (RobotVariable) element;
            return new StyledString("# " + variable.getComment(), commentStyler);
        }
        return new StyledString();
    }
}
