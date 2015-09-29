/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
public class VariableDefinitionLocator {

    private final RobotSuiteFile startingFile;

    private final boolean useCommonModel;

    public VariableDefinitionLocator(final RobotSuiteFile file) {
        this(file, true);
    }

    public VariableDefinitionLocator(final RobotSuiteFile file, final boolean useCommonModel) {
        this.startingFile = file;
        this.useCommonModel = useCommonModel;
    }

    public void locateVariableDefinition(final VariableDetector detector) {
        ContinueDecision shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInResourceFiles(PathsNormalizer.getNormalizedResourceFilesPaths(startingFile), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateInVariableFiles(PathsNormalizer.getNormalizedVariableFilesPaths(startingFile), detector);
    }

    private ContinueDecision locateInCurrentFile(final RobotSuiteFile file, final VariableDetector detector) {
        final Optional<RobotVariablesSection> section = file.findSection(RobotVariablesSection.class);
        if (!section.isPresent()) {
            return ContinueDecision.CONTINUE;
        }
        for (final RobotVariable var : section.get().getChildren()) {
            final ContinueDecision shouldContinue = detector.variableDetected(file, var);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInResourceFiles(final List<IPath> resources, final VariableDetector detector) {
        // FIXME : we should somehow be able to handle absolute files (probably by linking the file)
        for (final IPath path : resources) {
            if (!path.isAbsolute()) {
                final IFile resourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                final RobotSuiteFile resourceSuiteFile = getSuiteFile(resourceFile);
                final ContinueDecision shouldContinue = locateInCurrentFile(resourceSuiteFile, detector);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private RobotSuiteFile getSuiteFile(final IFile resourceFile) {
        return useCommonModel ? RedPlugin.getModelManager().createSuiteFile(resourceFile)
                : new RobotSuiteFile(null, resourceFile);
    }

    private ContinueDecision locateInVariableFiles(final List<IPath> variables, final VariableDetector detector) {
        // TODO : implement for java, python and yaml files
        return ContinueDecision.CONTINUE;
    }

    public interface VariableDetector {

        ContinueDecision variableDetected(RobotSuiteFile file, RobotVariable variable);

    }
}
