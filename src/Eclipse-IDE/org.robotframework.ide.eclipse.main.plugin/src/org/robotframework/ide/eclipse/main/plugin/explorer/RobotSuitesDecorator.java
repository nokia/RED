package org.robotframework.ide.eclipse.main.plugin.explorer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.robotframework.ide.eclipse.main.plugin.RobotFrameworkPluginActivator;
import org.robotframework.ide.eclipse.main.plugin.nature.RobotProjectNature;

public class RobotSuitesDecorator implements ILightweightLabelDecorator {

    @Override
    public void addListener(final ILabelProviderListener listener) {
        // nothing to do here
    }

    @Override
    public void dispose() {
        // nothing to do here
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // nothing to do here
    }

    @Override
    public void decorate(final Object element, final IDecoration decoration) {
        if (element instanceof IFolder && RobotProjectNature.isRobotSuite((IFolder) element)) {
            decoration.addOverlay(RobotFrameworkPluginActivator.getImageDescriptor("resources/robot_dec.png"));
        }
    }

}
