/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorPage;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesProjectConfigurationEditorPart.LibrariesProjectConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.viewers.ViewersCombiningSelectionProvider;

public class ReferencedLibrariesProjectConfigurationEditorPart
        extends DIEditorPart<LibrariesProjectConfigurationEditor> {

    public ReferencedLibrariesProjectConfigurationEditorPart() {
        super(LibrariesProjectConfigurationEditor.class);
    }

    static class LibrariesProjectConfigurationEditor extends RedProjectEditorPage {

        private ReferencedLibrariesFormFragment referencedFragment;

        private PathsFormFragment pathsFragment;

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            referencedFragment = new ReferencedLibrariesFormFragment();
            pathsFragment = new PathsFormFragment();
            return newArrayList(referencedFragment, pathsFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return new ViewersCombiningSelectionProvider(referencedFragment.getViewer(),
                    pathsFragment.getPythonPathViewer(), pathsFragment.getClassPathViewer());
        }
    }
}
