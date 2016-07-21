/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.CutCodeCellContentHandler.E4CutCodeCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4CutCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class CutCodeCellContentHandler extends DIParameterizedHandler<E4CutCodeCellContentHandler> {

    public CutCodeCellContentHandler() {
        super(E4CutCodeCellContentHandler.class);
    }

    public static class E4CutCodeCellContentHandler extends E4CutCellContentHandler {
    }
}
