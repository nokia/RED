/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

public abstract class RobotProjectElement implements RobotElement {

    private final RobotElement parent;

    RobotProjectElement(final RobotElement parent) {
        this.parent = parent;
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public int getIndex() {
        return parent == null ? -1 : parent.getChildren().indexOf(this);
    }

    public RobotProject getRobotProject() {
        RobotElement current = parent;
        while (current != null && !(current instanceof RobotProject)) {
            current = current.getParent();
        }
        return (RobotProject) current;
    }

}
