package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments;

public class RobotFramework extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.robotframework.ide.eclipse.main.plugin";

    private static RobotFramework plugin;

    private final List<File> installedPythons = new ArrayList<>();

    public static RobotFramework getDefault() {
        return plugin;
    }

    static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    public static RobotModelManager getModelManager() {
        return RobotModelManager.getInstance();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        installedPythons.clear();
        RobotModelManager.getInstance().dispose();
    }

    public RobotRuntimeEnvironment getActiveRobotInstallation() {
        return InstalledRobotEnvironments.getActiveRobotInstallation(getPreferenceStore());
    }

    public RobotRuntimeEnvironment getRobotInstallation(final File file) {
        for (final RobotRuntimeEnvironment env : getAllRuntimeEnvironments()) {
            if (file.equals(env.getFile())) {
                return env;
            }
        }
        return null;
    }

    public List<RobotRuntimeEnvironment> getAllRuntimeEnvironments() {
        return InstalledRobotEnvironments.getAllRobotInstallation(getPreferenceStore());
    }

    public static void log(final int severity, final String message) {
        getDefault().getLog().log(new Status(severity, PLUGIN_ID, message));
    }

    public static void log(final int severity, final String message, final Throwable cause) {
        getDefault().getLog().log(new Status(severity, PLUGIN_ID, message, cause));
    }
}
