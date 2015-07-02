package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class UserKeywordsContentProvider implements ITreeContentProvider {

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
        if (inputElement instanceof RobotKeywordsSection) {
            final RobotKeywordsSection section = (RobotKeywordsSection) inputElement;
            final boolean isEditable = section.getSuiteFile().isEditable();
            return extendWithAddingToken(section.getChildren().toArray(), "keyword", 0, isEditable);
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition def = (RobotKeywordDefinition) element;
            final boolean isEditable = def.getSuiteFile().isEditable();
            return extendWithAddingToken(def.getChildren().toArray(), "", 1, isEditable);
        }
        return new Object[0];
    }

    private Object[] extendWithAddingToken(final Object[] elements, final String name, final int rank,
            final boolean isEditable) {
        final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
        newElements[elements.length] = new ElementAddingToken(name, isEditable, rank);
        return newElements;
    }

    @Override
    public Object getParent(final Object element) {
        return ((RobotElement) element).getParent();
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof RobotKeywordDefinition;
    }

}
