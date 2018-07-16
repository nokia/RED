/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.validation.RobotTimeFormat;

public class RobotTimeFormatTest {

    @Test
    public void timeIsValid_whenConsistsOnlyOfDigits() {
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("0")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-1")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("10")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("123456789")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-123")).isTrue();
    }

    @Test
    public void timeIsValid_whenConsistsOfFloatingPointNumber() {
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1.")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-42.123")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("3.1415")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("0.0")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("0.9")).isTrue();
    }

    @Test
    public void timeIsValid_whenGivenInRobotTimeFormat() {
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1. day")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-1.2 d")).isTrue();

        assertThat(RobotTimeFormat.isValidRobotTimeArgument("42 hours")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1. hour")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-1.2 h")).isTrue();

        assertThat(RobotTimeFormat.isValidRobotTimeArgument("42 minutes")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1. minute")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("42.1 mins")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1.2 min")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-123 m")).isTrue();

        assertThat(RobotTimeFormat.isValidRobotTimeArgument("42 seconds")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1. second")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1.2 sec")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-123 s")).isTrue();

        assertThat(RobotTimeFormat.isValidRobotTimeArgument("42 milliseconds")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1. millisecond")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("1.2 millis")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-123 ms")).isTrue();

        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days 4 hours 1 minute 15 sec 123 ms")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("4 hours 1 minute 15 sec 123 ms")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days 1 minute 15 sec 123 ms")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days 4 hours 15 sec 123 ms")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days 4 hours 1 minute 123 ms")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days 4 hours 1 minute 15 sec")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-5 days -4 hours -1 minute -15 sec -123 ms")).isTrue();
    }

    @Test
    public void timeIsValid_whenGivenInTimerFormat() {
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("10:12")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("12:13")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("0:12:13")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("123:12:13")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("123:12:13.999")).isTrue();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-123:12:13.999")).isTrue();
    }

    @Test
    public void timeIsInvalid_whenGivenInWrongFormat() {
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("abc")).isFalse();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("5 days 1 minute 4 hours 15 sec 123 ms")).isFalse();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("15 sec 4 hours 123 ms")).isFalse();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("12:")).isFalse();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("-12:-5")).isFalse();
        assertThat(RobotTimeFormat.isValidRobotTimeArgument("12:13:14:15")).isFalse();
    }
}
