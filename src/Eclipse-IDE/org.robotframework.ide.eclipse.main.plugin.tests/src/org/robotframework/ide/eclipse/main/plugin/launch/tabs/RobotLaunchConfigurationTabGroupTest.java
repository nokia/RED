/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.junit.Test;

public class RobotLaunchConfigurationTabGroupTest {

    @Test
    public void fourTabsAreCreated_forRobotLaunchConfigInRunMode() {
        final RobotLaunchConfigurationTabGroup group = new RobotLaunchConfigurationTabGroup();
        group.createTabs(mock(ILaunchConfigurationDialog.class), ILaunchManager.RUN_MODE);

        final ILaunchConfigurationTab[] tabs = group.getTabs();
        assertThat(tabs.length).isEqualTo(4);
        assertThat(tabs[0]).isInstanceOf(RobotLaunchConfigurationMainTab.class);
        assertThat(tabs[1]).isInstanceOf(SourceLookupTab.class);
        assertThat(tabs[2]).isInstanceOf(CommonTab.class);
        assertThat(tabs[3]).isInstanceOf(RobotLaunchConfigurationRemoteTab.class);
    }

    @Test
    public void fourTabsAreCreated_forRobotLaunchConfigInDebugMode() {
        final RobotLaunchConfigurationTabGroup group = new RobotLaunchConfigurationTabGroup();
        group.createTabs(mock(ILaunchConfigurationDialog.class), ILaunchManager.DEBUG_MODE);

        final ILaunchConfigurationTab[] tabs = group.getTabs();
        assertThat(tabs.length).isEqualTo(4);
        assertThat(tabs[0]).isInstanceOf(RobotLaunchConfigurationMainTab.class);
        assertThat(tabs[1]).isInstanceOf(SourceLookupTab.class);
        assertThat(tabs[2]).isInstanceOf(CommonTab.class);
        assertThat(tabs[3]).isInstanceOf(RobotLaunchConfigurationRemoteTab.class);
    }

}
