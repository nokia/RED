package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

public class VariablesViewerComparator extends ViewerComparator {

    private int columnIndex = -1;

    private static final int DESC = -1;

    private static final int ASC = 1;

    private int direction = DESC;

    private int clickCounter = 0;

    public int getDirection() {
        if (direction == DESC) {
            clickCounter = 0;
            return SWT.DOWN;
        } else if (direction == ASC) {
            return SWT.UP;
        } else {
            return SWT.NONE;
        }
    }

    public void setColumn(int column) {

        if (column == this.columnIndex) {
            // Same column as last sort; toggle the direction
            direction = 1 - clickCounter;
            clickCounter++;
        } else {
            // New column; set descending sort
            this.columnIndex = column;
            direction = DESC;
            clickCounter = 0;
        }
    }

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        if (o1 instanceof RobotVariable && o2 instanceof RobotVariable) {
            RobotVariable variable1 = (RobotVariable) o1;
            RobotVariable variable2 = (RobotVariable) o2;
            int result = 0;
            switch (columnIndex) {
                case 0:
                    result = variable1.getName().compareToIgnoreCase(variable2.getName());
                    break;
                case 1:
                    result = variable1.getValue().compareToIgnoreCase(variable2.getValue());
                    break;
                case 2:
                    result = variable1.getComment().compareToIgnoreCase(variable2.getComment());
                    break;
                default:
                    result = 0;
            }
            // If descending order, flip the direction
            if (direction == DESC) {
                result = -result;
            }

            return result;
        }
        return 1;
    }
}
