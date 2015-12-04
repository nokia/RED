/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.swt;

import org.eclipse.swt.widgets.Display;

/**
 * @author Michal Anglart
 *
 */
public class SwtThread {

    public static <T> T syncExec(final Calculation<T> calculation) {
        return syncExec(Display.getDefault(), calculation);
    }

    public static <T> T syncExec(final Display display, final Calculation<T> calculation) {
        display.syncExec(calculation);
        return calculation.result;
    }

    public abstract static class Calculation<T> implements Runnable {

        private T result = null;

        T getResult() {
            return result;
        }

        @Override
        public final void run() {
            result = runCalculation();
        }

        public abstract T runCalculation();
    }
}
