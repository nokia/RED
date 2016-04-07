/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteClassPathHandler.E4DeleteClassPathHandler;

public class DeleteClassPathHandlerTest {

    @Test
    public void whenSomePathsShouldBeRemoved_theyAreAndEventBrokerNotifiesAboutIt() {
        final E4DeleteClassPathHandler handler = new E4DeleteClassPathHandler();
        
        final SearchPath path1 = SearchPath.create("path1");
        final SearchPath path2 = SearchPath.create("path2");
        final SearchPath path3 = SearchPath.create("path3");
        final SearchPath path4 = SearchPath.create("path4");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addClassPath(path1);
        config.addClassPath(path2);
        config.addClassPath(path3);
        config.addClassPath(path4);

        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getProjectConfiguration()).thenReturn(config);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedPaths = new StructuredSelection(newArrayList(path2, path4));
        handler.deleteSearchPaths(selectedPaths, input, eventBroker);

        verify(eventBroker, times(1)).send(RobotProjectConfigEvents.ROBOT_CONFIG_CLASSPATH_STRUCTURE_CHANGED,
                config.getClassPath());
        assertThat(config.getClassPath()).containsExactly(SearchPath.create("path1"), SearchPath.create("path3"));
    }

    @Test
    public void whenNoPathWasRemoved_eventBrokerDoesNotNotify() {
        final E4DeleteClassPathHandler handler = new E4DeleteClassPathHandler();

        final SearchPath path1 = SearchPath.create("path1");
        final SearchPath path2 = SearchPath.create("path2");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addClassPath(path1);
        config.addClassPath(path2);

        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getProjectConfiguration()).thenReturn(config);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedPaths = new StructuredSelection(
                newArrayList(SearchPath.create("path3"), SearchPath.create("path4")));
        handler.deleteSearchPaths(selectedPaths, input, eventBroker);

        verifyZeroInteractions(eventBroker);
        assertThat(config.getClassPath()).containsExactly(SearchPath.create("path1"), SearchPath.create("path2"));
    }

}
