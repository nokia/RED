package org.robotframework.viewers;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

import com.google.common.collect.Iterables;

public class Selections {

    public static final String SELECTION = "selection";

    public static <T> List<T> getElements(final IStructuredSelection selection, final Class<T> elementsClass) {
        return newArrayList(Iterables.filter(selection.toList(), elementsClass));
    }

    public static RobotVariable getSingleElement(final IStructuredSelection selection,
            final Class<RobotVariable> elementsClass) {
        final List<RobotVariable> elements = getElements(selection, elementsClass);
        if (elements.size() == 1) {
            return elements.get(0);
        }
        throw new IllegalArgumentException("Given selection should contain only one element of class "
                + elementsClass.getName() + ", but have " + elements.size() + " instead");
    }
}
