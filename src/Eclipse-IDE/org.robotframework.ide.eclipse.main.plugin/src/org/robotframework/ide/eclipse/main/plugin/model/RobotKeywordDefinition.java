package org.robotframework.ide.eclipse.main.plugin.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RobotKeywordDefinition extends RobotCodeHoldingElement {

    public static final String ARGUMENTS = "Arguments";
    public static final String DOCUMENTATION = "Documentation";
    public static final String TIMEOUT = "Timeout";
    public static final String TEARDOWN = "Teardown";
    public static final String RETURN = "Return";
    
    RobotKeywordDefinition(final RobotKeywordsSection parent, final String name, final String comment) {
        super(parent, name, comment);
    }

    @Override
    public RobotKeywordsSection getParent() {
        return (RobotKeywordsSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getUserKeywordImage();
    }

    public boolean hasArguments() {
        return getArgumentsSetting() != null;
    }

    public RobotDefinitionSetting getArgumentsSetting() {
        return findSetting(ARGUMENTS);
    }

    public boolean hasReturnValue() {
        return getReturnValueSetting() != null;
    }

    public RobotDefinitionSetting getReturnValueSetting() {
        return findSetting(RETURN);
    }

    public boolean hasDocumentation() {
        return getDocumentationSetting() != null;
    }

    public RobotDefinitionSetting getDocumentationSetting() {
        return findSetting(DOCUMENTATION);
    }

    public boolean hasTeardownValue() {
        return getTeardownSetting() != null;
    }

    public RobotDefinitionSetting getTeardownSetting() {
        return findSetting(TEARDOWN);
    }

    public boolean hasTimeoutValue() {
        return getTimeoutSetting() != null;
    }

    public RobotDefinitionSetting getTimeoutSetting() {
        return findSetting(TIMEOUT);
    }

    private RobotDefinitionSetting findSetting(final String name) {
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting && call.getName().equals(name)) {
                return (RobotDefinitionSetting) call;
            }
        }
        return null;
    }
}
