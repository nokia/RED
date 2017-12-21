/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteInTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteInKeywordTableHandler.E4DeleteInKeywordTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class DeleteInKeywordTableHandler extends DIParameterizedHandler<E4DeleteInKeywordTableHandler> {

    public DeleteInKeywordTableHandler() {
        super(E4DeleteInKeywordTableHandler.class);
    }

    public static class E4DeleteInKeywordTableHandler extends E4DeleteInTableHandler {

        @Override
        protected EditorCommand getCommandForSelectedElement(final RobotElement selectedElement, final int columnIndex,
                final int tableColumnCount) {
            return new KeywordsTableValuesChangingCommandsCollector()
                    .collectForRemoval(selectedElement, columnIndex, tableColumnCount)
                    .orElse(null);
        }
    }
}
