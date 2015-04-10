package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;

public class RobotTheme {

    private static final String HIGHLIGHTED_CELL_COLOR = "org.robotframework.ide.eclipse.cell.highlight";

    public static Color getHighlightedCellColor() {
        return getColorRegistry().get(HIGHLIGHTED_CELL_COLOR);
    }

    private static ColorRegistry getColorRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    }

}
