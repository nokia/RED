/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
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

            int row = 0, column = 0;

            final SelectionLayerAccessor accessor = editor.getSelectionLayerAccessor();
            final IDataProvider dataProvider = accessor.getDataProvider();
            final PositionCoordinate[] positions = accessor.getSelectedPositions();

            if (positions.length == 1) {
                row = positions[0].getRowPosition();
                column = positions[0].getColumnPosition();
                FindUsagesHandler.findElements(place, editor, (String) dataProvider.getDataValue(column, row));
            } else {
                final String name = element.getName();
                FindUsagesHandler.findElements(place, editor, name);
            }

        }

    }
}
