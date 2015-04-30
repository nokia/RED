package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

public class MetadataSettingsValuesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting metadataSetting = (RobotSetting) element;
            final List<String> arguments = metadataSetting.getArguments();

            return arguments.size() > 1 ? arguments.get(1) : "";
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(getText(element));
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotSetting) {
            final String value = getText(element);
            return value.isEmpty() ? "<empty>" : value;
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
