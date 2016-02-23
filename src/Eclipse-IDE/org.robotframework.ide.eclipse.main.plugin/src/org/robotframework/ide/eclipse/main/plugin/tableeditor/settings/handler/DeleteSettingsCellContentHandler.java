/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteSettingsCellContentHandler.E4DeleteSettingsCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class DeleteSettingsCellContentHandler extends DIParameterizedHandler<E4DeleteSettingsCellContentHandler> {

    public DeleteSettingsCellContentHandler() {
        super(E4DeleteSettingsCellContentHandler.class);
    }

    public static class E4DeleteSettingsCellContentHandler extends E4DeleteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns) {
            return new SettingsAttributesCommandsProvider().provide(element, index, noOfColumns, "");
        }
    }
}
