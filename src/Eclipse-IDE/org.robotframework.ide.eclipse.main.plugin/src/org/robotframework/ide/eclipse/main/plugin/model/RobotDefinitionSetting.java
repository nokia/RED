package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

public class RobotDefinitionSetting extends RobotKeywordCall {

    public RobotDefinitionSetting(final RobotCodeHoldingElement robotCodeHoldingElement, final String name,
            final List<String> args, final String comment) {
        super(robotCodeHoldingElement, name, args, comment);
    }

}
