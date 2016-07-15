package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

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
