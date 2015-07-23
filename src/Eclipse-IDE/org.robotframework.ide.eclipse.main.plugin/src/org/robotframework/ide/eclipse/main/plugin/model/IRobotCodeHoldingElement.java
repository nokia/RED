package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

public interface IRobotCodeHoldingElement extends RobotElement {

    @Override
    public List<RobotKeywordCall> getChildren();
}
