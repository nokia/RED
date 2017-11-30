/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotProjectDependencies.ErroneousLibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

public class NavigatorLibrariesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return ImagesManager.getImage(RedImages.getLibraryImage());
        } else if (element instanceof LibrarySpecification) {
            return ImagesManager.getImage(RedImages.getBookImage());
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return "Robot Standard libraries";
        } else if (element instanceof LibrarySpecification) {
            return ((LibrarySpecification) element).getName();
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            final RobotProjectDependencies dependencies = (RobotProjectDependencies) element;

            return new StyledString(dependencies.getName());
        } else if (element instanceof ErroneousLibrarySpecification) {
            final ErroneousLibrarySpecification libSpec = (ErroneousLibrarySpecification) element;

            final StyledString label = new StyledString(libSpec.getName(), new Styler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = ColorsManager.getColor(255, 0, 0);
                    textStyle.strikeout = true;
                }
            });
            label.append(" (non-accessible)", new Styler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = ColorsManager.getColor(255, 0, 0);
                }
            });
            return label;

        } else if (element instanceof LibrarySpecification) {

            final LibrarySpecification libSpec = (LibrarySpecification) element;
           
            final String dirtyLibSpecIndicator = libSpec.isModified() ? "*":"";
            final StyledString styled = new StyledString(dirtyLibSpecIndicator + libSpec.getName());
            final String additonalInfo = libSpec.getSecondaryKey();
            if (!additonalInfo.isEmpty()) {
                styled.append(" ");
                styled.append(additonalInfo, new Styler() {

                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = RedTheme.Colors.getEclipseDecorationColor();
                    }
                });
            }
            styled.append(" ");
            int numberOfKeywords = 0;
            final List<KeywordSpecification> keywords = libSpec.getKeywords();
            if (keywords != null) {
                numberOfKeywords = keywords.size();
            }
            styled.append("(" + numberOfKeywords + ")", new Styler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RedTheme.Colors.getEclipseDecorationColor();
                }
            });
            return styled;
        }
        return new StyledString();
    }
}
