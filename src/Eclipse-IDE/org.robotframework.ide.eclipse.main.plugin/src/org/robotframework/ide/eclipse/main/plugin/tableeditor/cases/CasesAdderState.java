/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;

public enum CasesAdderState implements TokenState {
    CASE("case"),
    CALL("call");

    private final String name;

    private CasesAdderState(final String name) {
        this.name = name;
    }

    @Override
    public String getNewObjectTypeName() {
        return name;
    }
}
