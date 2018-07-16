/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.validation;

import java.util.regex.Pattern;

/**
 * @author Michal Anglart
 *
 */
public class RobotTimeFormat {

    public static boolean isValidRobotTimeArgument(final String argument) {
        return isPlainNumber(argument) || isTimeString(argument) || isTimerString(argument);
    }

    private static boolean isPlainNumber(final String argument) {
        try {
            Long.parseLong(argument);
            return true;
        } catch (final NumberFormatException e1) {
            try {
                Double.parseDouble(argument);
                return true;
            } catch (final NumberFormatException e2) {
                return false;
            }
        }
    }

    private static boolean isTimeString(final String argument) {
        return Pattern.matches("^\\s*" + daySpecifier() + hoursSpecifier() + minutesSpecifier() + secondsSpecifier()
                + millisSpecifier() + "$", argument);
    }

    private static String daySpecifier() {
        return "(-?\\d+(\\.\\d*)?\\s*(days|day|d)\\s*)?";
    }

    private static String hoursSpecifier() {
        return "(-?\\d+(\\.\\d*)?\\s*(hours|hour|h)\\s*)?";
    }

    private static String minutesSpecifier() {
        return "(-?\\d+(\\.\\d*)?\\s*(minutes|minute|mins|min|m)\\s*)?";
    }

    private static String secondsSpecifier() {
        return "(-?\\d+(\\.\\d*)?\\s*(seconds|second|sec|s)\\s*)?";
    }

    private static String millisSpecifier() {
        return "(-?\\d+(\\.\\d*)?\\s*(milliseconds|millisecond|millis|ms)\\s*)?";
    }

    private static boolean isTimerString(final String argument) {
        return Pattern.matches("^-?(\\d+:)?\\d+:\\d+(\\.\\d+)?$", argument);
    }
}
