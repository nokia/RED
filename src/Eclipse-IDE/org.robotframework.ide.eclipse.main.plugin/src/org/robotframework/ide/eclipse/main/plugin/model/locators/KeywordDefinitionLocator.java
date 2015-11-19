/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;

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

    public void locateKeywordDefinition(final KeywordDetector detector) {
        final RobotSuiteFile startingFile = model.createSuiteFile(file);
        ContinueDecision shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        final List<IPath> resources = PathsResolver.getAbsoluteResourceFilesPaths(startingFile);
        shouldContinue = locateInResourceFiles(resources, newHashSet(startingFile.getFile()), startingFile, detector);
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

    private ContinueDecision locateInResourceFiles(final List<IPath> resources, final Set<IFile> alreadyVisited,
            final RobotSuiteFile startingFile, final KeywordDetector detector) {
        for (final IPath path : resources) {
            final IPath wsRelative = PathsConverter.toWorkspaceRelativeIfPossible(path);
            final IResource resourceFile = file.getWorkspace().getRoot().findMember(wsRelative);
            if (resourceFile == null || !resourceFile.exists() || resourceFile.getType() != IResource.FILE
                    || alreadyVisited.contains(resourceFile)) {
                continue;
            }
            alreadyVisited.add((IFile) resourceFile);

            final RobotSuiteFile resourceSuiteFile = model.createSuiteFile((IFile) resourceFile);
            final List<IPath> nestedResources = PathsResolver.getAbsoluteResourceFilesPaths(resourceSuiteFile);
            ContinueDecision shouldContinue = locateInResourceFiles(nestedResources, alreadyVisited, startingFile, detector);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
            shouldContinue = locateInLibrariesFromResourceFile(resourceSuiteFile.getImportedLibraries(), startingFile, detector);
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
    
    private ContinueDecision locateInLibrariesFromResourceFile(final Map<LibrarySpecification, String> librariesMap,
            final RobotSuiteFile startingFile, final KeywordDetector detector) {
        for (final LibrarySpecification spec : librariesMap.keySet()) {
            if (!spec.isAccessibleWithoutImport()) {
                boolean isFromNestedLibrary = true;
                if (startingFile.getImportedLibraries().containsKey(spec)) {
                    isFromNestedLibrary = false;
                }
                for (final KeywordSpecification kwSpec : spec.getKeywords()) {
                    final ContinueDecision shouldContinue = detector.libraryKeywordDetected(spec, kwSpec,
                            librariesMap.get(spec), isFromNestedLibrary);
                    if (shouldContinue == ContinueDecision.STOP) {
                        return ContinueDecision.STOP;
                    }
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInLibraries(final Collection<LibrarySpecification> collection,
            final KeywordDetector detector) {
        for (final LibrarySpecification libSpec : collection) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.libraryKeywordDetected(libSpec, kwSpec, "", false);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }
    
    private ContinueDecision locateInLibraries(final Map<LibrarySpecification, String> librariesMap,
            final KeywordDetector detector) {
        for (final LibrarySpecification libSpec : librariesMap.keySet()) {
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final ContinueDecision shouldContinue = detector.libraryKeywordDetected(libSpec, kwSpec,
                        librariesMap.get(libSpec), false);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    public interface KeywordDetector {

        ContinueDecision keywordDetected(RobotSuiteFile file, RobotKeywordDefinition keyword);

        ContinueDecision libraryKeywordDetected(LibrarySpecification libSpec, KeywordSpecification kwSpec,
                String libraryAlias, boolean isFromNestedLibrary);

    }
    
    public static class KeywordNameSplitter {

        private final String name;

        private final String source;

        public KeywordNameSplitter(final String name, final String source) {
            this.name = name;
            this.source = source;
        }

        public static KeywordNameSplitter splitKeywordName(final String keywordName) {
            final String[] split = keywordName.split("\\.");
            if (split.length == 1) {
                return new KeywordNameSplitter(split[0], "");
            } else if (split.length == 2) {
                return new KeywordNameSplitter(split[1], split[0]);
            }

            return new KeywordNameSplitter("", "");
        }

        public String getKeywordName() {
            return name;
        }

        public String getKeywordSource() {
            return source;
        }
    }
}
