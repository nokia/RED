package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class MetadataSettingsNamesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting metadataSetting = (RobotSetting) element;
            return new StyledString(metadataSetting.getNameInGroup());
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting metadataSetting = (RobotSetting) element;
            final String name = metadataSetting.getNameInGroup();
            return name.isEmpty() ? "<empty>" : name;
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotSetting) {
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}
