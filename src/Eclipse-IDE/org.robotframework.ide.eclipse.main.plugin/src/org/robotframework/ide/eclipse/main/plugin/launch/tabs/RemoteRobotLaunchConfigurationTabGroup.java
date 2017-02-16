/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;


public class RemoteRobotLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
        setTabs(new ILaunchConfigurationTab[] {
                new RobotLaunchConfigurationRemoteTab(ILaunchManager.RUN_MODE.equals(mode)),
                new SourceLookupTab(),
                new EnvironmentTab(),
                new CommonTab()
        });
    }

}
