/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.DynamicFlag;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.viewers.Stylers;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class ExecutionViewLabelProvider extends RedCommonLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final ExecutionTreeNode node = (ExecutionTreeNode) element;

        final StyledString label = new StyledString();

        final RGB red = new RGB(200, 0, 0);
        final RGB green = new RGB(0, 150, 0);
        final RGB blue = new RGB(0, 0, 200);

        if (node.getDynamic() == DynamicFlag.ADDED) {
            final Styler bracketStyler = Stylers.mixingStyler(Stylers.withForeground(green),
                    Stylers.withFont(JFaceResources.getTextFont()));
            final Styler flagStyler = Stylers.mixingStyler(Stylers.withForeground(green),
                    Stylers.withFont(JFaceResources.getTextFont()), Stylers.withFontStyle(SWT.BOLD));

            label.append("[", bracketStyler);
            label.append("+", flagStyler);
            label.append("]", bracketStyler);
            label.append(" ");

        } else if (node.getDynamic() == DynamicFlag.REMOVED) {
            final Styler bracketStyler = Stylers.mixingStyler(Stylers.withForeground(red),
                    Stylers.withFont(JFaceResources.getTextFont()));
            final Styler flagStyler = Stylers.mixingStyler(Stylers.withForeground(red),
                    Stylers.withFont(JFaceResources.getTextFont()), Stylers.withFontStyle(SWT.BOLD));

            label.append("[", bracketStyler);
            label.append("-", flagStyler);
            label.append("]", bracketStyler);
            label.append(" ");

        } else if (node.getDynamic() == DynamicFlag.OTHER) {
            final Styler bracketStyler = Stylers.mixingStyler(Stylers.withForeground(blue),
                    Stylers.withFont(JFaceResources.getTextFont()));
            final Styler flagStyler = Stylers.mixingStyler(Stylers.withForeground(blue),
                    Stylers.withFont(JFaceResources.getTextFont()), Stylers.withFontStyle(SWT.BOLD));

            label.append("[", bracketStyler);
            label.append("*", flagStyler);
            label.append("]", bracketStyler);
            label.append(" ");
        }
        if (node.getResolvedName() != null) {
            label.append(node.getResolvedName());
        } else {
            label.append(node.getName());
        }

        final int time = node.getElapsedTime();
        if (time >= 0) {
            label.append(String.format(" (%.3f s)", ((double) time) / 1000),
                    Stylers.Common.ECLIPSE_DECORATION_STYLER);
        }
        return label;
    }
    
    @Override
    public Image getImage(final Object element) {
        final ExecutionTreeNode node = (ExecutionTreeNode) element;

        if (node.getKind() == ElementKind.SUITE) {

            if (node.getStatus().isPresent()) {
                if (node.isRunning()) {
                    return ImagesManager.getImage(RedImages.getSuiteInProgressImage());
                } else if (node.isPassed()) {
                    return ImagesManager.getImage(RedImages.getSuitePassImage());
                } else if (node.isFailed()) {
                    return ImagesManager.getImage(RedImages.getSuiteFailImage());
                }
            } else {
                return ImagesManager.getImage(RedImages.getSuiteImage());
            }

        } else if (node.getKind() == ElementKind.TEST) {

            if (node.getStatus().isPresent()) {
                if (node.isRunning()) {
                    return ImagesManager.getImage(RedImages.getTestInProgressImage());
                } else if (node.isPassed()) {
                    return ImagesManager.getImage(RedImages.getTestPassImage());
                } else if (node.isFailed()) {
                    return ImagesManager.getImage(RedImages.getTestFailImage());
                }
            } else {
                return ImagesManager.getImage(RedImages.getTestImage());
            }
        }
        return null;
    }
}
