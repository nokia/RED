package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Map.Entry;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CommentsLabelProvider;

class GeneralSettingsCommentsLabelProvider extends CommentsLabelProvider {

    @Override
    public Color getBackground(final Object element) {
        // FIXME : resource leak
        return getSetting(element) == null ? new Color(Display.getDefault(), 250, 250, 250) : null;
    }

    @Override
    protected String getComment(final Object element) {
        final RobotSetting setting = getSetting(element);
        return setting != null ? setting.getComment() : "";
    }

    private RobotSetting getSetting(final Object element) {
        return (RobotSetting) ((Entry<?, ?>) element).getValue();
    }
}
