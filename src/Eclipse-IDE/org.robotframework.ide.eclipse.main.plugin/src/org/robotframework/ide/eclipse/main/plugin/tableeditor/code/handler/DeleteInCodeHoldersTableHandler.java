/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteInCodeHoldersTableHandler.E4DeleteInCodeHoldersTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteInTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class DeleteInCodeHoldersTableHandler extends DIParameterizedHandler<E4DeleteInCodeHoldersTableHandler> {

    public DeleteInCodeHoldersTableHandler() {
        super(E4DeleteInCodeHoldersTableHandler.class);
    }

    public static class E4DeleteInCodeHoldersTableHandler extends E4DeleteInTableHandler {

        @Override
        protected EditorCommand getCommandForSelectedElement(final RobotElement selectedElement, final int columnIndex,
                final int tableColumnCount) {
            return new CodeTableValuesChangingCommandsCollector()
                    .collectForRemoval(selectedElement, columnIndex, tableColumnCount)
                    .orElse(null);
        }
    }
}
