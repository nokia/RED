/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
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

            final StyledString styled = new StyledString(dependencies.getName());
            final String additionalInfo = dependencies.getAdditionalInformation();
            if (!additionalInfo.isEmpty()) {
                styled.append(" ");
            }
            return styled.append(additionalInfo, new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RedTheme.getEclipseDecorationColor();
                }
            });
        } else if (element instanceof LibrarySpecification) {
            final LibrarySpecification libSpec = (LibrarySpecification) element;
            final StyledString styled = new StyledString(libSpec.getName());
            final String additonalInfo = libSpec.getAdditionalInformation();
            if (!additonalInfo.isEmpty()) {
                styled.append(" ");
                styled.append(additonalInfo, new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RedTheme.getEclipseDecorationColor();
                }
                });
            }
            styled.append(" ");
            return styled.append("(" + libSpec.getKeywords().size() + ")", new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RedTheme.getEclipseDecorationColor();
                }
            });
        }
        return new StyledString();
    }
}
