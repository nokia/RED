/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.menus.TextState;
import org.eclipse.ui.commands.ICommandService;
import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.SwitchShellModeHandler.E4SwitchShellModeHandler;

public class SwitchShellModeHandlerTest {

    @Test
    public void handlerSwitchesModeInViewAndStoresNewStateInProvidedCmdStateObject_whenModeIsNull() {
        final DebugShellView view = mock(DebugShellView.class);
        when(view.switchToMode(null)).thenReturn(ExpressionType.PYTHON);

        final DebugShellViewWrapper viewWrapper = mock(DebugShellViewWrapper.class);
        when(viewWrapper.getView()).thenReturn(view);

        final ICommandService cmdService = mock(ICommandService.class);

        final PersistentState cmdState = new TextState();
        cmdState.setValue(ExpressionType.ROBOT.name());

        final String mode = null;
        new E4SwitchShellModeHandler().switchMode(cmdService, viewWrapper, mode, cmdState);

        assertThat(cmdState.getValue()).isEqualTo("PYTHON");

        verify(view).switchToMode(null);
        verify(cmdService).refreshElements(SwitchShellModeHandler.COMMAND_ID, null);
    }

    @Test
    public void handlerSwitchesModeInViewAndStoresNewStateInProvidedCmdStateObject_whenModeIsGiven() {
        final DebugShellView view = mock(DebugShellView.class);
        when(view.switchToMode(ExpressionType.VARIABLE)).thenReturn(ExpressionType.VARIABLE);

        final DebugShellViewWrapper viewWrapper = mock(DebugShellViewWrapper.class);
        when(viewWrapper.getView()).thenReturn(view);

        final ICommandService cmdService = mock(ICommandService.class);

        final PersistentState cmdState = new TextState();
        cmdState.setValue(ExpressionType.ROBOT.name());

        final String mode = ExpressionType.VARIABLE.name();
        new E4SwitchShellModeHandler().switchMode(cmdService, viewWrapper, mode, cmdState);

        assertThat(cmdState.getValue()).isEqualTo("VARIABLE");

        verify(view).switchToMode(ExpressionType.VARIABLE);
        verify(cmdService).refreshElements(SwitchShellModeHandler.COMMAND_ID, null);
    }
}
