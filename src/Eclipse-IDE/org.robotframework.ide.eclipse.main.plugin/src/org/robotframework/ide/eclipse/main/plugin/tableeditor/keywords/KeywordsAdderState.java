/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;


public enum KeywordsAdderState implements TokenState {
    KEYWORD("keyword"),
    CALL("call");

    private String name;

    private KeywordsAdderState(final String name) {
        this.name = name;
    }

    @Override
    public String getNewObjectTypeName() {
        return name;
    }
}
