/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.red.graphics.ImagesManager;

public class NavigatorLabelProvider extends ColumnLabelProvider {

    @Override
    public void addListener(final ILabelProviderListener listener) {
        // nothing to do
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // nothing to do
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotElement) {
            final ImageDescriptor image = ((RobotElement) element).getImage();
            return image == null ? null : ImagesManager.getImage(image);
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting groupedElement = (RobotSetting) element;
            return groupedElement.getGroup().getName() == null ? groupedElement.getName()
                    : groupedElement.getNameInGroup();
        } else if (element instanceof RobotKeywordCall) {
            return ((RobotKeywordCall) element).getLabel();
        } else if (element instanceof RobotElement) {
            return ((RobotElement) element).getName();
        }
        return "";
    }

}
