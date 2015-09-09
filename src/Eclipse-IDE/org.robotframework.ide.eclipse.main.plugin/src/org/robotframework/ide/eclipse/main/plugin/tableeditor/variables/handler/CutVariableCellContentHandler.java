/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4CutCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CutVariableCellContentHandler.E4CutVariableCellContentHandler;

import com.google.common.base.Optional;

public class CutVariableCellContentHandler extends DIHandler<E4CutVariableCellContentHandler> {

    public CutVariableCellContentHandler() {
        super(E4CutVariableCellContentHandler.class);
    }

    public static class E4CutVariableCellContentHandler extends E4CutCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns) {
            return new VariablesAttributesCommandsProvider().provide(element, index, "");
        }
    }
}
