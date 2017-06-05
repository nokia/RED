/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.FindElementUsagesHandler.E4FindUsagesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.FindUsagesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class FindElementUsagesHandler extends DIParameterizedHandler<E4FindUsagesHandler> {

    public FindElementUsagesHandler() {
        super(E4FindUsagesHandler.class);
    }

    public static class E4FindUsagesHandler {

        @Execute
        public void findUsages(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named("org.robotframework.ide.eclipse.findElementUsages.place") final String place) {

            final RobotFileInternalElement element = Selections.getSingleElement(selection,
                    RobotFileInternalElement.class);
            final String name = element.getName();

            FindUsagesHandler.findElements(place, editor, name);
        }

    }
}
