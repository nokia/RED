package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

public class NavigatorLabelProvider implements ILabelProvider {

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
            return ((RobotElement) element).getImage().createImage();
        }
        return null;
	}

	@Override
	public String getText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting groupedElement = (RobotSetting) element;
            return groupedElement.getGroup().getName() == null ? groupedElement.getName() : groupedElement
                    .getNameInGroup();
        } else if (element instanceof RobotElement) {
            return ((RobotElement) element).getName();
        }
        return "";
	}

}
