package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class RobotCase extends RobotCodeHoldingElement {

    RobotCase(final RobotCasesSection parent, final String name, final String comment) {
        super(parent, name, comment);
    }

    public List<String> getArguments() {
        return new ArrayList<String>();
    }

    @Override
    public RobotCasesSection getParent() {
        return (RobotCasesSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getTestCaseImage();
    }
}
