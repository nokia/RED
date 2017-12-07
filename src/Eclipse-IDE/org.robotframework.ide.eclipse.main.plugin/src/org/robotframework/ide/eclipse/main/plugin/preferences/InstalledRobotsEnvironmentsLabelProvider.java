/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.Arrays;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

public abstract class InstalledRobotsEnvironmentsLabelProvider extends ColumnLabelProvider {

    private final CheckboxTableViewer viewer;

    private InstalledRobotsEnvironmentsLabelProvider(final CheckboxTableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public Color getForeground(final Object element) {
        final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) element;
        if (!env.isValidPythonInstallation()) {
            return viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_RED);
        } else if (!env.hasRobotInstalled()) {
            return viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
        }
        return null;
    }

    @Override
    public Font getFont(final Object element) {
        if (Arrays.asList(viewer.getCheckedElements()).contains(element)) {
            return FontsManager.transformFontWithStyle(viewer.getTable().getFont(), SWT.BOLD);
        }
        return super.getFont(element);
    }

    @Override
    public String getToolTipText(final Object element) {
        final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) element;
        if (!env.isValidPythonInstallation()) {
            return "The location " + env.getFile().getAbsolutePath() + " does not seem to be a valid python directory.";
        } else if (!env.hasRobotInstalled()) {
            return "The python installation " + env.getFile().getAbsolutePath()
                    + " does not seem to have robot framework installed.";
        }
        return "Python installation in " + env.getFile().getAbsolutePath() + " has " + env.getVersion();
    }

    @Override
    public Image getToolTipImage(final Object element) {
        final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) element;
        if (!env.isValidPythonInstallation()) {
            return ImagesManager.getImage(RedImages.getTooltipProhibitedImage());
        } else if (!env.hasRobotInstalled()) {
            return ImagesManager.getImage(RedImages.getTooltipWarnImage());
        }
        return ImagesManager.getImage(RedImages.getTooltipImage());
    }

    public static class InstalledRobotsNamesLabelProvider extends InstalledRobotsEnvironmentsLabelProvider {

        public InstalledRobotsNamesLabelProvider(final CheckboxTableViewer viewer) {
            super(viewer);
        }

        @Override
        public String getText(final Object element) {
            final String version = ((RobotRuntimeEnvironment) element).getVersion();
            return version == null ? "<unknown>" : version;
        }
    }

    public static class InstalledRobotsPathsLabelProvider extends InstalledRobotsEnvironmentsLabelProvider {

        public InstalledRobotsPathsLabelProvider(final CheckboxTableViewer viewer) {
            super(viewer);
        }

        @Override
        public String getText(final Object element) {
            return ((RobotRuntimeEnvironment) element).getFile().getAbsolutePath();
        }
    }
}
