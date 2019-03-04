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
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.LibrariesProjectConfigurationEditorPart.LibrariesProjectConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.viewers.ViewersCombiningSelectionProvider;

public class LibrariesProjectConfigurationEditorPart
        extends DIEditorPart<LibrariesProjectConfigurationEditor> {

    public LibrariesProjectConfigurationEditorPart() {
        super(LibrariesProjectConfigurationEditor.class);
    }

    static class LibrariesProjectConfigurationEditor extends RedProjectEditorPage {

        private LibrariesFormFragment librariesFragment;

        private PathsFormFragment pathsFragment;

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            librariesFragment = new LibrariesFormFragment();
            pathsFragment = new PathsFormFragment();
            return newArrayList(librariesFragment, pathsFragment);
        }

        @Override
        protected int getNumberOfColumnsInForm() {
            return 2;
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return new ViewersCombiningSelectionProvider(librariesFragment.getReferencedLibrariesViewer(),
                    librariesFragment.getRemoteLocationsViewer(), pathsFragment.getPythonPathViewer(),
                    pathsFragment.getClassPathViewer());
        }
    }
}
