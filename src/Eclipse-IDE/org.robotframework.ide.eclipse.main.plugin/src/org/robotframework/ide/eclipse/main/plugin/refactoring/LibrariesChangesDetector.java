/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
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
        for (final ReferencedLibrary library : config.getReferencedLibraries()) {
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
        final IPath potentiallyAffectedPath = Path.fromPortableString(pythonLibrary.getPath());

        if (afterRefactorPath.isPresent()) {

            final Optional<IPath> transformedPath = Changes.transformAffectedPath(beforeRefactorPath,
                    afterRefactorPath.get(), potentiallyAffectedPath);
            if (transformedPath.isPresent()) {
                final String name = getNewLibraryName(segmentedName);
                final String path = transformedPath.get().makeRelative().toPortableString();

                final ReferencedLibrary modifiedLibrary = ReferencedLibrary.create(LibraryType.PYTHON, name, path);
                processor.pathModified(pythonLibrary, modifiedLibrary);
                return;
            }

        } else if (beforeRefactorPath.isPrefixOf(potentiallyAffectedPath)) {
            processor.pathRemoved(config, pythonLibrary);
            return;
        }
    }

    private String getNewLibraryName(final List<String> segmentedName) {
        final String[] beforeSegments = beforeRefactorPath.removeFileExtension().segments();
        final String[] afterSegments = afterRefactorPath.get().removeFileExtension().segments();
        final Range nameInPath = getNameRangeInPath(segmentedName.toArray(new String[segmentedName.size()]),
                beforeSegments);
        final Range changeInBeforePath = getChangeRangeInPaths(beforeSegments, afterSegments);
        final String changedNamePart = getNewNamePart(beforeSegments, afterSegments, changeInBeforePath);

        List<String> name = new ArrayList<>();
        for (int i = 0; i < beforeSegments.length; i++) {
            if (nameInPath.contains(i)) {
                if (changeInBeforePath.contains(i)) {
                    name.add(changedNamePart);
                    i = changeInBeforePath.end - 1;
                } else {
                    name.add(beforeSegments[i]);
                }
            }
        }
        // add unmodified name part and possible class name
        for (int i = nameInPath.size(); i < segmentedName.size(); i++) {
            name.add(segmentedName.get(i));
        }
        return name.stream().filter(p -> !p.isEmpty()).collect(Collectors.joining("."));
    }

    private String getNewNamePart(String[] beforeSegments, String[] afterSegments, Range changeInBeforePath) {
        final int lastSegmentsToRemove = beforeSegments.length - changeInBeforePath.end + 1;
        return String.join(".", Arrays.copyOfRange(afterSegments, changeInBeforePath.start,
                afterSegments.length - lastSegmentsToRemove + 1));
    }

    private Range getNameRangeInPath(final String[] segmentedName, String[] beforeSegments) {
        if ("__init__".equals(beforeSegments[beforeSegments.length - 1])) {
            beforeSegments = Arrays.copyOfRange(beforeSegments, 0, beforeSegments.length - 1);
        }
        final String pathToCompare = String.join(".", beforeSegments);
        for (int i = segmentedName.length; i > 0; i--) {
            if (pathToCompare.endsWith(String.join(".", Arrays.copyOfRange(segmentedName, 0, i)))) {
                return new Range(beforeSegments.length - i, beforeSegments.length);
            }
        }
        return new Range(-1, -1);
    }

    private Range getChangeRangeInPaths(final String[] beforeSegments, final String[] afterSegments) {
        final Range result = new Range(0, 0);
        for (int i = 0; i < beforeSegments.length && i < afterSegments.length; i++) {
            if (!beforeSegments[i].equals(afterSegments[i])) {
                result.start = i;
                break;
            }
        }
        final int pathShift = afterSegments.length - beforeSegments.length;
        for (int i = beforeSegments.length - 1; i > 0 && i + pathShift >= 0; i--) {
            if (!beforeSegments[i].equals(afterSegments[i + pathShift])) {
                result.end = i + 1;
                return result;
            }
        }
        return result;
    }
}
