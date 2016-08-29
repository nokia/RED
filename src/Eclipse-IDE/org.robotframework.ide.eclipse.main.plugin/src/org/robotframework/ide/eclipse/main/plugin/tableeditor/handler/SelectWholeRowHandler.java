/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.SelectWholeRowHandler.E4SelectWholeRowHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class SelectWholeRowHandler extends DIParameterizedHandler<E4SelectWholeRowHandler> {

    public SelectWholeRowHandler() {
        super(E4SelectWholeRowHandler.class);
    }

    public static class E4SelectWholeRowHandler {

        @Execute
        public void selectWholeRows(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            selectionLayerAccessor.expandSelectionToWholeRows();
        }
    }
}
