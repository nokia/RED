/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;

public class RobotConsoleFacadeTest {

    private Supplier<IConsole[]> consolesSupplier;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void beforeTest() throws Exception {
        consolesSupplier = mock(Supplier.class);
    }

    @Test
    public void exceptionIsThrown_whenRobotLaunchConfigurationCannotBeCreated() throws Exception {
        final ILaunchConfiguration config = createConfigMock("unknownId", "redType", "redName");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> RobotConsoleFacade.findConsole(config, "redDescription", consolesSupplier, 0))
                .withMessage("Unrecognized configuration type")
                .withNoCause();

        verifyNoInteractions(consolesSupplier);
    }

    @Test
    public void exceptionIsThrown_whenProvidedConsolesAreEmpty() throws Exception {
        when(consolesSupplier.get()).thenReturn(new IConsole[0]);

        final ILaunchConfiguration config = createConfigMock(RobotLaunchConfiguration.TYPE_ID, "redType", "redName");

        assertThatIllegalStateException()
                .isThrownBy(() -> RobotConsoleFacade.findConsole(config, "redDescription", consolesSupplier, 0))
                .withMessage(
                        "Unable to find output console. Trying to look for a console with name:\nredName [redType] redDescription")
                .withNoCause();

        verify(consolesSupplier).get();
        verifyNoMoreInteractions(consolesSupplier);
    }

    @Test
    public void exceptionIsThrown_whenProvidedConsolesDoNotContainIOConsole() throws Exception {
        final IConsole console = mock(IConsole.class);
        when(consolesSupplier.get()).thenReturn(new IConsole[] { console });

        final ILaunchConfiguration config = createConfigMock(RobotLaunchConfiguration.TYPE_ID, "redType", "redName");

        assertThatIllegalStateException()
                .isThrownBy(() -> RobotConsoleFacade.findConsole(config, "redDescription", consolesSupplier, 0))
                .withMessage(
                        "Unable to find output console. Trying to look for a console with name:\nredName [redType] redDescription")
                .withNoCause();

        verify(consolesSupplier).get();
        verifyNoMoreInteractions(consolesSupplier);
    }

    @Test
    public void exceptionIsThrown_whenProvidedConsolesDoNotContainIOConsoleWithMatchingName() throws Exception {
        final IOConsole console = mock(IOConsole.class);
        when(console.getName()).thenReturn("otherConsole");
        when(consolesSupplier.get()).thenReturn(new IConsole[] { console });

        final ILaunchConfiguration config = createConfigMock(RobotLaunchConfiguration.TYPE_ID, "redType", "redName");

        assertThatIllegalStateException()
                .isThrownBy(() -> RobotConsoleFacade.findConsole(config, "redDescription", consolesSupplier, 0))
                .withMessage(
                        "Unable to find output console. Trying to look for a console with name:\nredName [redType] redDescription")
                .withNoCause();

        verify(consolesSupplier).get();
        verifyNoMoreInteractions(consolesSupplier);
    }

    @Test
    public void firstMatchingIOConsoleIsFound_whenNameIsEqualToRequestedOne() throws Exception {
        final IOConsole console1 = mock(IOConsole.class);
        when(console1.getName()).thenReturn("otherConsole");
        final IOConsole console2 = mock(IOConsole.class);
        when(console2.getName()).thenReturn("redName [redType] redDescription");
        final IOConsole console3 = mock(IOConsole.class);
        when(consolesSupplier.get()).thenReturn(new IConsole[] { mock(IConsole.class), console1, mock(IConsole.class),
                console2, mock(IConsole.class), console3, mock(IConsole.class) });

        final ILaunchConfiguration config = createConfigMock(RobotLaunchConfiguration.TYPE_ID, "redType", "redName");

        assertThat(RobotConsoleFacade.findConsole(config, "redDescription", consolesSupplier, 0)).isSameAs(console2);

        verify(consolesSupplier).get();
        verifyNoMoreInteractions(consolesSupplier);
    }

    @Test
    public void firstMatchingIOConsoleIsFound_whenNameContainsRequestedOne() throws Exception {
        final IOConsole console1 = mock(IOConsole.class);
        when(console1.getName()).thenReturn("remoteName [remoteType] remoteDescription ABC");
        final IOConsole console2 = mock(IOConsole.class);
        when(consolesSupplier.get()).thenReturn(new IConsole[] { mock(IConsole.class), console1, mock(IConsole.class),
                console2, mock(IConsole.class), });

        final ILaunchConfiguration config = createConfigMock(RemoteRobotLaunchConfiguration.TYPE_ID, "remoteType",
                "remoteName");

        assertThat(RobotConsoleFacade.findConsole(config, "remoteDescription", consolesSupplier, 0)).isSameAs(console1);

        verify(consolesSupplier).get();
        verifyNoMoreInteractions(consolesSupplier);
    }

    @Test
    public void firstMatchingIOConsoleIsFound_whenConsolesAreProvidedAsynchronously() throws Exception {
        final IOConsole console1 = mock(IOConsole.class);
        when(console1.getName()).thenReturn("otherConsole");
        final IOConsole console2 = mock(IOConsole.class);
        when(console2.getName()).thenReturn("redName [redType] redDescription");
        when(consolesSupplier.get()).thenReturn(new IConsole[0], new IConsole[] { console1 },
                new IConsole[] { console2 });

        final ILaunchConfiguration config = createConfigMock(RobotLaunchConfiguration.TYPE_ID, "redType", "redName");

        assertThat(RobotConsoleFacade.findConsole(config, "redDescription", consolesSupplier, 300)).isSameAs(console2);

        verify(consolesSupplier, times(3)).get();
        verifyNoMoreInteractions(consolesSupplier);
    }

    private ILaunchConfiguration createConfigMock(final String typeId, final String typeName, final String configName)
            throws CoreException {
        final ILaunchConfigurationType type = mock(ILaunchConfigurationType.class);
        when(type.getIdentifier()).thenReturn(typeId);
        when(type.getName()).thenReturn(typeName);
        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getType()).thenReturn(type);
        when(config.getName()).thenReturn(configName);
        return config;
    }

}
