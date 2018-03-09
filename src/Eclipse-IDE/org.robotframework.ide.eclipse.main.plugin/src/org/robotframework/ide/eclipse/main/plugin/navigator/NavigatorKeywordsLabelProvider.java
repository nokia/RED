/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
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
            final KeywordSpecification kwSpecification = (KeywordSpecification) element;
            if (!kwSpecification.isDeprecated()) {
                return new StyledString(kwSpecification.getName());
            } else {
                final StyledString label = new StyledString(kwSpecification.getName(), new Styler() {
                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = ColorsManager.getColor(245, 160, 70);
                        textStyle.strikeout = true;
                    }
                });
                label.append(" (deprecated)", new Styler() {
                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = ColorsManager.getColor(245, 160, 70);
                    }
                });
                return label;
            }
        }
        return new StyledString();
    }

}
