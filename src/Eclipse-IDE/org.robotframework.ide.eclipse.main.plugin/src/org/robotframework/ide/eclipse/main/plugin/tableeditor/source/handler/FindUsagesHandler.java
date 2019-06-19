/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

public class FindUsagesHandler {

    public static void findElements(final String place, final RobotFormEditor editor, final String selectedText) {
        try {
            final List<IResource> resourcesToLookInto;
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            if ("workspace".equals(place)) {
                resourcesToLookInto = getResourcesToQuery(root);
            } else if ("project".equals(place)) {
                final IProject project = root.getFile(editor.provideSuiteModel().getFullPath()).getProject();
                resourcesToLookInto = getResourcesToQuery(project);
            } else {
                throw new IllegalStateException("Unknown place for searching: " + place);
            }

            final ISearchQuery query = TextSearchQueryProvider.getPreferred()
                    .createQuery(selectedText, resourcesToLookInto.toArray(new IResource[0]));
            NewSearchUI.runQueryInBackground(query);
        } catch (final CoreException e) {
            RedPlugin.logError("Unable to find usages of '" + selectedText + "' in " + place, e);
        }
    }

    private static List<IResource> getResourcesToQuery(final IWorkspaceRoot workspaceRoot) throws CoreException {
        final List<IResource> resources = new ArrayList<>();
        for (final IResource project : workspaceRoot.members()) {
            resources.addAll(getResourcesToQuery((IProject) project));
        }
        return resources;
    }

    private static List<IResource> getResourcesToQuery(final IProject project) throws CoreException {
        // returns resources from given project excluding libspec folder
        final List<IResource> resources = new ArrayList<>();
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
