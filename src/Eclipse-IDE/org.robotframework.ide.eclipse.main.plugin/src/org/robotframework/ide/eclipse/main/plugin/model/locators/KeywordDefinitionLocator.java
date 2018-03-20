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
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 */
public class KeywordDefinitionLocator {

    private final IFile file;

    private final RobotModel model;

    private final boolean includeNotImportedLibraries;

    public KeywordDefinitionLocator(final IFile file) {
        this(file, RedPlugin.getModelManager().getModel());
    }

    public KeywordDefinitionLocator(final IFile file, final RobotModel model) {
        this(file, model, false);
    }

    public KeywordDefinitionLocator(final IFile file, final RobotModel model,
            final boolean includeNotImportedLibraries) {
        this.file = file;
        this.model = model;
        this.includeNotImportedLibraries = includeNotImportedLibraries;
    }

    public void locateKeywordDefinitionInLibraries(final RobotProject project, final KeywordDetector detector) {
        final ContinueDecision shouldContinue = locateInLibraries(project.getStandardLibraries().values(), detector);
        if (shouldContinue == ContinueDecision.CONTINUE) {
            locateInLibraries(project.getReferencedLibraries().values(), detector);
        }
    }

    private ContinueDecision locateInLibraries(final Collection<LibrarySpecification> libSpecs,
            final KeywordDetector detector) {
        for (final LibrarySpecification libSpec : filter(libSpecs, Predicates.notNull())) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.nonAccessibleLibraryKeywordDetected(libSpec, kwSpec,
                        null);
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
        final List<IResource> resources = startingFile.getImportedResources();
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

    private ContinueDecision locateInResourceFiles(final List<IResource> resources, final Set<IFile> alreadyVisited,
            final RobotSuiteFile startingFile, final KeywordDetector detector) {
        for (final IResource resourceFile : resources) {
            if (!resourceFile.exists() || resourceFile.getType() != IResource.FILE
                    || alreadyVisited.contains(resourceFile)) {
                continue;
            }
            alreadyVisited.add((IFile) resourceFile);

            final RobotSuiteFile resourceSuiteFile = model.createSuiteFile((IFile) resourceFile);
            final List<IResource> nestedResources = resourceSuiteFile.getImportedResources();
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

    private ContinueDecision locateInLibraries(final RobotSuiteFile file, final KeywordDetector detector) {
        final Multimap<LibrarySpecification, Optional<String>> importedLibs = file.getImportedLibraries();
        for (final LibrarySpecification libSpec : importedLibs.keySet()) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.accessibleLibraryKeywordDetected(libSpec, kwSpec,
                        importedLibs.get(libSpec), file);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }

        if (!includeNotImportedLibraries) {
            return ContinueDecision.CONTINUE;
        }

        final Set<LibrarySpecification> notImportedLibs = file.getNotImportedLibraries();
        for (final LibrarySpecification libSpec : notImportedLibs) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.nonAccessibleLibraryKeywordDetected(libSpec, kwSpec,
                        file);
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
         * @param libraryAliases
         *            Library aliases (may be empty)
         * @param exposingFile
         *            The file which imported given library
         * @return A decision whether detection should proceed
         */
        ContinueDecision accessibleLibraryKeywordDetected(LibrarySpecification libSpec, KeywordSpecification kwSpec,
                Collection<Optional<String>> libraryAliases, RobotSuiteFile exposingFile);

        /**
         * Called when keyword of given specification was found within given library which is about
         * to be exposed by given file
         *
         * @param libSpec
         *            Specification of library where keyword was detected
         * @param kwSpec
         *            Specification of detected keyword
         * @param exposingFile
         *            The file which is about to import given library
         * @return A decision whether detection should proceed
         */
        ContinueDecision nonAccessibleLibraryKeywordDetected(LibrarySpecification libSpec, KeywordSpecification kwSpec,
                RobotSuiteFile exposingFile);

    }
}
