/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;


public class RobotElementChange {

    public enum Kind {
        ADDED, REMOVED, CHANGED
    }

    private final Kind kind;
    private final RobotElement element;

    private RobotElementChange(final Kind kind, final RobotElement element) {
        this.kind = kind;
        this.element = element;
    }

    public static RobotElementChange createAddedElement(final RobotElement element) {
        return new RobotElementChange(Kind.ADDED, element);
    }

    public static RobotElementChange createRemovedElement(final RobotElement element) {
        return new RobotElementChange(Kind.REMOVED, element);
    }

    public static RobotElementChange createChangedElement(final RobotElement element) {
        return new RobotElementChange(Kind.CHANGED, element);
    }

    public Kind getKind() {
        return kind;
    }

    public RobotElement getElement() {
        return element;
    }
}