/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.executor.SuiteExecutor;

/**
 * @author Michal Anglart
 *
 */
public class RobotLaunchConfigurationMock extends RobotLaunchConfiguration {

    private final String projectName;

    private boolean isUsingProjectInterpreter;

    private final HashMap<String, List<String>> suites = new HashMap<>();

    public RobotLaunchConfigurationMock(final String projectName) {
        super(null);
        this.projectName = projectName;
        this.isUsingProjectInterpreter = true;
    }

    @Override
    public String getProjectName() throws CoreException {
        return projectName;
    }

    @Override
    public boolean isUsingInterpreterFromProject() throws CoreException {
        return isUsingProjectInterpreter;
    }

    @Override
    public void setUsingInterpreterFromProject(final boolean usesProjectExecutor) {
        this.isUsingProjectInterpreter = usesProjectExecutor;
    }

    @Override
    public SuiteExecutor getExecutor() throws CoreException {
        return SuiteExecutor.Python;
    }

    @Override
    public Map<String, List<String>> getSuitePaths() throws CoreException {
        return suites;
    }

    public void addSuite(final String suitePath, final List<String> cases) {
        suites.put(suitePath, cases);
    }

    @Override
    public String getExecutorArguments() throws CoreException {
        return "";
    }

    @Override
    public String getInterpeterArguments() throws CoreException {
        return "";
    }

    @Override
    public boolean isIncludeTagsEnabled() throws CoreException {
        return false;
    }

    @Override
    public boolean isExcludeTagsEnabled() throws CoreException {
        return false;
    }
}
