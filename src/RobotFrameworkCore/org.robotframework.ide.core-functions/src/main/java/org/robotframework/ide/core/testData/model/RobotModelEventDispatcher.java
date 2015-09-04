package org.robotframework.ide.core.testData.model;

import org.robotframework.ide.core.testData.model.listener.IRobotProjectHolder;


public class RobotModelEventDispatcher {

    private final IRobotProjectHolder projectHolder;


    public RobotModelEventDispatcher(final IRobotProjectHolder projectHolder) {
        this.projectHolder = projectHolder;
    }
}
