/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

public class RobotTimeFormat {

    public static boolean isTimeAsNumber(final String expression) {
        try {
            Integer.parseInt(expression);
            return true;
        } catch (final NumberFormatException e ) {
            // ok then we will try to parse it to double
        }
        try {
            Double.parseDouble(expression);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

}
