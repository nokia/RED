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
import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordArguments;
import org.robotframework.ide.core.testData.robotImported.ARobotInternalVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
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
    
    public void locateVariableDefinitionWithLocalScope(final VariableDetector detector, final int offset) {
        first(locateInLocalScope(startingFile, detector, offset))
          .or(locateInCurrentFile(startingFile, detector))
          .or(locateInResourceFiles(PathsNormalizer.getNormalizedResourceFilesPaths(startingFile), detector))
          .or(locateInVariableFiles(PathsNormalizer.getNormalizedVariableFilesPaths(startingFile), detector))
          .or(locateGlobalVariables(startingFile, detector));
    }

    public void locateVariableDefinition(final VariableDetector detector) {
        first(locateInCurrentFile(startingFile, detector))
          .or(locateInResourceFiles(PathsNormalizer.getNormalizedResourceFilesPaths(startingFile), detector))
          .or(locateInVariableFiles(PathsNormalizer.getNormalizedVariableFilesPaths(startingFile), detector))
          .or(locateGlobalVariables(startingFile, detector));
    }

    private Optional<VoidResult> first(final Optional<VoidResult> firstStep) {
        return firstStep;
    }

    private Optional<VoidResult> locateInLocalScope(final RobotSuiteFile file, final VariableDetector detector,
            final int offset) {
        final Optional<? extends RobotElement> element = file.findElement(offset);
        if (element.isPresent() && element.get() instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element.get();
            final IRobotCodeHoldingElement parent = call.getParent();

            locateInKeywordArguments(file, detector, parent).or(locateInPreviousCalls(file, detector, parent, call));
        }
        return Optional.absent();
    }

    private Optional<VoidResult> locateInKeywordArguments(final RobotSuiteFile file, final VariableDetector detector,
            final IRobotCodeHoldingElement parent) {
        if (parent instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) parent;
            final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
            final AModelElement<?> linkedElement = argumentsSetting == null ? null
                    : argumentsSetting.getLinkedElement();
            if (linkedElement instanceof KeywordArguments) {
                final KeywordArguments args = (KeywordArguments) linkedElement;
                for (final RobotToken token : args.getArguments()) {
                    final ContinueDecision shouldContinue = detector.localVariableDetected(file, token);
                    if (shouldContinue == ContinueDecision.STOP) {
                        return Optional.of(new VoidResult());
                    }
                }
            }
        }
        return Optional.absent();
    }

    private Optional<VoidResult> locateInPreviousCalls(final RobotSuiteFile file, final VariableDetector detector,
            final IRobotCodeHoldingElement parent, final RobotKeywordCall call) {
        final List<RobotKeywordCall> children = parent.getChildren();
        final int index = children.indexOf(call);
        for (int i = 0; i < index; i++) {
            final AModelElement<?> linkedElement = children.get(i).getLinkedElement();
            if (linkedElement instanceof RobotExecutableRow<?>) {
                final RobotExecutableRow<?> executableRow = (RobotExecutableRow<?>) linkedElement;
                
                for (final RobotToken token : executableRow.buildLineDescription().getAssignments()) {
                    final ContinueDecision shouldContinue = detector.localVariableDetected(file, token);
                    if (shouldContinue == ContinueDecision.STOP) {
                        return Optional.of(new VoidResult());
                    }
                }
            }
        }
        return Optional.absent();
    }

    private Optional<VoidResult> locateInCurrentFile(final RobotSuiteFile file, final VariableDetector detector) {
        final Optional<RobotVariablesSection> section = file.findSection(RobotVariablesSection.class);
        if (!section.isPresent()) {
            return Optional.absent();
        }
        for (final RobotVariable var : section.get().getChildren()) {
            final ContinueDecision shouldContinue = detector.variableDetected(file, var);
            if (shouldContinue == ContinueDecision.STOP) {
                return Optional.of(new VoidResult());
            }
        }
        return Optional.absent();
    }

    private Optional<VoidResult> locateInResourceFiles(final List<IPath> resources, final VariableDetector detector) {
        // FIXME : we should somehow be able to handle absolute files (probably by linking the file)
        for (final IPath path : resources) {
            if (!path.isAbsolute()) {
                final IFile resourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                final RobotSuiteFile resourceSuiteFile = getSuiteFile(resourceFile);

                return locateInCurrentFile(resourceSuiteFile, detector);
            }
        }
        return Optional.absent();
    }

    private RobotSuiteFile getSuiteFile(final IFile resourceFile) {
        return useCommonModel ? RedPlugin.getModelManager().createSuiteFile(resourceFile)
                : new RobotSuiteFile(null, resourceFile);
    }

    private Optional<VoidResult> locateInVariableFiles(final List<IPath> variables, final VariableDetector detector) {
        // TODO : implement for java, python and yaml files
        return Optional.absent();
    }

    private Optional<VoidResult> locateGlobalVariables(final RobotSuiteFile startingFile,
            final VariableDetector detector) {
        final RobotProjectHolder projectHolder = startingFile.getProject().getRobotProjectHolder();
        final List<ARobotInternalVariable<?>> globalVariables = projectHolder.getGlobalVariables();

        for (final ARobotInternalVariable<?> variable : globalVariables) {
            final ContinueDecision shouldContinue = detector.globalVariableDetected(variable.getName(),
                    variable.getValue());
            if (shouldContinue == ContinueDecision.STOP) {
                return Optional.of(new VoidResult());
            }
        }
        return Optional.absent();
    }

    public interface VariableDetector {

        ContinueDecision variableDetected(RobotSuiteFile file, RobotVariable variable);

        ContinueDecision localVariableDetected(RobotSuiteFile file, RobotToken variable);

        ContinueDecision globalVariableDetected(String name, Object value);
    }

    private static class VoidResult {
        // nothing to define
    }
}
