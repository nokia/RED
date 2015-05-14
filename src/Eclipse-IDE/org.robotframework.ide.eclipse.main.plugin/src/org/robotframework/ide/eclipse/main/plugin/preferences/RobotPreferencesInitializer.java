package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

public class RobotPreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        final IScopeContext scope = DefaultScope.INSTANCE;
        final IEclipsePreferences preferences = scope.getNode(RobotFramework.PLUGIN_ID);

        final File pybotPath = RobotRuntimeEnvironment.whereIsDefaultPython();
        if (pybotPath != null) {
            final String absolutePath = pybotPath.getAbsolutePath();
            preferences.put(InstalledRobotEnvironments.ACTIVE_RUNTIME, absolutePath);
            preferences.put(InstalledRobotEnvironments.OTHER_RUNTIMES, absolutePath);
        }
    }

}
