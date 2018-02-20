/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import static java.util.stream.Collectors.joining;
import static org.eclipse.jface.viewers.Stylers.mixingStyler;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotProjectDependencies.ErroneousLibrarySpecification;
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

            return new StyledString(dependencies.getName());

        } else if (element instanceof ErroneousLibrarySpecification) {
            final ErroneousLibrarySpecification libSpec = (ErroneousLibrarySpecification) element;

            final StyledString label = new StyledString(libSpec.getName(),
                    mixingStyler(Stylers.Common.STRIKEOUT_STYLER, Stylers.Common.ERROR_STYLER));
            label.append(" (non-accessible)", Stylers.Common.ERROR_STYLER);
            return label;

        } else if (element instanceof LibrarySpecification) {
            final LibrarySpecification libSpec = (LibrarySpecification) element;
           
            final String dirtyLibSpecIndicator = libSpec.isModified() ? "*" : "";
            final StyledString styled = new StyledString(dirtyLibSpecIndicator + libSpec.getName());

            final List<String> arguments = libSpec.getDescriptor().getArguments();
            if (!arguments.isEmpty()) {
                styled.append(arguments.stream().collect(joining(", ", " [", "]")),
                        Stylers.Common.ECLIPSE_DECORATION_STYLER);
            }

            final int numberOfKeywords = libSpec.getKeywords() == null ? 0 : libSpec.getKeywords().size();
            styled.append(" (" + numberOfKeywords + ")", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return styled;
        }
        return new StyledString();
    }
}
