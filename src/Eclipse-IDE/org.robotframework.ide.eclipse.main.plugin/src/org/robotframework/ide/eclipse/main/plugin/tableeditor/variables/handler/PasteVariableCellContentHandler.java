/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4PasteCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteVariableCellContentHandler.E4PasteVariableCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class PasteVariableCellContentHandler extends DIParameterizedHandler<E4PasteVariableCellContentHandler> {

    public PasteVariableCellContentHandler() {
        super(E4PasteVariableCellContentHandler.class);
    }

    public static class E4PasteVariableCellContentHandler extends E4PasteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns, final String newAttribute) {
            return new VariablesAttributesCommandsProvider().provide(element, index, newAttribute);
        }
    }
}
