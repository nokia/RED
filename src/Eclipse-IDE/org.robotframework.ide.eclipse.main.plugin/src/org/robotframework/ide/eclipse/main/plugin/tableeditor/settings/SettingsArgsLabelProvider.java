package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

public class SettingsArgsLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    private final int index;

    public SettingsArgsLabelProvider(final int index) {
        this.index = index;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> arguments = setting.getArguments();
            if (index < arguments.size()) {
                return arguments.get(index);
            }
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(getText(element));
    }

    @Override
    public String getToolTipText(final Object element) {
        final String tooltipText = getText(element);
        return tooltipText.isEmpty() ? "<empty>" : tooltipText;
    }

    @Override
    public Image getToolTipImage(final Object object) {
        return RobotImages.getTooltipImage().createImage();
    }
}
