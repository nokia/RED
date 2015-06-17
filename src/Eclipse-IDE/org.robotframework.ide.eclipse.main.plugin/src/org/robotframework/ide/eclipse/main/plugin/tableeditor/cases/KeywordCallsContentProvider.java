package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCaseSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class KeywordCallsContentProvider implements IStructuredContentProvider {

    private final boolean isEditable;

    public KeywordCallsContentProvider(final boolean editable) {
        this.isEditable = editable;
    }

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
        if (inputElement instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) inputElement;
            final Iterable<RobotElement> filteredCalls = Iterables.filter(testCase.getChildren(),
                    new Predicate<RobotElement>() {
                        @Override
                        public boolean apply(final RobotElement element) {
                            return !(element instanceof RobotCaseSetting);
                        }
                    });
            final RobotElement[] elements = Iterables.toArray(filteredCalls, RobotElement.class);
            final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
            newElements[elements.length] = new ElementAddingToken("", isEditable);
            return newElements;
        }
        return new Object[0];
    }

}
