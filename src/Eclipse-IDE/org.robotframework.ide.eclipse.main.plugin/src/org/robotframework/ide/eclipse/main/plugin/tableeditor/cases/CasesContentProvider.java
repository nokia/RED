package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class CasesContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        if (inputElement instanceof RobotCasesSection) {
            final RobotCasesSection section = (RobotCasesSection) inputElement;
            final Object[] elements = section.getChildren().toArray();
            final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
            newElements[elements.length] = new ElementAddingToken("test case", !section.isReadOnly());
            return newElements;
        }
        return new Object[0];
    }

}
