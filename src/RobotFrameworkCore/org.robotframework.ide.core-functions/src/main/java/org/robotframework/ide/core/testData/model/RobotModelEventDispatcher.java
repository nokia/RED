package org.robotframework.ide.core.testData.model;

import org.robotframework.ide.core.testData.model.listener.IRobotProjectHolder;
import org.robotframework.ide.core.testData.model.listener.IRobotProjectHolderListenable;
import org.robotframework.ide.core.testData.model.listener.proxy.ModelInvocationHandler;


public class RobotModelEventDispatcher {

    private final IRobotProjectHolder projectHolder;


    public RobotModelEventDispatcher(final IRobotProjectHolder projectHolder) {
        this.projectHolder = projectHolder;
    }


    public static IRobotProjectHolder listener(final IRobotProjectHolder project) {
        IRobotProjectHolder withProxy = ModelInvocationHandler.createListener(
                project, IRobotProjectHolderListenable.class);
        return withProxy;
    }
}
