package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;

public enum CasesAdderState implements TokenState {
    CASE("case"),
    CALL("");

    private final String name;

    private CasesAdderState(final String name) {
        this.name = name;
    }

    @Override
    public String getNewObjectTypeName() {
        return name;
    }
}
