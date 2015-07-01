package org.robotframework.ide.eclipse.main.plugin;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

public class RobotCaseSetting extends RobotKeywordCall {

    public RobotCaseSetting(final RobotCase parent, final String name, final List<String> args,
            final String comment) {
        super(parent, name, args, comment);
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getTestCaseSettingImage();
    }
}
