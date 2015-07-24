package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class RobotKeywordDefinition extends RobotCodeHoldingElement {

    public static final String RETURN = "Return";
    public static final String TEARDOWN = "Teardown";
    public static final String TIMEOUT = "Timeout";
    public static final String DOCUMENTATION = "Documentation";
    public static final String ARGUMENTS = "Arguments";
    public static List<String> ALLOWED_SETTINGS = newArrayList(ARGUMENTS, DOCUMENTATION, TIMEOUT, TEARDOWN,
            RETURN);
    
    RobotKeywordDefinition(final RobotKeywordsSection parent, final String name, final String comment) {
        super(parent, name, comment);
    }

    @Override
    public RobotKeywordsSection getParent() {
        return (RobotKeywordsSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getUserKeywordImage();
    }

    public boolean hasArguments() {
        return getArgumentsSetting() != null;
    }

    public RobotDefinitionSetting getArgumentsSetting() {
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting && call.getName().equals(ARGUMENTS)) {
                return (RobotDefinitionSetting) call;
            }
        }
        return null;
    }

}
