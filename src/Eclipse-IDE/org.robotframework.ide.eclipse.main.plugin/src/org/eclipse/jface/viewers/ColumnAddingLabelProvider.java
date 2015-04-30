package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class ColumnAddingLabelProvider extends ColumnLabelProvider {

    @Override
    public Color getBackground(final Object element) {
        return null;
    }

    @Override
    public String getText(final Object element) {
        return "";
    }

    @Override
    public String getToolTipText(final Object element) {
        return "Activate this cell to add new columns for arguments";
    }

    @Override
    public Image getToolTipImage(final Object object) {
        return RobotImages.getTooltipAddImage().createImage();
    }
}