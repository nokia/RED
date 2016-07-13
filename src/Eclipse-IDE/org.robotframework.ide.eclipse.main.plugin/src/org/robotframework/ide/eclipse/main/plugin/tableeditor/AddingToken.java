/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import com.google.common.base.Preconditions;

/**
 * @author Michal Anglart
 *
 */
public class AddingToken {

    private final Object parent;

    private final TokenState[] states;

    private int current;

    public AddingToken(final Object parent, final TokenState... states) {
        Preconditions.checkArgument(states.length > 0);
        this.parent = parent;
        this.states = states;
        this.current = 0;
    }

    public Object getParent() {
        return parent;
    }

    public TokenState getState() {
        return states[current];
    }

    public void switchToNext() {
        current = (current + 1) % states.length;
    }

    public String getLabel() {
        return isNested() ? "..." : "...add new " + states[current].getNewObjectTypeName();
    }

    public boolean isNested() {
        return parent != null;
    }

    public static interface TokenState {

        String getNewObjectTypeName();

    }
}
