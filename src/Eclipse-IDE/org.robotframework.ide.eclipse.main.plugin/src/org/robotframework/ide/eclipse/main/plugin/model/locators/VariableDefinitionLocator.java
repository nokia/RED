/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.core.testData.importer.AVariableImported;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;
import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordArguments;
import org.robotframework.ide.core.testData.robotImported.ARobotInternalVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;

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
        ContinueDecision shouldContinue = locateInLocalScope(startingFile, detector, offset);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInResourceFiles(PathsResolver.getAbsoluteResourceFilesPaths(startingFile), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInVariableFiles(PathsResolver.getAbsoluteVariableFilesPaths(startingFile), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateGlobalVariables(startingFile, detector);
    }

    public void locateVariableDefinition(final VariableDetector detector) {
        ContinueDecision shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInResourceFiles(PathsResolver.getAbsoluteResourceFilesPaths(startingFile), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInVariableFiles(PathsResolver.getAbsoluteVariableFilesPaths(startingFile), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateGlobalVariables(startingFile, detector);
    }

    private ContinueDecision locateInLocalScope(final RobotSuiteFile file, final VariableDetector detector,
            final int offset) {
        final Optional<? extends RobotElement> element = file.findElement(offset);
        if (element.isPresent() && element.get() instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element.get();
            final IRobotCodeHoldingElement parent = call.getParent();

            final ContinueDecision shouldContinue = locateInKeywordArguments(file, detector, parent);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
            return locateInPreviousCalls(file, detector, parent, call);
        }
        return ContinueDecision.STOP;
    }

    private ContinueDecision locateInKeywordArguments(final RobotSuiteFile file, final VariableDetector detector,
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
                        return ContinueDecision.STOP;
                    }
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInPreviousCalls(final RobotSuiteFile file, final VariableDetector detector,
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
                        return ContinueDecision.STOP;
                    }
                }
            }
        }
        return ContinueDecision.CONTINUE;
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
        
        final ContinueDecision shouldContinue = locateInLocalVariableFiles(file, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return ContinueDecision.STOP;
        }
        
        return ContinueDecision.CONTINUE;
    }
    
    private ContinueDecision locateInLocalVariableFiles(final RobotSuiteFile file, final VariableDetector detector) {
        for (final VariablesFileImportReference variablesFileImportReference : file.getVariablesFromLocalReferencedFiles()) {
            final String path = variablesFileImportReference.getImportDeclaration()
                    .getPathOrName()
                    .getText()
                    .toString();
            final ReferencedVariableFile localReferencedFile = new ReferencedVariableFile();
            localReferencedFile.setPath(path);
            localReferencedFile.setName(path);
            for (final AVariableImported<?> aVariableImported : variablesFileImportReference.getVariables()) {
                final ContinueDecision shouldContinue = detector.varFileVariableDetected(localReferencedFile,
                        aVariableImported.getRobotRepresentation(), aVariableImported.getValue());
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }
    
    private ContinueDecision locateInResourceFiles(final List<IPath> resources, final VariableDetector detector) {
        for (final IPath path : resources) {
            final IPath wsRelative = PathsConverter.toWorkspaceRelativeIfPossible(path);
            final IResource resourceFile = startingFile.getFile().getWorkspace().getRoot().findMember(wsRelative);
            if (resourceFile == null || !resourceFile.exists() || resourceFile.getType() != IResource.FILE) {
                continue;
            }
            final RobotSuiteFile resourceSuiteFile = getSuiteFile((IFile) resourceFile);
            final List<IPath> nestedResources = PathsResolver.getAbsoluteResourceFilesPaths(resourceSuiteFile);
            locateInResourceFiles(nestedResources, detector);
            final ContinueDecision result = locateInCurrentFile(resourceSuiteFile, detector);
            if(!result.equals(Optional.absent())) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private RobotSuiteFile getSuiteFile(final IFile resourceFile) {
        return useCommonModel ? RedPlugin.getModelManager().createSuiteFile(resourceFile)
                : new RobotSuiteFile(null, resourceFile);
    }

    private ContinueDecision locateInVariableFiles(final List<IPath> absoluteVariablePaths,
            final VariableDetector detector) {

        final List<ReferencedVariableFile> knownParamFiles = startingFile.getProject()
                .getVariablesFromReferencedFiles();
        for (final IPath importedFilePath : absoluteVariablePaths) {
            for (final ReferencedVariableFile knownFile : knownParamFiles) {
                final IPath absKnownFilePath = PathsConverter
                        .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(knownFile.getPath()));

                if (absKnownFilePath.equals(importedFilePath)) {
                    for (final Entry<String, Object> var : knownFile.getVariablesWithProperPrefixes().entrySet()) {
                        final ContinueDecision shouldContinue = detector.varFileVariableDetected(knownFile,
                                var.getKey(), var.getValue());
                        if (shouldContinue == ContinueDecision.STOP) {
                            return ContinueDecision.STOP;
                        }
                    }
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateGlobalVariables(final RobotSuiteFile startingFile,
            final VariableDetector detector) {
        final RobotProjectHolder projectHolder = startingFile.getProject().getRobotProjectHolder();
        final List<ARobotInternalVariable<?>> globalVariables = projectHolder.getGlobalVariables();

        for (final ARobotInternalVariable<?> variable : globalVariables) {
            final ContinueDecision shouldContinue = detector.globalVariableDetected(variable.getName(),
                    variable.getValue());
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    public interface VariableDetector {

        ContinueDecision localVariableDetected(RobotSuiteFile file, RobotToken variable);

        ContinueDecision variableDetected(RobotSuiteFile file, RobotVariable variable);

        ContinueDecision varFileVariableDetected(ReferencedVariableFile file, String variableName, Object value);

        ContinueDecision globalVariableDetected(String name, Object value);
    }
}
