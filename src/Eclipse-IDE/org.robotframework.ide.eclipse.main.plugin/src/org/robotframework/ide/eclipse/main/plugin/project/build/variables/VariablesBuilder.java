/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.variables;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SubMonitor;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;

public class VariablesBuilder {

    public void buildVariables(final RobotProject project, final RobotRuntimeEnvironment runtimeEnvironment,
            final RobotProjectConfig configuration, final SubMonitor monitor) {
        monitor.subTask("generating variables from variable files");

        final List<ReferencedVariableFile> referencedVariableFiles = configuration.getReferencedVariableFiles();
        if (referencedVariableFiles == null) {
            project.setReferencedVariableFiles(null);
            monitor.done();
            return;
        }

        monitor.setWorkRemaining(referencedVariableFiles.size());
        for (final ReferencedVariableFile referencedVariableFile : referencedVariableFiles) {
            if (monitor.isCanceled()) {
                return;
            }
            monitor.subTask("generating variables from " + referencedVariableFile.getName() + " file");
            @SuppressWarnings("unchecked")
            final Map<String, Object> varsMap = (Map<String, Object>) runtimeEnvironment.getVariablesFromFile(
                    referencedVariableFile.getPath(), referencedVariableFile.getArguments());
            if (varsMap != null && !varsMap.isEmpty()) {
                referencedVariableFile.setVariables(varsMap);
            }
            monitor.worked(1);
        }
        project.setReferencedVariableFiles(referencedVariableFiles);
        monitor.done();
    }
}
