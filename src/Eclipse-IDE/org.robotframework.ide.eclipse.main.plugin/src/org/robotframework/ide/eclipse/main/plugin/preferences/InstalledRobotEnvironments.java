package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class InstalledRobotEnvironments {

    // active environment is cached, since its retrieval can take a little bit
    private static RobotRuntimeEnvironment ACTIVE = null;
    private static List<RobotRuntimeEnvironment> ALL = null;
    static {
        InstanceScope.INSTANCE.getNode(RobotFramework.PLUGIN_ID).addPreferenceChangeListener(
                new IPreferenceChangeListener() {
                    @Override
                    public void preferenceChange(
                            final org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent event) {
                        if (event == null) {
                            return;
                        } else if (ACTIVE_RUNTIME.equals(event.getKey())) {
                            ACTIVE = createRuntimeEnvironment((String) event.getNewValue());
                        } else if (OTHER_RUNTIMES.equals(event.getKey())) {
                            ALL = createRuntimeEnvironments((String) event.getNewValue());
                        }
                    }
                });
    }

    public static final String OTHER_RUNTIMES = "otherRuntimes";
    public static final String ACTIVE_RUNTIME = "activeRuntime";

    public static synchronized RobotRuntimeEnvironment getActiveRobotInstallation(final IPreferenceStore store) {
        if (ACTIVE == null) {
            ACTIVE = readActiveFromPreferences(store);
        }
        return ACTIVE;
    }
    
    public static synchronized List<RobotRuntimeEnvironment> getAllRobotInstallation(final IPreferenceStore store) {
        if (ALL == null) {
            ALL = readALLFromPreferences(store);
        }
        return ALL;
    }
    
    private static RobotRuntimeEnvironment readActiveFromPreferences(final IPreferenceStore store) {
        return createRuntimeEnvironment(store.getString(ACTIVE_RUNTIME));
    }
    
    private static List<RobotRuntimeEnvironment> readALLFromPreferences(final IPreferenceStore store) {
        return createRuntimeEnvironments(store.getString(OTHER_RUNTIMES));
    }

    private static RobotRuntimeEnvironment createRuntimeEnvironment(final String prefValue) {
        return Strings.isNullOrEmpty(prefValue) ? null : RobotRuntimeEnvironment.create(prefValue);
    }

    private static List<RobotRuntimeEnvironment> createRuntimeEnvironments(final String prefValue) {
        if (Strings.isNullOrEmpty(prefValue)) {
            return null;
        }
        final List<String> all = newArrayList(prefValue.split(";"));
        final List<RobotRuntimeEnvironment> envs = newArrayList(Iterables.transform(all, new Function<String, RobotRuntimeEnvironment>() {
            @Override
            public RobotRuntimeEnvironment apply(final String path) {
                return createRuntimeEnvironment(path);
            }
        }));
        return newArrayList(Iterables.filter(envs, Predicates.notNull()));
    }
}
