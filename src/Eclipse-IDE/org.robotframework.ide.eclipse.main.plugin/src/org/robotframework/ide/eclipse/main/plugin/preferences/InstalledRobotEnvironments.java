package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RobotRuntimeEnvironment;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class InstalledRobotEnvironments {

    public static final String OTHER_RUNTIMES = "otherRuntimes";
    public static final String ACTIVE_RUNTIME = "activeRuntime";

    public static List<RobotRuntimeEnvironment> readFromPreferences(final IPreferenceStore store) {
        final String[] otherRuntimes = store.getString(OTHER_RUNTIMES).split(";");

        List<String> all = newArrayList(otherRuntimes);
        all = newArrayList(Iterables.filter(all, new Predicate<String>() {
            @Override
            public boolean apply(final String path) {
                return !Strings.isNullOrEmpty(path);
            }
        }));

        return newArrayList(Iterables.transform(all, new Function<String, RobotRuntimeEnvironment>() {
            @Override
            public RobotRuntimeEnvironment apply(final String path) {
                return RobotRuntimeEnvironment.create(path);
            }
        }));
    }

    public static RobotRuntimeEnvironment getActiveRobotInstallation(final IPreferenceStore store) {
        final String activeRuntime = store.getString(ACTIVE_RUNTIME);
        return Strings.isNullOrEmpty(activeRuntime) ? null : RobotRuntimeEnvironment.create(activeRuntime);
    }

}
