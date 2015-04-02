package org.robotframework.ide.eclipse.main.plugin.tempmodel;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RobotFrameworkPluginActivator;

public class FileSection {

	private final String sectionName;

	public FileSection(final String sectionName) {
		this.sectionName = sectionName;
	}

	public String getName() {
		return sectionName;
	}

    public ImageDescriptor getImage() {
        return RobotFrameworkPluginActivator.getImageDescriptor("resources/settings.png");
	}
}
