/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.console;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.junit.Test;

public class RedSessionConsoleParticipantTest {

    @Test
    public void participantInitializationCreatesAndAddsAllActions() {
        final RedSessionConsole console = new RedSessionConsole("console", mock(Process.class));
        console.initializeStreams();

        final IToolBarManager toolbarManager = mock(IToolBarManager.class);

        final RedSessionConsoleParticipant participant = new RedSessionConsoleParticipant();
        participant.init(createPage(toolbarManager), console);

        verify(toolbarManager).appendToGroup(anyString(), any(TerminateRedSessionAction.class));
        verify(toolbarManager).appendToGroup(anyString(), any(RemoveTerminatedRedSessionAction.class));
        verify(toolbarManager).appendToGroup(anyString(), any(RemoveAllTerminatedRedSessionsAction.class));
        verify(toolbarManager).appendToGroup(anyString(), any(SaveRedSessionAction.class));
        verify(toolbarManager, times(2)).appendToGroup(anyString(), any(ActivateOnInputChangeAction.class));
        participant.dispose();
    }

    private static IPageBookViewPage createPage(final IToolBarManager toolbarManager) {
        final IPageBookViewPage page = mock(IPageBookViewPage.class);
        final IPageSite site = mock(IPageSite.class);
        final IActionBars actionBars = mock(IActionBars.class);

        when(page.getSite()).thenReturn(site);
        when(site.getActionBars()).thenReturn(actionBars);
        when(actionBars.getToolBarManager()).thenReturn(toolbarManager);
        return page;
    }

}
