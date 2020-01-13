/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RedActivationStrategy;

public class CellsActivationStrategyTest {

    @Test
    public void whenAnyPrintableCharIsTypedIn_editorShouldBeActivatedAndKeyEventCannotBePropagatedFurther() {
        final RedActivationStrategy activationSupport = new RedActivationStrategy(null);

        for (char character = 0; character < 256; character++) {
            if (' ' <= character && character <= '~') {
                final KeyEvent keyEvent = createKeyEvent(character);
                final ColumnViewerEditorActivationEvent event = createEditorActivationEvent(keyEvent);

                assertThat(activationSupport.isEditorActivationEvent(event)).isTrue();
                assertThat(keyEvent.doit).isFalse();
            }
        }
    }

    @Test
    public void whenCRIsTypedIn_editorShouldBeActivatedAndKeyEventCannotBePropagatedFurther() {
        final RedActivationStrategy activationSupport = new RedActivationStrategy(null);

        final KeyEvent keyEvent = createKeyEvent('\r');
        final ColumnViewerEditorActivationEvent event = createEditorActivationEvent(keyEvent);

        assertThat(activationSupport.isEditorActivationEvent(event)).isTrue();
        assertThat(keyEvent.doit).isFalse();
    }

    @Test
    public void whenNonPrintableCharOrNonCRIsTypedIn_editorShouldNotActivateAndKeyEventIsPropagated() {
        final RedActivationStrategy activationSupport = new RedActivationStrategy(null);

        for (char character = 0; character < 256; character++) {
            if (character != '\r' && (' ' > character || character > '~')) {
                final KeyEvent keyEvent = createKeyEvent(character);
                final ColumnViewerEditorActivationEvent event = createEditorActivationEvent(keyEvent);

                assertThat(activationSupport.isEditorActivationEvent(event)).isFalse();
                assertThat(keyEvent.doit).isTrue();
            }
        }
    }

    private static ColumnViewerEditorActivationEvent createEditorActivationEvent(final KeyEvent keyEvent) {
        return new ColumnViewerEditorActivationEvent(mock(ViewerCell.class), keyEvent);
    }

    private static KeyEvent createKeyEvent(final char character) {
        final Event event = new Event();
        event.widget = mock(Widget.class);
        final KeyEvent keyEvent = new KeyEvent(event);
        keyEvent.character = character;
        keyEvent.doit = true;
        return keyEvent;
    }
}
