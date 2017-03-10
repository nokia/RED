/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfiguration;

public class RemoteRobotLaunchConfiguration extends AbstractRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.remoteRobotLaunchConfiguration";

    public RemoteRobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
    }

    @Override
    public List<IResource> getResourcesUnderDebug() throws CoreException {
        return newArrayList(getProject());
    }

    @Override
    public boolean isDefiningProjectDirectly() {
        return true;
    }

    @Override
    public boolean isRemoteAgent() throws CoreException {
        return true;
    }

}
