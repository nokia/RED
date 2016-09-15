/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.viewers.Selections;

public abstract class E4CopyCodeHoldersHandler {

    protected final boolean copyCodeHolders(final IStructuredSelection selection, final RedClipboard clipboard) {

        final RobotCodeHoldingElement<?>[] codeHolders = Selections.getElementsArray(selection, getCodeHolderClass());
        final RobotKeywordCall[] calls = Selections.getElementsArray(selection, RobotKeywordCall.class);
        if (codeHolders.length > 0) {
            final Object data = ArraysSerializerDeserializer.copy(getCodeHolderClass(), codeHolders);
            clipboard.insertContent(data);
            return true;

        } else if (calls.length > 0) {
            final Object data = ArraysSerializerDeserializer.copy(RobotKeywordCall.class, calls);
            clipboard.insertContent(data);
            return true;
        }
        return false;
    }

    protected abstract Class<? extends RobotCodeHoldingElement<?>> getCodeHolderClass();
}
