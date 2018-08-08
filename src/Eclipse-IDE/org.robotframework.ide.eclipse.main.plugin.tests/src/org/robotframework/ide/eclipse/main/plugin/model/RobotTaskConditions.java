/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import org.assertj.core.api.Condition;

public class RobotTaskConditions {

    public static Condition<RobotTask> properlySetParent() {
        return new Condition<RobotTask>() {

            @Override
            public boolean matches(final RobotTask task) {
                return task.getParent() != null && task.getParent().getChildren().contains(task)
                        && task.getLinkedElement().getParent() != null
                        && task.getLinkedElement().getParent().getTasks().contains(task.getLinkedElement())
                        && task.getParent().getLinkedElement() == task.getLinkedElement().getParent();
            }
        };
    }
}
