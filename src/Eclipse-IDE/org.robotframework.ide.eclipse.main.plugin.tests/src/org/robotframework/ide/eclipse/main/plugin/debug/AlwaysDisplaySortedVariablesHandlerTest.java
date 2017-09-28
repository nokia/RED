package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.commands.Command;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.UIElement;
import org.junit.After;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.AlwaysDisplaySortedVariablesHandler.E4AlwaysDisplayVariablesSortedHandler;

public class AlwaysDisplaySortedVariablesHandlerTest {

    @After
    public void afterTest() {
        final PersistentState state = provideSortingState();
        state.setValue(false);
        state.setShouldPersist(false);
    }

    @Test
    public void utilityMethodProperlyGetsTheState() {
        final PersistentState state = provideSortingState();

        state.setValue(true);
        assertThat(AlwaysDisplaySortedVariablesHandler.isSortingEnabled()).isTrue();
        assertThat(state.shouldPersist()).isTrue();

        state.setValue(false);
        assertThat(AlwaysDisplaySortedVariablesHandler.isSortingEnabled()).isFalse();
        assertThat(state.shouldPersist()).isTrue();
    }

    @Test
    public void uiElementIsSetToBeChecked_whenStateHasTrueValue() {
        final PersistentState state = provideSortingState();
        state.setValue(true);

        final UIElement element = mock(UIElement.class);

        final AlwaysDisplaySortedVariablesHandler handler = new AlwaysDisplaySortedVariablesHandler();
        handler.updateElement(element, null);

        assertThat(state.shouldPersist()).isTrue();
        verify(element).setChecked(true);
    }

    @Test
    public void uiElementIsSetToBeUnchecked_whenStateHasFalseValue() {
        final PersistentState state = provideSortingState();
        state.setValue(false);

        final UIElement element = mock(UIElement.class);

        final AlwaysDisplaySortedVariablesHandler handler = new AlwaysDisplaySortedVariablesHandler();
        handler.updateElement(element, null);

        assertThat(state.shouldPersist()).isTrue();
        verify(element).setChecked(false);
    }

    @Test
    public void handlerEnableSorting_whenSortingIsCurrentlyDisabled() {
        final E4AlwaysDisplayVariablesSortedHandler handler = new E4AlwaysDisplayVariablesSortedHandler();

        final Viewer viewer = mock(Viewer.class);

        final IDebugView view = mock(IDebugView.class);
        when(view.getViewer()).thenReturn(viewer);

        final PersistentState state = provideSortingState();
        state.setValue(false);
        assertThat(state.getValue()).isEqualTo(false);

        handler.displayVariablesSorted(state, view);
        assertThat(state.getValue()).isEqualTo(true);
        assertThat(state.shouldPersist()).isTrue();
        verify(viewer).refresh();
    }

    @Test
    public void handlerDisablesSorting_whenSortingIsCurrentlyEnabled() {
        final E4AlwaysDisplayVariablesSortedHandler handler = new E4AlwaysDisplayVariablesSortedHandler();

        final Viewer viewer = mock(Viewer.class);

        final IDebugView view = mock(IDebugView.class);
        when(view.getViewer()).thenReturn(viewer);

        final PersistentState state = provideSortingState();
        state.setValue(true);
        assertThat(state.getValue()).isEqualTo(true);

        handler.displayVariablesSorted(state, view);
        assertThat(state.getValue()).isEqualTo(false);
        assertThat(state.shouldPersist()).isTrue();
        verify(viewer).refresh();
    }

    private static PersistentState provideSortingState() {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        final Command command = commandService.getCommand(AlwaysDisplaySortedVariablesHandler.COMMAND_ID);
        return (PersistentState) command.getState(AlwaysDisplaySortedVariablesHandler.COMMAND_STATE_ID);
    }

}
