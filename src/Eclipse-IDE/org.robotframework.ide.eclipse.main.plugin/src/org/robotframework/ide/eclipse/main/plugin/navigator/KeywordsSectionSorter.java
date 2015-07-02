package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;

public class KeywordsSectionSorter extends ViewerSorter {

    public KeywordsSectionSorter() {
        // nothing to do
    }

    public KeywordsSectionSorter(final Collator collator) {
        super(collator);
    }

    @Override
    public int category(final Object element) {
        return 0;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        final int cat1 = category(e1);
        final int cat2 = category(e2);

        if (cat1 != cat2) {
            return cat1 - cat2;
        }
        final RobotKeywordDefinition def1 = (RobotKeywordDefinition) e1;
        final RobotKeywordDefinition def2 = (RobotKeywordDefinition) e2;

        final int index1 = def1.getParent().getChildren().indexOf(def1);
        final int index2 = def2.getParent().getChildren().indexOf(def2);

        return index1 - index2;
    }
}
