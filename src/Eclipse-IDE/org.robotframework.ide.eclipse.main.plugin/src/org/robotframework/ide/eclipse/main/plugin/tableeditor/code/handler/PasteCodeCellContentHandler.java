/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.PasteCodeCellContentHandler.E4PasteCodeCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4PasteCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class PasteCodeCellContentHandler extends DIParameterizedHandler<E4PasteCodeCellContentHandler> {

    public PasteCodeCellContentHandler() {
        super(E4PasteCodeCellContentHandler.class);
    }

    public static class E4PasteCodeCellContentHandler extends E4PasteCellContentHandler {
    }
}
