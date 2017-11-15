/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ResourceImportsPathsResolver;

public class LibraryImportCollector {

    public static List<LibraryImport> collectLibraryImportsIncludingNestedResources(final RobotSuiteFile suite) {
        return collectLibraryImportsIncludingNestedResources(suite, new HashSet<>());
    }

    private static List<LibraryImport> collectLibraryImportsIncludingNestedResources(final RobotSuiteFile suite,
            final Set<IResource> alreadyVisited) {
        final RobotModel model = (RobotModel) suite.getProject().getParent();

        final List<LibraryImport> imports = collectLibraryImports(suite);
        for (final IFile resourceFile : findResourceImportFiles(suite, alreadyVisited)) {
            alreadyVisited.add(resourceFile);
            final RobotSuiteFile resourceSuite = model.createSuiteFile(resourceFile);
            imports.addAll(collectLibraryImportsIncludingNestedResources(resourceSuite, alreadyVisited));
        }
        return imports;
    }

    private static List<IFile> findResourceImportFiles(final RobotSuiteFile suite,
            final Set<IResource> alreadyVisited) {
        final IWorkspaceRoot workspaceRoot = suite.getFile().getWorkspace().getRoot();
        return ResourceImportsPathsResolver.getWorkspaceRelativeResourceFilesPaths(suite)
                .stream()
                .distinct()
                .map(path -> workspaceRoot.findMember(path))
                .filter(res -> isExistingFile(res) && !alreadyVisited.contains(res))
                .map(IFile.class::cast)
                .collect(Collectors.toList());
    }

    private static boolean isExistingFile(final IResource res) {
        return res != null && res.exists() && res.getType() == IResource.FILE;
    }

    private static List<LibraryImport> collectLibraryImports(final RobotSuiteFile currentModel) {
        final List<LibraryImport> imports = new ArrayList<>();
        final Optional<RobotSettingsSection> settingsSection = currentModel.findSection(RobotSettingsSection.class);
        if (settingsSection.isPresent()) {
            for (final RobotKeywordCall setting : settingsSection.get().getLibrariesSettings()) {
                imports.add((LibraryImport) setting.getLinkedElement());
            }
        }
        return imports;
    }

}
