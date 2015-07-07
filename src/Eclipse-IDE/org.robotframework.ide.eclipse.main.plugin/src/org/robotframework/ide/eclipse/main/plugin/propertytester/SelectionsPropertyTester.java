package org.robotframework.ide.eclipse.main.plugin.propertytester;

import java.util.List;

import javax.inject.Named;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.propertytester.SelectionsPropertyTester.E4SelectionPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DIPropertyTester;
import org.robotframework.viewers.Selections;

public class SelectionsPropertyTester extends DIPropertyTester<E4SelectionPropertyTester> {

    public SelectionsPropertyTester() {
        super(E4SelectionPropertyTester.class);
    }

    public static class E4SelectionPropertyTester {

        @PropertyTest
        public Boolean testEditorProperties(@Named(DIPropertyTester.RECEIVER) final IStructuredSelection selection,
                @Named(DIPropertyTester.PROPERTY) final String propertyName,
                @Named(DIPropertyTester.EXPECTED_VALUE) final Boolean expected) {

            if ("allElementsHaveSameType".equals(propertyName)) {
                final List<Object> elements = Selections.getElements(selection, Object.class);
                if (elements.isEmpty()) {
                    return expected;
                }
                final Class<? extends Object> classOfFirst = elements.get(0).getClass();
                for (final Object element : elements) {
                    if (!classOfFirst.isInstance(element)) {
                        return !expected;
                    }
                }
                return expected;
            }
            return false;
        }
    }

}
