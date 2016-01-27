/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.FindElementUsagesHandler.E4FindUsagesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class FindElementUsagesHandler extends DIParameterizedHandler<E4FindUsagesHandler> {

    public FindElementUsagesHandler() {
        super(E4FindUsagesHandler.class);
    }

    public static class E4FindUsagesHandler {

        @Execute
        public Object findUsages(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named("org.robotframework.ide.eclipse.findElementUsages.place") final String place) {
            final RobotFileInternalElement element = Selections.getSingleElement(selection,
                    RobotFileInternalElement.class);
            final String name = element.getName();

            try {
                final List<IResource> resourcesToLookInto;
                if ("workspace".equals(place)) {
                    resourcesToLookInto = getResourcesToQuery(ResourcesPlugin.getWorkspace().getRoot());
                } else if ("project".equals(place)) {
                    resourcesToLookInto = getResourcesToQuery(element.getSuiteFile().getProject().getProject());
                } else {
                    throw new IllegalStateException("Unknown place for searching: " + place);
                }

                if (place == null) {
                    return null;
                }
                final ISearchQuery query = TextSearchQueryProvider.getPreferred().createQuery(name,
                        resourcesToLookInto.toArray(new IResource[0]));
                NewSearchUI.runQueryInBackground(query);
            } catch (final CoreException e) {
                RedPlugin.logError("Unable to find usages of '" + name + "' in " + place, e);
            }
            return null;
        }

        private List<IResource> getResourcesToQuery(final IWorkspaceRoot workspaceRoot) throws CoreException {
            final List<IResource> resources = newArrayList();
            for (final IResource project : workspaceRoot.members()) {
                resources.addAll(newArrayList(getResourcesToQuery((IProject) project)));
            }
            return resources;
        }

        private List<IResource> getResourcesToQuery(final IProject project) throws CoreException {
            // returns resources from given project excluding libspec folder
            final List<IResource> resources = newArrayList();
            final LibspecsFolder libspecsFolder = LibspecsFolder.get(project);
            if (!libspecsFolder.exists()) {
                resources.add(project);
                return resources;
            }

            for (final IResource child : project.members()) {
                if (!libspecsFolder.getResource().equals(child)) {
                    resources.add(child);
                }
            }
            return resources;
        }
    }
}
