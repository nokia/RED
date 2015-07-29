package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;

class SettingsMatchesFilter extends ViewerFilter {

    private final MatchesCollection matches;

    SettingsMatchesFilter(final MatchesCollection matches) {
        this.matches = matches;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof RobotSetting) {
            return settingMatches((RobotSetting) element);
        } else if (element instanceof Entry<?, ?>) {
            final RobotSetting setting = (RobotSetting) ((Entry<?, ?>) element).getValue();
            if (setting != null) {
                return settingMatches(setting);
            }
        }
        return true;
    }

    private boolean settingMatches(final RobotSetting setting) {
        return matches.contains(setting.getName()) || matches.contains(setting.getComment())
                || matchesContainsArgument(setting.getArguments());
    }

    private boolean matchesContainsArgument(final List<String> arguments) {
        for (final String argument : arguments) {
            if (matches.contains(argument)) {
                return true;
            }
        }
        return false;
    }
}
