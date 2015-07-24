package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

class VariablesViewerComparators {

    static ViewerComparator variableNamesAscendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
                    return compareVariableNames(o1, o2);
                }
                return 1;
            }
        };
    }

    static ViewerComparator variableNamesDescendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
                    final int signReverser = -1;
                    return signReverser * compareVariableNames(o1, o2);
                }
                return 1;
            }
        };
    }

    static ViewerComparator variableValuesAscendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
                    return compareVariableValues(o1, o2);
                }
                return 1;
            }
        };
    }

    static ViewerComparator variableValuesDescendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
                    final int signReverser = -1;
                    return signReverser * compareVariableValues(o1, o2);
                }
                return 1;
            }
        };
    }

    static ViewerComparator variableCommentsAscendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
                    return compareVariableComments(o1, o2);
                }
                return 1;
            }
        };
    }

    static ViewerComparator variableCommentsDescendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
                    final int signReverser = -1;
                    return signReverser * compareVariableComments(o1, o2);
                }
                return 1;
            }
        };
    }

    private static int compareVariableNames(final Object o1, final Object o2) {
        final RobotVariable variable1 = (RobotVariable) o1;
        final RobotVariable variable2 = (RobotVariable) o2;
        return variable1.getName().compareTo(variable2.getName());
    }

    private static int compareVariableValues(final Object o1, final Object o2) {
        final RobotVariable variable1 = (RobotVariable) o1;
        final RobotVariable variable2 = (RobotVariable) o2;
        return variable1.getValue().compareTo(variable2.getValue());
    }

    private static int compareVariableComments(final Object o1, final Object o2) {
        final RobotVariable variable1 = (RobotVariable) o1;
        final RobotVariable variable2 = (RobotVariable) o2;
        return variable1.getComment().compareTo(variable2.getComment());
    }
}
