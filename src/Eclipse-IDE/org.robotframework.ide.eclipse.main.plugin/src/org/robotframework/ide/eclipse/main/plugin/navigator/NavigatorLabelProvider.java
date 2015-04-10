package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;

public class NavigatorLabelProvider implements ILabelProvider {

	@Override
	public void addListener(final ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {

	}

	@Override
	public Image getImage(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getImage().createImage();
        }
        return null;
	}

	@Override
	public String getText(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getName();
        }
        return "";
	}

}
