/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.CollapseAllHandler.E4CollapseAllHandler;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.viewers.TestViewer;
import org.robotframework.red.viewers.TestViewer.Node;

@SuppressWarnings("restriction")
@ExtendWith(FreshShellExtension.class)
public class CollapseAllHandlerTest {

    @FreshShell
    Shell shell;

    @Test
    public void viewerNodesAreCollapsed() {
        final Node tree = new Node(null, "1", new Node(null, "11"), new Node(null, "11"));
        
        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        final ExecutionView view = mock(ExecutionView.class);
        final TreeViewer viewer = TestViewer.create(shell);
        viewer.setInput(newArrayList(tree));

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getViewer()).thenReturn(viewer);

        viewer.getTree().getItems()[0].setExpanded(true);

        assertThat(viewer.getTree().getItems()[0].getExpanded()).isTrue();

        new E4CollapseAllHandler().collapseAll(viewWrapper);

        assertThat(viewer.getTree().getItems()[0].getExpanded()).isFalse();
    }
}
