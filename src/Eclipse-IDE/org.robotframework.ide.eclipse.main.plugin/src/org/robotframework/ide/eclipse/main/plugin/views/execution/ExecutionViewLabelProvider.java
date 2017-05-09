/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class ExecutionViewLabelProvider extends RedCommonLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final ExecutionTreeNode node = (ExecutionTreeNode) element;

        final StyledString label = new StyledString(node.getName());
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
                if (node.getStatus().get() == Status.RUNNING) {
                    return ImagesManager.getImage(RedImages.getSuiteInProgressImage());
                } else if (node.getStatus().get() == Status.PASS) {
                    return ImagesManager.getImage(RedImages.getSuitePassImage());
                } else if (node.getStatus().get() == Status.FAIL) {
                    return ImagesManager.getImage(RedImages.getSuiteFailImage());
                }
            } else {
                return ImagesManager.getImage(RedImages.getSuiteImage());
            }

        } else if (node.getKind() == ElementKind.TEST) {

            if (node.getStatus().isPresent()) {
                if (node.getStatus().get() == Status.RUNNING) {
                    return ImagesManager.getImage(RedImages.getTestInProgressImage());
                } else if (node.getStatus().get() == Status.PASS) {
                    return ImagesManager.getImage(RedImages.getTestPassImage());
                } else if (node.getStatus().get() == Status.FAIL) {
                    return ImagesManager.getImage(RedImages.getTestFailImage());
                }
            } else {
                return ImagesManager.getImage(RedImages.getTestImage());
            }
        }

        return null;
    }
    
}
