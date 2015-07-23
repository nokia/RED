package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;

public class VariablesSectionSorter extends ViewerSorter {

    public VariablesSectionSorter() {
        // nothing to do
    }

    public VariablesSectionSorter(final Collator collator) {
        super(collator);
    }

    @Override
    public int category(final Object element) {
        return ((RobotVariable) element).getType() == Type.LIST ? 0 : 1;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        final int cat1 = category(e1);
        final int cat2 = category(e2);

        if (cat1 != cat2) {
            return cat1 - cat2;
        }
        final RobotVariable var1 = (RobotVariable) e1;
        final RobotVariable var2 = (RobotVariable) e2;

        final int index1 = var1.getParent().getChildren().indexOf(var1);
        final int index2 = var2.getParent().getChildren().indexOf(var2);

        return index1 - index2;
    }
}
