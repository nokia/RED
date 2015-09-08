package org.robotframework.ide.core.testData.model.listener;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.IRobotProjectHolder;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.model.listener.proxy.ModelInvocationHandler;


public class RobotModelEventDispatcher implements IRobotModelEventDispatcher {

    private static final Map<IRobotProjectHolder, RobotModelEventDispatcher> dispatchersPerProject = new LinkedHashMap<>();
    private final IRobotProjectHolder projectHolder;


    private RobotModelEventDispatcher(final IRobotProjectHolder projectHolder) {
        this.projectHolder = projectHolder;
    }


    public static RobotModelEventDispatcher getInstance(
            final IRobotProjectHolder projectHolder) {
        RobotModelEventDispatcher dispatcher = dispatchersPerProject
                .get(projectHolder);
        if (dispatcher == null) {
            dispatcher = new RobotModelEventDispatcher(projectHolder);
            dispatchersPerProject.put(projectHolder, dispatcher);
        }

        return dispatcher;
    }


    public static IRobotProjectHolder listener(final RobotProjectHolder project) {
        IRobotProjectHolder withProxy = ModelInvocationHandler.createListener(
                project, getInstance(project),
                IRobotProjectHolderListenable.class);
        return withProxy;
    }


    @Override
    public IRobotFileOutput listener(final RobotFileOutput robotOutput) {
        IRobotFileOutput withProxy = ModelInvocationHandler.createListener(
                robotOutput, this, IRobotFileOutputListenable.class);

        return withProxy;
    }


    @Override
    public void dispatchEvent(final ARobotModelEvent<?> event) {
        System.out.println("Event :) " + event);
    }
}
