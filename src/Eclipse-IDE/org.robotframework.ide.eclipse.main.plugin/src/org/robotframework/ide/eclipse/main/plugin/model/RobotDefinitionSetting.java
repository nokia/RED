/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

public class RobotDefinitionSetting extends RobotKeywordCall {

    public RobotDefinitionSetting(final RobotCodeHoldingElement robotCodeHoldingElement, final String name,
            final List<String> args, final String comment) {
        super(robotCodeHoldingElement, name, args, comment);
    }

}
