/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
public class KeywordDefinitionLocator {

    private final RobotSuiteFile startingFile;

    private final boolean useCommonModel;

    public KeywordDefinitionLocator(final RobotSuiteFile file) {
        this(file, true);
    }

    public KeywordDefinitionLocator(final RobotSuiteFile file, final boolean useCommonModel) {
        this.startingFile = file;
        this.useCommonModel = useCommonModel;
    }

    public void locateKeywordDefinitionInLibraries(final RobotProject project, final KeywordDetector detector) {
        final ContinueDecision shouldContinue = locateInLibraries(project.getStandardLibraries().values(), detector);
        if (shouldContinue == ContinueDecision.CONTINUE) {
            locateInLibraries(project.getReferencedLibraries().values(), detector);
        }
    }

    public void locateKeywordDefinition(final KeywordDetector detector) {
        ContinueDecision shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        final List<IPath> resources = PathsResolver.getAbsoluteResourceFilesPaths(startingFile);
        shouldContinue = locateInResourceFiles(resources, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateInLibraries(startingFile.getImportedLibraries(), detector);
    }

    private ContinueDecision locateInCurrentFile(final RobotSuiteFile file, final KeywordDetector detector) {
        final Optional<RobotKeywordsSection> section = file.findSection(RobotKeywordsSection.class);
        if (!section.isPresent()) {
            return ContinueDecision.CONTINUE;
        }
        for (final RobotKeywordDefinition keyword : section.get().getChildren()) {
            final ContinueDecision shouldContinue = detector.keywordDetected(file, keyword);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInResourceFiles(final List<IPath> resources, final KeywordDetector detector) {
        for (final IPath path : resources) {
            final IPath wsRelative = PathsConverter.toWorkspaceRelativeIfPossible(path);
            final IResource resourceFile = startingFile.getFile().getWorkspace().getRoot().findMember(wsRelative);
            if (resourceFile == null || !resourceFile.exists() || resourceFile.getType() != IResource.FILE) {
                continue;
            }
            final RobotSuiteFile resourceSuiteFile = getSuiteFile((IFile) resourceFile);
            final List<IPath> nestedResources = PathsResolver.getAbsoluteResourceFilesPaths(resourceSuiteFile);
            locateInResourceFiles(nestedResources, detector);
            final ContinueDecision shouldContinue = locateInCurrentFile(resourceSuiteFile, detector);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private RobotSuiteFile getSuiteFile(final IFile resourceFile) {
        return useCommonModel ? RedPlugin.getModelManager().createSuiteFile(resourceFile)
                : new RobotSuiteFile(null, resourceFile);
    }

    private ContinueDecision locateInLibraries(final Collection<LibrarySpecification> collection,
            final KeywordDetector detector) {
        for (final LibrarySpecification libSpec : collection) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.libraryKeywordDetected(libSpec, kwSpec);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    public interface KeywordDetector {

        ContinueDecision keywordDetected(RobotSuiteFile file, RobotKeywordDefinition keyword);

        ContinueDecision libraryKeywordDetected(LibrarySpecification libSpec, KeywordSpecification kwSpec);

    }
}
