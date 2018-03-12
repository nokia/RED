/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

public class FindUsagesHandler {

    public static void findElements(final String place, RobotFormEditor editor, final String selectedText) {
        IFile file = getUnderlayingfile(editor);
        try {
            final List<IResource> resourcesToLookInto;
            if ("workspace".equals(place)) {
                resourcesToLookInto = getResourcesToQuery(ResourcesPlugin.getWorkspace().getRoot());
            } else if ("project".equals(place)) {
                resourcesToLookInto = getResourcesToQuery(file.getProject().getProject());

            } else {
                throw new IllegalStateException("Unknown place for searching: " + place);
            }

            final ISearchQuery query = TextSearchQueryProvider.getPreferred().createQuery(selectedText,
                    resourcesToLookInto.toArray(new IResource[0]));
            NewSearchUI.runQueryInBackground(query);
        } catch (final CoreException e) {
            RedPlugin.logError("Unable to find usages of '" + selectedText + "' in " + place, e);
        }
    }

    private static List<IResource> getResourcesToQuery(final IWorkspaceRoot workspaceRoot) throws CoreException {
        final List<IResource> resources = newArrayList();
        for (final IResource project : workspaceRoot.members()) {
            resources.addAll(getResourcesToQuery((IProject) project));
        }
        return resources;
    }

    private static List<IResource> getResourcesToQuery(final IProject project) throws CoreException {
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

    public static boolean isTSV(RobotFormEditor editor) {
        IFile file = getUnderlayingfile(editor);
        return file.getFileExtension().equals("tsv");
    }

    private static IFile getUnderlayingfile(RobotFormEditor editor) {
        final IEditorInput input = editor.getEditorInput();
        final FileEditorInput editorfile = (FileEditorInput) input;
        final IPath path = editorfile.getPath();
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
    }
}
