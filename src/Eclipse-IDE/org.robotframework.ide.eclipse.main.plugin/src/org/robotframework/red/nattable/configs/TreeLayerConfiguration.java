/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;


public class TreeLayerConfiguration extends DefaultTreeLayerConfiguration {

    public TreeLayerConfiguration(final TreeLayer treeLayer) {
        super(treeLayer);
    }

    @Override
    public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
        final TreeExpandCollapseAction treeExpandCollapseAction = new TreeExpandCollapseAction();
        final CellPainterMouseEventMatcher treeImagePainterMouseEventMatcher = new CellPainterMouseEventMatcher(
                GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, TreeImagePainter.class);

        uiBindingRegistry.registerFirstMouseDownBinding(treeImagePainterMouseEventMatcher, treeExpandCollapseAction);
    }
}
