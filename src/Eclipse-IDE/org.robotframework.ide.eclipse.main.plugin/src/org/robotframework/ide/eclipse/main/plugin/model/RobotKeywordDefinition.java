package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class RobotKeywordDefinition extends RobotCodeHoldingElement {

    private final List<String> arguments;

    RobotKeywordDefinition(final RobotKeywordsSection parent, final String name, final List<String> arguments,
            final String comment) {
        super(parent, name, comment);
        this.arguments = arguments;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public RobotKeywordsSection getParent() {
        return (RobotKeywordsSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getUserKeywordImage();
    }

}
