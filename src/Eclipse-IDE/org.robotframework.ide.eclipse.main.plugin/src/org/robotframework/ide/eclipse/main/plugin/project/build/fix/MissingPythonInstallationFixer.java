package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsPreferencesPage;

public class MissingPythonInstallationFixer implements IMarkerResolution {

    @Override
    public String getLabel() {
        return "Add Robot execution environment in Preferences";
    }

    @Override
    public void run(final IMarker marker) {
        PreferencesUtil.createPreferenceDialogOn(null, InstalledRobotsPreferencesPage.ID,
                new String[] { InstalledRobotsPreferencesPage.ID }, null).open();
    }
}
