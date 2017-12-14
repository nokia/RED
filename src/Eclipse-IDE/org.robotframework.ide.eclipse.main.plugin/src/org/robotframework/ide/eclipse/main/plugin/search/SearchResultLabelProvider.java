/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.KeywordWithParent;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.LibraryWithParent;
import org.robotframework.ide.eclipse.main.plugin.search.SearchResultContentProvider.Libs;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;

/**
 * @author Michal Anglart
 *
 */
class SearchResultLabelProvider extends RedCommonLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof IResource) {
            final IResource resource = (IResource) element;
            final IWorkbenchAdapter workbenchAdapter = resource.getAdapter(IWorkbenchAdapter.class);
            return new StyledString(workbenchAdapter.getLabel(resource));

        } else if (element instanceof Libs) {
            return new StyledString("Libraries");

        } else if (element instanceof LibraryWithParent) {
            return ((LibraryWithParent) element).getLabel();

        } else if (element instanceof KeywordWithParent) {
            return ((KeywordWithParent) element).getLabel();

        } else if (element instanceof DocumentationMatch) {
            return ((DocumentationMatch) element).getStyledLabel();
        }
        return new StyledString();
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof IResource) {
            final IResource resource = (IResource) element;
            final IWorkbenchAdapter workbenchAdapter = resource.getAdapter(IWorkbenchAdapter.class);
            return ImagesManager.getImage(workbenchAdapter.getImageDescriptor(resource));

        } else if (element instanceof Libs) {
            return ImagesManager.getImage(RedImages.getLibraryImage());

        } else if (element instanceof LibraryWithParent) {
            return ImagesManager.getImage(RedImages.getBookImage());

        } else if (element instanceof KeywordWithParent) {
            return ImagesManager.getImage(RedImages.getKeywordImage());

        } else if (element instanceof Match) {
            return ImagesManager.getImage(RedImages.getSearchMarkerImage());
        }
        return null;
    }
}
