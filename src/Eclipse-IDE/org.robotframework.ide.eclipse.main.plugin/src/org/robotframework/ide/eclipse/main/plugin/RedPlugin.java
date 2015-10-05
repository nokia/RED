/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.robotframework.ide.core.executor.RobotCommandExecutor;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

public class RedPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.robotframework.ide.eclipse.main.plugin";

    private static RedPlugin plugin;

    private final List<File> installedPythons = new ArrayList<>();

    public static RedPlugin getDefault() {
        return plugin;
    }

    public static RobotModelManager getModelManager() {
        return RobotModelManager.getInstance();
    }

    static ImageDescriptor getImageDescriptor(final String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
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
        ColorsManager.disposeColors();
        FontsManager.disposeFonts();
        ImagesManager.disposeImages();
        RobotModelManager.getInstance().dispose();
        RobotCommandExecutor.getInstance().close();
    }

    public RedPreferences getPreferences() {
        return new RedPreferences(getPreferenceStore());
    }

    public RobotRuntimeEnvironment getActiveRobotInstallation() {
        return InstalledRobotEnvironments.getActiveRobotInstallation(new RedPreferences(getPreferenceStore()));
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
        return InstalledRobotEnvironments.getAllRobotInstallation(new RedPreferences(getPreferenceStore()));
    }

    public static void logInfo(final String message) {
        getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    public static void logWarning(final String message) {
        getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    public static void logWarning(final String message, final Throwable cause) {
        getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, cause));
    }

    public static void logError(final String message, final Throwable cause) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, cause));
    }
}
