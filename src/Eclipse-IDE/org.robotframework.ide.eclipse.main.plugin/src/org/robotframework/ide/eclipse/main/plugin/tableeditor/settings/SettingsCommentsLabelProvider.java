package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Map.Entry;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CommentsLabelProvider;

class SettingsCommentsLabelProvider extends CommentsLabelProvider {

    @Override
    public Color getBackground(final Object element) {
        if (element instanceof Entry<?, ?> && ((Entry<?, ?>) element).getValue() == null) {
            return new Color(Display.getDefault(), 250, 250, 250);
        } else {
            return null;
        }
    }

    @Override
    protected String getComment(final Object element) {
        final RobotSetting setting = getSetting(element);
        return setting != null ? setting.getComment() : "";
    }

    private RobotSetting getSetting(final Object element) {
        if (element instanceof RobotSetting) {
            return (RobotSetting) element;
        } else if (element instanceof Entry<?, ?>) {
            return (RobotSetting) ((Entry<?, ?>) element).getValue();
        }
        return null;
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotSetting || element instanceof Entry<?, ?>) {
            return super.getToolTipText(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotSetting || element instanceof Entry<?, ?>) {
            return super.getToolTipImage(element);
        }
        return null;
    }
}
