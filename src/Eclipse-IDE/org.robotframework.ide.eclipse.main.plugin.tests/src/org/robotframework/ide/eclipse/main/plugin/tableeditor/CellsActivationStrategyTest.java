/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RedActivationStrategy;


@RunWith(Theories.class)
public class CellsActivationStrategyTest {

    @DataPoints
    public static char[] characters() {
        final char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) i;
        }
        return chars;
    }

    @Theory
    public void whenAnyPrintableCharIsTypedIn_editorShouldBeActivatedAndKeyEventCannotBePropagatedFurther(
            final Character character) {
        assumeTrue(' ' <= character.charValue() && character.charValue() <= '~');

        final RedActivationStrategy activationSupport = new RedActivationStrategy(null);
        
        final KeyEvent keyEvent = createKeyEvent(character.charValue());
        final ColumnViewerEditorActivationEvent event = createEditorActivationEvent(keyEvent);

        assertThat(activationSupport.isEditorActivationEvent(event)).isTrue();
        assertThat(keyEvent.doit).isFalse();
    }

    @Test
    public void whenCRIsTypedIn_editorShouldBeActivatedAndKeyEventCannotBePropagatedFurther() {
        final RedActivationStrategy activationSupport = new RedActivationStrategy(null);

        final KeyEvent keyEvent = createKeyEvent('\r');
        final ColumnViewerEditorActivationEvent event = createEditorActivationEvent(keyEvent);

        assertThat(activationSupport.isEditorActivationEvent(event)).isTrue();
        assertThat(keyEvent.doit).isFalse();
    }

    @Theory
    public void whenNonPrintalbeCharOrNonCRIsTypedIn_editorShouldNotActivateAndKeyEventIsPropagated(
            final Character character) {
        assumeFalse(character == '\r' || ' ' <= character.charValue() && character.charValue() <= '~');

        final RedActivationStrategy activationSupport = new RedActivationStrategy(null);

        final KeyEvent keyEvent = createKeyEvent(character);
        final ColumnViewerEditorActivationEvent event = createEditorActivationEvent(keyEvent);

        assertThat(activationSupport.isEditorActivationEvent(event)).isFalse();
        assertThat(keyEvent.doit).isTrue();
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
