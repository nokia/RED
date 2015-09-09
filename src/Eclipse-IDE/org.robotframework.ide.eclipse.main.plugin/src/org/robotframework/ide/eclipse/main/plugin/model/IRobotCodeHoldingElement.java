/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

public interface IRobotCodeHoldingElement extends RobotElement {

    @Override
    public List<RobotKeywordCall> getChildren();
}
