package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;

class MetadataSettingsMatchesCollection extends MatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotSetting) {
            collectMetadataMatches((RobotSetting) element, filter);
        }
    }

    private void collectMetadataMatches(final RobotSetting setting, final String filter) {
        boolean isMatching = false; 

        for (final String argument : setting.getArguments()) {
            isMatching |= collectMatches(filter, argument);
        }
        isMatching |= collectMatches(filter, setting.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }
}
