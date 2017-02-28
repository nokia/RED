/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.List;

import org.eclipse.core.runtime.CoreException;

public interface ITagsRobotLaunchConfiguration extends IRobotLaunchConfiguration {

    void setIsIncludeTagsEnabled(final boolean isIncludeTagsEnabled) throws CoreException;

    void setIncludedTags(final List<String> tags) throws CoreException;

    void setIsExcludeTagsEnabled(final boolean isExcludeTagsEnabled) throws CoreException;

    void setExcludedTags(final List<String> tags) throws CoreException;

    boolean isIncludeTagsEnabled() throws CoreException;

    List<String> getIncludedTags() throws CoreException;

    boolean isExcludeTagsEnabled() throws CoreException;

    List<String> getExcludedTags() throws CoreException;

}
