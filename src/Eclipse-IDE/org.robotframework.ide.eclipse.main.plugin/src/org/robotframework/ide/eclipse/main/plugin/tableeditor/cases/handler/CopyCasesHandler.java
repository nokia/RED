/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyCasesHandler.E4CopyCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyCasesHandler extends DIParameterizedHandler<E4CopyCasesHandler> {

    public CopyCasesHandler() {
        super(E4CopyCasesHandler.class);
    }

    public static class E4CopyCasesHandler {

        @Execute
        public boolean copyCases(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            final RobotCase[] cases = Selections.getElementsArray(selection, RobotCase.class);
            final RobotKeywordCall[] calls = Selections.getElementsArray(selection, RobotKeywordCall.class);
            if (cases.length > 0) {
                final Object data = ArraysSerializerDeserializer.copy(RobotCase.class, cases);
                clipboard.insertContent(data);
                return true;

            } else if (calls.length > 0) {
                final Object data = ArraysSerializerDeserializer.copy(RobotKeywordCall.class, calls);
                clipboard.insertContent(data);
                return true;
            }
            return false;
        }
    }
}
