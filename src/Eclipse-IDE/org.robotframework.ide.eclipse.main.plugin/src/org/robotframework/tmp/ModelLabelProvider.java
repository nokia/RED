package org.robotframework.tmp;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;

public class ModelLabelProvider extends ColumnLabelProvider {

    @Override
    public String getText(final Object element) {
        return ((RobotElement) element).getName();
    }
}
