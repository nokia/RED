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
import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
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

            final StyledString label = new StyledString();
            label.append(libSpec.getName(), mixingStyler(Stylers.Common.STRIKEOUT_STYLER, Stylers.Common.ERROR_STYLER));
            label.append(argumentsDecoration(libSpec.getDescriptor()), Stylers.Common.ERROR_STYLER);
            label.append(" (non-accessible)", Stylers.Common.ERROR_STYLER);
            return label;

        } else if (element instanceof LibrarySpecification) {
            final LibrarySpecification libSpec = (LibrarySpecification) element;
           
            final StyledString label = new StyledString();
            label.append(modificationDecoration(libSpec));
            label.append(libSpec.getName());
            label.append(argumentsDecoration(libSpec.getDescriptor()), Stylers.Common.ECLIPSE_DECORATION_STYLER);
            label.append(numberOfKeywordsDecoration(libSpec), Stylers.Common.ECLIPSE_DECORATION_STYLER);
            return label;
        }
        return new StyledString();
    }

    private static String modificationDecoration(final LibrarySpecification libSpec) {
        return libSpec.isModified() ? "*" : "";
    }

    private static String argumentsDecoration(final LibraryDescriptor descriptor) {
        final List<String> arguments = descriptor.getArguments();
        if (arguments.isEmpty()) {
            return "";
        }
        return arguments.stream().collect(joining(", ", " [", "]"));
    }

    private static String numberOfKeywordsDecoration(final LibrarySpecification libSpec) {
        final int numberOfKeywords = libSpec.getKeywords() == null ? 0 : libSpec.getKeywords().size();
        return " (" + numberOfKeywords + ")";
    }
}
