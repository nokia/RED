/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TreeLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ExpandAllHandler.E4ExpandAllHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class ExpandAllHandler extends DIParameterizedHandler<E4ExpandAllHandler> {

    public ExpandAllHandler() {
        super(E4ExpandAllHandler.class);
    }

    public static class E4ExpandAllHandler {

        @Execute
        public void expandAll(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            final Optional<TreeLayerAccessor> treeLayerAccessor = editor.getTreeLayerAccessor();

            if (treeLayerAccessor.isPresent()) {
                editor.getSelectionLayerAccessor()
                        .preserveElementSelectionWhen(() -> treeLayerAccessor.get().expandAll());
            }
        }
    }
}
