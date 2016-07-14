package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;

enum SettingsAdderState implements TokenState {
    METADATA("metadata"),
    IMPORT("import");

    private final String name;

    SettingsAdderState(final String name) {
        this.name = name;
    }

    @Override
    public String getNewObjectTypeName() {
        return name;
    }
}