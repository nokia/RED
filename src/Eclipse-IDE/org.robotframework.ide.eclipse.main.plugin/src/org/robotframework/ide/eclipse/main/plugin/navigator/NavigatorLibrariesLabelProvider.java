package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotLibrary;

public class NavigatorLibrariesLabelProvider implements ILabelProvider {

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
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return "Robot Standard libraries";
        } else if (element instanceof RobotLibrary) {
            return ((RobotLibrary) element).getName();
        }
        return "";
    }


}
