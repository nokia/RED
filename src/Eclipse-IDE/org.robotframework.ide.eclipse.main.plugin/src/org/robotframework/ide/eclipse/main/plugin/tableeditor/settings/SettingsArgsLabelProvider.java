package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

class SettingsArgsLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    private final int index;
    private final boolean withAddingTokenInfo;

    SettingsArgsLabelProvider(final int index) {
        this(index, false);
    }

    SettingsArgsLabelProvider(final int index, final boolean withAddingTokenInfo) {
        this.index = index;
        this.withAddingTokenInfo = withAddingTokenInfo;
    }

    @Override
    public Image getImage(final Object element) {
        if (withAddingTokenInfo && element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return super.getImage(element);
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
        if (element instanceof RobotSetting) {
            return new StyledString(getText(element));
        } else if (withAddingTokenInfo && element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotSetting) {
            final String tooltipText = getText(element);
            return tooltipText.isEmpty() ? "<empty>" : tooltipText;
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
