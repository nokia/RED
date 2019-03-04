/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteRemoteLocationHandler.E4DeleteRemoteLocationHandler;

public class DeleteRemoteLocationHandlerTest {

    @Test
    public void whenSomeRemoteLocationsShouldBeRemoved_theyAreAndEventBrokerNotifiesAboutIt() {
        final E4DeleteRemoteLocationHandler handler = new E4DeleteRemoteLocationHandler();

        final RemoteLocation location1 = RemoteLocation.create("http://127.0.0.1:8270/");
        final RemoteLocation location2 = RemoteLocation.create("http://127.0.0.1:8271/");
        final RemoteLocation location3 = RemoteLocation.create("http://127.0.0.2:8270/");
        final RemoteLocation location4 = RemoteLocation.create("http://127.0.0.2:8271/");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addRemoteLocation(location1);
        config.addRemoteLocation(location2);
        config.addRemoteLocation(location3);
        config.addRemoteLocation(location4);

        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);
        when(input.getProjectConfiguration()).thenReturn(config);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedLocations = new StructuredSelection(newArrayList(location2, location4));
        handler.deleteRemoteLocations(selectedLocations, input, eventBroker);

        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED),
                argThat(hasCorrectEventData(file, newArrayList(location2, location4))));
        assertThat(config.getRemoteLocations()).containsExactly(location1, location3);
    }

    @Test
    public void whenNoRemoteLocationWasRemoved_eventBrokerDoesNotNotify() {
        final E4DeleteRemoteLocationHandler handler = new E4DeleteRemoteLocationHandler();

        final RemoteLocation location1 = RemoteLocation.create("http://127.0.0.1:8270/");
        final RemoteLocation location2 = RemoteLocation.create("http://127.0.0.1:8271/");
        final RemoteLocation location3 = RemoteLocation.create("http://127.0.0.2:8270/");
        final RemoteLocation location4 = RemoteLocation.create("http://127.0.0.2:8271/");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addRemoteLocation(location1);
        config.addRemoteLocation(location2);

        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getProjectConfiguration()).thenReturn(config);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedPaths = new StructuredSelection(newArrayList(location3, location4));
        handler.deleteRemoteLocations(selectedPaths, input, eventBroker);

        verifyZeroInteractions(eventBroker);
        assertThat(config.getRemoteLocations()).containsExactly(location1, location2);
    }

    private static ArgumentMatcher<Object> hasCorrectEventData(final IFile file, final List<RemoteLocation> locations) {
        return object -> object instanceof RedProjectConfigEventData<?>
                && file.equals(((RedProjectConfigEventData<?>) object).getUnderlyingFile())
                && locations.equals(((RedProjectConfigEventData<?>) object).getChangedElement());
    }
}
