package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;

public class CodesViewerComparators {
    static ViewerComparator codeNamesAscendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotCodeHoldingElement && o2 instanceof RobotCodeHoldingElement) {
                    return compareCodeHoldingNames(o1, o2);
                }
                return 1;
            }
        };
    }

    static ViewerComparator codeNamesDescendingComparator() {
        return new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object o1, final Object o2) {
                if (o1 instanceof RobotCodeHoldingElement && o2 instanceof RobotCodeHoldingElement) {
                    final int signReverser = -1;
                    return signReverser * compareCodeHoldingNames(o1, o2);
                }
                return 1;
            }
        };
    }

    private static int compareCodeHoldingNames(final Object o1, final Object o2) {
        final RobotCodeHoldingElement code1 = (RobotCodeHoldingElement) o1;
        final RobotCodeHoldingElement code2 = (RobotCodeHoldingElement) o2;
        return code1.getName().compareTo(code2.getName());
    }
}
