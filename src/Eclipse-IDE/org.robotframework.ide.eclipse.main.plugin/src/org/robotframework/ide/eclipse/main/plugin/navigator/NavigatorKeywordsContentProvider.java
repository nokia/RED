/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public class NavigatorKeywordsContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return null;
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof LibrarySpecification) {
            List<KeywordSpecification> keywords = ((LibrarySpecification) parentElement).getKeywords();
            if (keywords != null) {
                return keywords.toArray();
            }
        }
        return new Object[0];
    }

    @Override
    public Object getParent(final Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof LibrarySpecification;
    }

}
