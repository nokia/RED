/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.ExcludedResources;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchTarget;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 */
class SearchQueryTargets {

    private final Set<IFile> filesToSearch = new LinkedHashSet<>();

    private final Multimap<IProject, LibrarySpecification> librariesToSearch = LinkedHashMultimap.create();

    private final RobotModel model;

    public SearchQueryTargets(final RobotModel model) {
        this.model = model;
    }

    Set<IFile> getResourcesToSearch() {
        return filesToSearch;
    }

    Multimap<IProject, LibrarySpecification> getLibrariesToSearch() {
        return librariesToSearch;
    }

    void collect(final List<IResource> resourcesRoots, final Set<SearchTarget> targets) {
        for (final IResource resourceRoot : resourcesRoots) {
            if (isClosedOrNonRobotProject(resourceRoot)) {
                continue;
            }

            final IProject project = resourceRoot.getProject();
            if (resourceRoot.getType() == IResource.PROJECT && !librariesToSearch.containsKey(project)) {
                final RobotProject robotProject = model.createRobotProject(project);
                librariesToSearch.putAll(project, robotProject.getLibrarySpecifications());
            }

            try {
                resourceRoot.accept(new IResourceVisitor() {

                    @Override
                    public boolean visit(final IResource resource) throws CoreException {
                        if (resource.getType() == IResource.FILE && !ExcludedResources.isHiddenInEclipse(resource)) {
                            final IFile file = (IFile) resource;
                            if (targets.contains(SearchTarget.SUITE) && ASuiteFileDescriber.isSuiteFile(file)) {
                                filesToSearch.add(file);
                            } else if (targets.contains(SearchTarget.RESOURCE)
                                    && (ASuiteFileDescriber.isInitializationFile(file)
                                            || ASuiteFileDescriber.isResourceFile(file))) {
                                filesToSearch.add(file);
                            }
                        }
                        return true;
                    }
                });
            } catch (final CoreException e) {
                // then we'll try to get those resources we would be able to
            }
        }
    }

    private boolean isClosedOrNonRobotProject(final IResource resourceRoot) {
        return resourceRoot instanceof IProject
                && (!((IProject) resourceRoot).isOpen() || !RobotProjectNature.hasRobotNature((IProject) resourceRoot));
    }
}
