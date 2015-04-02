package org.robotframework.ide.eclipse.main.plugin.explorer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.tempmodel.FileSection;

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
		return ((FileSection) element).getImage().createImage();
	}

	@Override
	public String getText(final Object element) {
		return ((FileSection) element).getName();
	}

}
