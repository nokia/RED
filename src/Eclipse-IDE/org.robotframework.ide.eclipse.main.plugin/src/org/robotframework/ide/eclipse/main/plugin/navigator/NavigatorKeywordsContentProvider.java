/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.red.viewers.TreeContentProvider;

public class NavigatorKeywordsContentProvider extends TreeContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        return null;
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof LibrarySpecification) {
            final List<KeywordSpecification> keywords = ((LibrarySpecification) parentElement).getKeywords();
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
