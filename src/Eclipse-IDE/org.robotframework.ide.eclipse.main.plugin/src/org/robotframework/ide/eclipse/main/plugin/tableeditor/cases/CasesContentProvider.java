package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class CasesContentProvider implements ITreeContentProvider {

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
            final boolean isEditable = section.getSuiteFile().isEditable();
            return extendWithAddingToken(inputElement, section.getChildren().toArray(), "test case", 0, isEditable);
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            final boolean isEditable = testCase.getSuiteFile().isEditable();
            return extendWithAddingToken(element, testCase.getChildren().toArray(), "", 1, isEditable);
        }
        return new Object[0];
    }

    private Object[] extendWithAddingToken(final Object parent, final Object[] elements, final String name,
            final int rank, final boolean isEditable) {
        final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
        newElements[elements.length] = new ElementAddingToken(parent, name, isEditable, rank);
        return newElements;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof RobotCase;
    }

}
