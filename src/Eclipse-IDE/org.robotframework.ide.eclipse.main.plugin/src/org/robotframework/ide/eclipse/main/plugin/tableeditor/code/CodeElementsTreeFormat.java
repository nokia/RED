package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;

import ca.odell.glazedlists.TreeList;

public class CodeElementsTreeFormat implements TreeList.Format<Object> {

    private ISortModel sortModel;

    public void setSortModel(final ISortModel treeSortModel) {
        this.sortModel = treeSortModel;
    }

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
        if (sortModel == null || sortModel.getSortDirection(0) == SortDirectionEnum.NONE) {
            return null;
        } else if (sortModel.getSortDirection(0) == SortDirectionEnum.ASC) {
            return depth == 0 ? new CodeAlphabeticalComparator() : new CallsFileOrderComparator();
        } else if (sortModel.getSortDirection(0) == SortDirectionEnum.DESC) {
            return depth == 0 ? new ReverseComparator<>(new CodeAlphabeticalComparator())
                    : new CallsFileOrderComparator();
        }
        return null;
    }

    private static class CallsFileOrderComparator implements Comparator<Object> {

        private final CodeAlphabeticalComparator comparator = new CodeAlphabeticalComparator();

        @Override
        public int compare(final Object o1, final Object o2) {
            final IRobotCodeHoldingElement holder1 = getHolder(o1);
            final IRobotCodeHoldingElement holder2 = getHolder(o2);
            if (holder1 != holder2) {
                // final int index1 = holder1.getParent().getChildren().indexOf(holder1);
                // final int index2 = holder2.getParent().getChildren().indexOf(holder2);
                // return index1 < index2 ? -1 : 1;
                return comparator.compare(holder1, holder2);
            }

            if (o1 instanceof RobotKeywordCall && o2 instanceof RobotKeywordCall) {
                final RobotKeywordCall call1 = (RobotKeywordCall) o1;
                final RobotKeywordCall call2 = (RobotKeywordCall) o2;
                final int index1 = call1.getParent().getChildren().indexOf(call1);
                final int index2 = call2.getParent().getChildren().indexOf(call2);
                return o1 == o2 ? 0 : (index1 < index2 ? -1 : 1);
            } else if (o1 instanceof RobotKeywordCall && o2 instanceof AddingToken) {
                return -1;
            } else if (o1 instanceof AddingToken && o2 instanceof RobotKeywordCall) {
                return 1;
            } else {
                return 0;
            }
        }

        private IRobotCodeHoldingElement getHolder(final Object o) {
            if (o instanceof RobotKeywordCall) {
                final RobotKeywordCall call = (RobotKeywordCall) o;
                return call.getParent();
            } else if (o instanceof AddingToken) {
                final AddingToken token = (AddingToken) o;
                return (IRobotCodeHoldingElement) token.getParent();
            }
            throw new IllegalStateException("Unknown element " + o.toString());
        }

    }

    private static class CodeAlphabeticalComparator implements Comparator<Object> {

        @Override
        public int compare(final Object o1, final Object o2) {
            final RobotCodeHoldingElement<?> elem1 = (RobotCodeHoldingElement<?>) o1;
            final RobotCodeHoldingElement<?> elem2 = (RobotCodeHoldingElement<?>) o2;
            return elem1.getName().compareToIgnoreCase(elem2.getName());
        }
    }

    private static class ReverseComparator<T> implements Comparator<T> {

        private final Comparator<T> comparator;

        public ReverseComparator(final Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(final T o1, final T o2) {
            return comparator.compare(o2, o1);
        }
    }
}
