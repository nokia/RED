package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

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
        return getReturnValueSetting() != null;
    }

    public RobotDefinitionSetting getDocumentationSetting() {
        return findSetting(DOCUMENTATION);
    }

    public boolean hasTeardownValue() {
        return getReturnValueSetting() != null;
    }

    public RobotDefinitionSetting getTeardownSetting() {
        return findSetting(TEARDOWN);
    }

    public boolean hasTimeoutValue() {
        return getReturnValueSetting() != null;
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
