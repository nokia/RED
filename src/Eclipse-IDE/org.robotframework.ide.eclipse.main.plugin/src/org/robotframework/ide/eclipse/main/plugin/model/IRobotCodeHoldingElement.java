/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

public interface IRobotCodeHoldingElement extends RobotFileInternalElement {

    @Override
    public List<RobotKeywordCall> getChildren();

    public void removeChild(RobotKeywordCall call);
}
