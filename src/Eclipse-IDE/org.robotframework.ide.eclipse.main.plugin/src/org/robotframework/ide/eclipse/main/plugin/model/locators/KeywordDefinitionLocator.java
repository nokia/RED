/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;

/**
 * @author Michal Anglart
 */
public class KeywordDefinitionLocator {

    private final IFile file;

    private final RobotModel model;

    public KeywordDefinitionLocator(final IFile file) {
        this(file, RedPlugin.getModelManager().getModel());
    }

    public KeywordDefinitionLocator(final IFile file, final RobotModel model) {
        this.file = file;
        this.model = model;
    }

    public void locateKeywordDefinitionInLibraries(final RobotProject project, final KeywordDetector detector) {
        final ContinueDecision shouldContinue = locateInLibraries(project.getStandardLibraries().values(), detector);
        if (shouldContinue == ContinueDecision.CONTINUE) {
            locateInLibraries(project.getReferencedLibraries().values(), detector);
        }
    }

    private ContinueDecision locateInLibraries(final Collection<LibrarySpecification> collection,
            final KeywordDetector detector) {
        for (final LibrarySpecification libSpec : filter(collection, Predicates.notNull())) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.libraryKeywordDetected(libSpec, kwSpec, "", null);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    public void locateKeywordDefinition(final KeywordDetector detector) {
        final RobotSuiteFile startingFile = model.createSuiteFile(file);
        ContinueDecision shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        final List<IPath> resources = PathsResolver.getWorkspaceRelativeResourceFilesPaths(startingFile);
        shouldContinue = locateInResourceFiles(resources, newHashSet(startingFile.getFile()), startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateInLibraries(startingFile, detector);
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

    private ContinueDecision locateInResourceFiles(final List<IPath> resources, final Set<IFile> alreadyVisited,
            final RobotSuiteFile startingFile, final KeywordDetector detector) {
        for (final IPath path : resources) {
            final IResource resourceFile = file.getWorkspace().getRoot().findMember(path);
            if (resourceFile == null || !resourceFile.exists() || resourceFile.getType() != IResource.FILE
                    || alreadyVisited.contains(resourceFile)) {
                continue;
            }
            alreadyVisited.add((IFile) resourceFile);

            final RobotSuiteFile resourceSuiteFile = model.createSuiteFile((IFile) resourceFile);
            final List<IPath> nestedResources = PathsResolver.getWorkspaceRelativeResourceFilesPaths(resourceSuiteFile);
            ContinueDecision shouldContinue = locateInResourceFiles(nestedResources, alreadyVisited, startingFile,
                    detector);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
            shouldContinue = locateInLibraries(resourceSuiteFile, detector);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
            shouldContinue = locateInCurrentFile(resourceSuiteFile, detector);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInLibraries(final RobotSuiteFile file,
            final KeywordDetector detector) {
        final Map<LibrarySpecification, String> librariesMap = file.getImportedLibraries();
        for (final LibrarySpecification libSpec : librariesMap.keySet()) {
            final List<KeywordSpecification> keywords = libSpec.getKeywords();
            for (final KeywordSpecification kwSpec : keywords) {
                final ContinueDecision shouldContinue = detector.libraryKeywordDetected(libSpec, kwSpec,
                        librariesMap.get(libSpec), file);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    public interface KeywordDetector {

        /**
         * Called when keyword definition was detected inside file.
         * 
         * @param file
         *            File in which definition was detected
         * @param keyword
         *            Detected keyword definition
         * @return A decision whether detection should proceed
         */
        ContinueDecision keywordDetected(RobotSuiteFile file, RobotKeywordDefinition keyword);

        /**
         * Called when keyword of given specification was found within given library which was
         * exposed by given file
         * 
         * @param libSpec
         *            Specification of library where keyword was detected
         * @param kwSpec
         *            Specification of detected keyword
         * @param libraryAlias
         *            Library alias (may be empty)
         * @param exposingFile
         *            The file which imported given library
         * @return A decision whether detection should proceed
         */
        ContinueDecision libraryKeywordDetected(LibrarySpecification libSpec, KeywordSpecification kwSpec,
                String libraryAlias, RobotSuiteFile exposingFile);

    }
}
