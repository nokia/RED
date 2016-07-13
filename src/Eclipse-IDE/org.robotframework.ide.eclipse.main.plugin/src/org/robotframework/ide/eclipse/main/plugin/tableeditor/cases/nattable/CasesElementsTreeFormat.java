package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.nattable;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;

import ca.odell.glazedlists.TreeList;

class CasesElementsTreeFormat implements TreeList.Format<Object> {

    private static class CallsComparator implements Comparator<Object> {

        @Override
        public int compare(final Object o1, final Object o2) {
            if (o1 instanceof RobotKeywordCall && o2 instanceof RobotKeywordCall) {
                final RobotKeywordCall call1 = (RobotKeywordCall) o1;
                final RobotKeywordCall call2 = (RobotKeywordCall) o2;
                final int index1 = call1.getParent().getChildren().indexOf(call1);
                final int index2 = call2.getParent().getChildren().indexOf(call2);
                return index1 - index2;
            } else if (o1 instanceof RobotKeywordCall && o2 instanceof AddingToken) {
                return -1;
            } else if (o1 instanceof AddingToken && o2 instanceof RobotKeywordCall) {
                return 1;
            } else {
                return 0;
            }
        }

    }

    private static class CasesNaturalOrderComparator implements Comparator<Object> {

        @Override
        public int compare(final Object o1, final Object o2) {
            final RobotCase case1 = (RobotCase) o1;
            final RobotCase case2 = (RobotCase) o2;
            final int index1 = case1.getParent().getChildren().indexOf(case1);
            final int index2 = case2.getParent().getChildren().indexOf(case2);
            return index1 - index2;
        }

    }

    private static class CasesAlphabeticalComparator implements Comparator<Object> {

        @Override
        public int compare(final Object o1, final Object o2) {
            final RobotCase case1 = (RobotCase) o1;
            final RobotCase case2 = (RobotCase) o2;
            return case1.getName().compareToIgnoreCase(case2.getName());
        }

    }

    private ISortModel sortModel;

    @Override
    public void getPath(final List<Object> path, final Object element) {
        if (element instanceof RobotKeywordCall) {
            path.add(((RobotKeywordCall) element).getParent());
        } else if (element instanceof AddingToken) {
            path.add(((AddingToken) element).getParent());
        }
        path.add(element);
    }

    @Override
    public boolean allowsChildren(final Object element) {
        return true;
    }

    @Override
    public Comparator<? super Object> getComparator(final int depth) {
        // if (depth == 0) {
        // return sortModel == null ? null
        // : new SortableTreeComparator<>(new CasesAlphabeticalComparator(), this.sortModel);
        // }
        return null;

    }

    public void setSortModel(final ISortModel treeSortModel) {
        this.sortModel = treeSortModel;
    }
}
