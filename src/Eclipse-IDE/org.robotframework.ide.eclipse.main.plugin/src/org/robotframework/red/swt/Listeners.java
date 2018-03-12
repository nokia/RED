/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.swt;

import java.util.function.Consumer;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;

public class Listeners {

    public static FocusListener focusGainedAdapter(final Consumer<FocusEvent> consumer) {
        return new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static FocusListener focusLostAdapter(final Consumer<FocusEvent> consumer) {
        return new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static KeyListener keyPressedAdapter(final Consumer<KeyEvent> consumer) {
        return new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static KeyListener keyReleasedAdapter(final Consumer<KeyEvent> consumer) {
        return new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static MouseListener mouseUpAdapter(final Consumer<MouseEvent> consumer) {
        return new MouseAdapter() {

            @Override
            public void mouseUp(final MouseEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static MouseListener mouseDownAdapter(final Consumer<MouseEvent> consumer) {
        return new MouseAdapter() {

            @Override
            public void mouseDown(final MouseEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static MouseListener mouseDoubleClickAdapter(final Consumer<MouseEvent> consumer) {
        return new MouseAdapter() {

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static SelectionListener widgetSelectedAdapter(final Consumer<SelectionEvent> consumer) {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static SelectionListener widgetDefaultSelectedAdapter(final Consumer<SelectionEvent> consumer) {
        return new SelectionAdapter() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static MenuListener menuHiddenAdapter(final Consumer<MenuEvent> consumer) {
        return new MenuAdapter() {

            @Override
            public void menuHidden(final MenuEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static MenuListener menuShownAdapter(final Consumer<MenuEvent> consumer) {
        return new MenuAdapter() {

            @Override
            public void menuShown(final MenuEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static ShellListener shellClosedAdapter(final Consumer<ShellEvent> consumer) {
        return new ShellAdapter() {

            @Override
            public void shellClosed(final ShellEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static IHyperlinkListener linkEnteredAdapter(final Consumer<HyperlinkEvent> consumer) {
        return new HyperlinkAdapter() {

            @Override
            public void linkEntered(final HyperlinkEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static IHyperlinkListener linkActivatedAdapter(final Consumer<HyperlinkEvent> consumer) {
        return new HyperlinkAdapter() {

            @Override
            public void linkActivated(final HyperlinkEvent e) {
                consumer.accept(e);
            }
        };
    }

    public static IHyperlinkListener linkExitedAdapter(final Consumer<HyperlinkEvent> consumer) {
        return new HyperlinkAdapter() {

            @Override
            public void linkExited(final HyperlinkEvent e) {
                consumer.accept(e);
            }
        };
    }
}
