/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.CutCodeCellContentHandler.E4CutCodeCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4CutCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class CutCodeCellContentHandler extends DIParameterizedHandler<E4CutCodeCellContentHandler> {

    public CutCodeCellContentHandler() {
        super(E4CutCodeCellContentHandler.class);
    }

    public static class E4CutCodeCellContentHandler extends E4CutCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns) {
            return new CodeAttributesCommandsProvider().provideChangeAttributeCommand(element, index, noOfColumns, "");
        }
    }
}
