package org.robotframework.tmp;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class ContextLabelProvider extends ColumnLabelProvider {

    @Override
    public String getText(final Object element) {
        return element.toString();
    }
}
