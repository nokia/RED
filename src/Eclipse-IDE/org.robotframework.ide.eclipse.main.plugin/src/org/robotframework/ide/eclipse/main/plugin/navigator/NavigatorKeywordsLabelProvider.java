/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.red.graphics.ImagesManager;

public class NavigatorKeywordsLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    @Override
    public Image getImage(final Object element) {
        if (element instanceof KeywordSpecification) {
            return ImagesManager.getImage(RedImages.getKeywordImage());
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof KeywordSpecification) {
            return ((KeywordSpecification) element).getName();
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof KeywordSpecification) {
            return new StyledString(((KeywordSpecification) element).getName());
        }
        return new StyledString();
    }

}
