/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.io.File;
import java.util.function.Predicate;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.PythonVersion;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.annotations.VisibleForTesting;

public abstract class InstalledRobotsEnvironmentsLabelProvider extends ColumnLabelProvider {

    private final Predicate<Object> elementShouldBeMarkedBold;

    @VisibleForTesting
    public InstalledRobotsEnvironmentsLabelProvider(final Predicate<Object> elementShouldBeMarkedBold) {
        this.elementShouldBeMarkedBold = elementShouldBeMarkedBold;
    }

    @Override
    public Color getForeground(final Object element) {
        final IRuntimeEnvironment env = (IRuntimeEnvironment) element;
        if (!env.isValidPythonInstallation()) {
            return ColorsManager.getColor(SWT.COLOR_RED);

        } else if (!env.hasRobotInstalled() || PythonVersion.from(env.getVersion()).isDeprecated()
                || env.getRobotVersion().isDeprecated()) {
            return ColorsManager.getColor(SWT.COLOR_DARK_YELLOW);
        }
        return null;
    }

    @Override
    public Font getFont(final Object element) {
        final Font fontToUse = super.getFont(element);
        return elementShouldBeMarkedBold.test(element) ? FontsManager.transformFontWithStyle(fontToUse, SWT.BOLD)
                : fontToUse;
    }

    @Override
    public String getToolTipText(final Object element) {
        final IRuntimeEnvironment env = (IRuntimeEnvironment) element;
        final String dirPath = env.getFile().getAbsolutePath();
        if (!env.isValidPythonInstallation()) {
            return String.format("The location '%s' does not seem to be a valid Python directory", dirPath);
        }
        final String execPath = dirPath + File.separator + env.getInterpreter().executableName();
        if (!env.hasRobotInstalled()) {
            return String.format("Python installation '%s' does not seem to have Robot Framework installed", execPath);
        }
        if (PythonVersion.from(env.getVersion()).isDeprecated()) {
            return String.format(
                    "Python installation '%s' has deprecated version (%s). RED or Robot Framework may be not compatible with it.",
                    execPath, PythonVersion.from(env.getVersion()).asString());
        }
        if (env.getRobotVersion().isDeprecated()) {
            return String.format(
                    "Robot Framework installation has deprecated version (%s). RED may be not compatible with it.",
                    env.getRobotVersion().asString());
        }
        return String.format("Python installation '%s' has %s", execPath, env.getVersion());
    }

    @Override
    public Image getToolTipImage(final Object element) {
        final IRuntimeEnvironment env = (IRuntimeEnvironment) element;
        if (!env.isValidPythonInstallation()) {
            return ImagesManager.getImage(RedImages.getTooltipProhibitedImage());
        } else if (!env.hasRobotInstalled() || PythonVersion.from(env.getVersion()).isDeprecated()
                || env.getRobotVersion().isDeprecated()) {
            return ImagesManager.getImage(RedImages.getTooltipWarnImage());
        }
        return ImagesManager.getImage(RedImages.getTooltipImage());
    }

    public static class InstalledRobotsNamesLabelProvider extends InstalledRobotsEnvironmentsLabelProvider {

        public InstalledRobotsNamesLabelProvider() {
            this(elem -> false);
        }

        public InstalledRobotsNamesLabelProvider(final Predicate<Object> elementShouldBeMarkedBold) {
            super(elementShouldBeMarkedBold);
        }

        @Override
        public String getText(final Object element) {
            return ((IRuntimeEnvironment) element).getVersion();
        }
    }

    public static class InstalledRobotsPathsLabelProvider extends InstalledRobotsEnvironmentsLabelProvider {

        public InstalledRobotsPathsLabelProvider() {
            super(elem -> false);
        }

        public InstalledRobotsPathsLabelProvider(final Predicate<Object> elementShouldBeMarkedBold) {
            super(elementShouldBeMarkedBold);
        }

        @Override
        public String getText(final Object element) {
            return ((IRuntimeEnvironment) element).getFile().getAbsolutePath();
        }
    }
}
