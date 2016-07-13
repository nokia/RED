package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.nattable;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;

import ca.odell.glazedlists.TreeList;

class CasesElementsTreeFormat implements TreeList.Format<Object> {

    private ISortModel treeSortModel;

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
        // if (treeSortModel != null && depth == 0) {
        // Comparator<Object> comparator = new Comparator<Object>() {
        //
        // @Override
        // public int compare(Object o1, Object o2) {
        // if (o1 instanceof RobotKeywordDefinition && o2 instanceof RobotKeywordDefinition) {
        // RobotKeywordDefinition d1 = (RobotKeywordDefinition) o1;
        // RobotKeywordDefinition d2 = (RobotKeywordDefinition) o2;
        // return d1.getName().compareToIgnoreCase(d2.getName());
        // }
        // return 0;
        // }
        // };
        // return new SortableTreeComparator<Object>(comparator, treeSortModel);
        // }
        return null;
    }

    public void setTreeSortModel(final ISortModel treeSortModel) {
        this.treeSortModel = treeSortModel;
    }
}
