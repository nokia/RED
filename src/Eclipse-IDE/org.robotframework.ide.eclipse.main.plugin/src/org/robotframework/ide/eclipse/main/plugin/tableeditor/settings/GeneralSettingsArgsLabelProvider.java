package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

public class GeneralSettingsArgsLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    private final int index;

    public GeneralSettingsArgsLabelProvider(final int index) {
        this.index = index;
    }

    @Override
    public Color getBackground(final Object element) {
        final Entry<String, RobotElement> entry = getEntry(element);

        if (entry.getValue() == null) {
            return new Color(Display.getDefault(), 250, 250, 250);
        } else {
            return null;
        }
    }

    @Override
    public String getText(final Object element) {
        final Entry<String, RobotElement> entry = getEntry(element);
        final RobotSetting setting = (RobotSetting) entry.getValue();

        if (setting != null) {
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

    @SuppressWarnings("unchecked")
    private Entry<String, RobotElement> getEntry(final Object element) {
        return (Entry<String, RobotElement>) element;
    }
}
