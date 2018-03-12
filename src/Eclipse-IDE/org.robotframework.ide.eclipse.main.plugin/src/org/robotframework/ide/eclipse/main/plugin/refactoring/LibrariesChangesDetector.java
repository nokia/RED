/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;

import com.google.common.base.Splitter;

class LibrariesChangesDetector {

    private final IPath beforeRefactorPath;

    private final Optional<IPath> afterRefactorPath;

    private final RobotProjectConfig config;

    LibrariesChangesDetector(final IPath beforeRefactorPath, final Optional<IPath> afterRefactorPath,
            final RobotProjectConfig config) {
        this.beforeRefactorPath = beforeRefactorPath;
        this.afterRefactorPath = afterRefactorPath;
        this.config = config;
    }

    void detect(final RedXmlChangesProcessor<ReferencedLibrary> processor) {
        for (final ReferencedLibrary library : config.getLibraries()) {
            if (library.provideType() == LibraryType.VIRTUAL) {
                detectChangeInVirtualLibrary(processor, library);
            } else if (library.provideType() == LibraryType.JAVA) {
                detectChangeInJavaLibrary(processor, library);
            } else if (library.provideType() == LibraryType.PYTHON) {
                detectChangeInPythonLibrary(processor, library);
            }
        }
    }

    private void detectChangeInVirtualLibrary(final RedXmlChangesProcessor<ReferencedLibrary> processor,
            final ReferencedLibrary virtualLibrary) {

        final IPath potentiallyAffectedPath = Path.fromPortableString(virtualLibrary.getPath());
        if (potentiallyAffectedPath.isAbsolute()) {
            // we only detect changes when affected library is in workspace imported relatively
            return;
        }

        if (afterRefactorPath.isPresent()) {
            final Optional<IPath> transformedPath = Changes.transformAffectedPath(beforeRefactorPath,
                    afterRefactorPath.get(), potentiallyAffectedPath);
            if (transformedPath.isPresent()) {
                final ReferencedLibrary modifiedLibrary = ReferencedLibrary.create(LibraryType.VIRTUAL,
                        transformedPath.get().lastSegment(), transformedPath.get().makeRelative().toPortableString());
                processor.pathModified(virtualLibrary, modifiedLibrary);
            }

        } else if (beforeRefactorPath.isPrefixOf(potentiallyAffectedPath)) {
            processor.pathRemoved(config, virtualLibrary);
        }
    }

    private void detectChangeInJavaLibrary(final RedXmlChangesProcessor<ReferencedLibrary> processor,
            final ReferencedLibrary javaLibrary) {

        final IPath potentiallyAffectedPath = Path.fromPortableString(javaLibrary.getPath());
        if (potentiallyAffectedPath.isAbsolute()) {
            // we only detect changes when affected library is in workspace imported relatively
            return;
        }

        if (afterRefactorPath.isPresent()) {
            final Optional<IPath> transformedPath = Changes.transformAffectedPath(beforeRefactorPath,
                    afterRefactorPath.get(), potentiallyAffectedPath);
            if (transformedPath.isPresent()) {
                final ReferencedLibrary modifiedLibrary = ReferencedLibrary.create(LibraryType.JAVA,
                        javaLibrary.getName(), transformedPath.get().makeRelative().toPortableString());
                processor.pathModified(javaLibrary, modifiedLibrary);
            }

        } else if (beforeRefactorPath.isPrefixOf(potentiallyAffectedPath)) {
            processor.pathRemoved(config, javaLibrary);
        }
    }

    private void detectChangeInPythonLibrary(final RedXmlChangesProcessor<ReferencedLibrary> processor,
            final ReferencedLibrary pythonLibrary) {

        if (Path.fromPortableString(pythonLibrary.getPath()).isAbsolute()) {
            // we only detect changes when affected library is in workspace imported relatively
            return;
        }

        // in case of Python libraries part of path can be encoded in name separated by dots
        // (pythonic notation for modules)
        final List<String> segmentedName = Splitter.on('.').splitToList(pythonLibrary.getName());

        for (int i = 0; i <= segmentedName.size(); i++) {
            // the module may be a directory or .py file
            for (final String extension : newHashSet("", ".py")) {

                final List<String> currentPartFromName = segmentedName.subList(0, i);
                final String partOfPathFromName = currentPartFromName.stream()
                        .collect(joining("/", currentPartFromName.isEmpty() ? "" : "/", ""));

                final IPath potentiallyAffectedPath = Path
                        .fromPortableString(pythonLibrary.getPath() + partOfPathFromName + extension);

                if (afterRefactorPath.isPresent()) {

                    final Optional<IPath> transformedPath = Changes.transformAffectedPath(beforeRefactorPath,
                            afterRefactorPath.get(), potentiallyAffectedPath);
                    if (transformedPath.isPresent()) {
                        final IPath lastSegments = transformedPath.get()
                                .makeRelative()
                                .removeFileExtension()
                                .removeFirstSegments(transformedPath.get().segmentCount() - i)
                                .append(new Path(
                                        segmentedName.subList(i, segmentedName.size()).stream().collect(joining("/"))));

                        final String name = Stream.of(lastSegments.segments()).collect(joining("."));
                        final String path = transformedPath.get()
                                .makeRelative()
                                .removeLastSegments(i)
                                .toPortableString();

                        final ReferencedLibrary modifiedLibrary = ReferencedLibrary.create(LibraryType.PYTHON, name,
                                path);
                        processor.pathModified(pythonLibrary, modifiedLibrary);
                        return;
                    }

                } else if (beforeRefactorPath.isPrefixOf(potentiallyAffectedPath)) {
                    processor.pathRemoved(config, pythonLibrary);
                    return;
                }
            }
        }
    }
}
