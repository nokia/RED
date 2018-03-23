/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class LibraryImportCollector {

    public static Map<RobotSuiteFile, List<LibraryImport>> collectLibraryImportsIncludingNestedResources(
            final RobotSuiteFile suite) {
        return collectLibraryImportsIncludingNestedResources(suite, new HashSet<>());
    }

    private static Map<RobotSuiteFile, List<LibraryImport>> collectLibraryImportsIncludingNestedResources(
            final RobotSuiteFile suite, final Set<IResource> alreadyVisited) {
        final RobotModel model = (RobotModel) suite.getProject().getParent();

        final Map<RobotSuiteFile, List<LibraryImport>> imports = collectLibraryImports(suite);
        for (final IFile resourceFile : findResourceImportFiles(suite, alreadyVisited)) {
            alreadyVisited.add(resourceFile);
            final RobotSuiteFile resourceSuite = model.createSuiteFile(resourceFile);
            imports.putAll(collectLibraryImportsIncludingNestedResources(resourceSuite, alreadyVisited));
        }
        return imports;
    }

    private static List<IFile> findResourceImportFiles(final RobotSuiteFile suite,
            final Set<IResource> alreadyVisited) {
        return suite.getImportedResources()
                .stream()
                .distinct()
                .filter(res -> isExistingFile(res) && !alreadyVisited.contains(res))
                .map(IFile.class::cast)
                .collect(Collectors.toList());
    }

    private static boolean isExistingFile(final IResource res) {
        return res.exists() && res.getType() == IResource.FILE;
    }

    private static Map<RobotSuiteFile, List<LibraryImport>> collectLibraryImports(final RobotSuiteFile currentModel) {
        final Map<RobotSuiteFile, List<LibraryImport>> imports = new HashMap<>();
        final Optional<RobotSettingsSection> settings = currentModel.findSection(RobotSettingsSection.class);
        if (settings.isPresent()) {
            final List<LibraryImport> libraryImports = settings.get()
                    .getLibrariesSettings()
                    .stream()
                    .map(RobotSetting::getLinkedElement)
                    .map(LibraryImport.class::cast)
                    .collect(toList());
            if (!libraryImports.isEmpty()) {
                imports.put(currentModel, libraryImports);
            }
        }
        return imports;
    }

}
