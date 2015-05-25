package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class TestCasesNameLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotCase) {
            return new StyledString(((RobotCase) element).getName());
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return null;
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }
}
