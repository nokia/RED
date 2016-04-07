/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;

public class RedXmlSearchPathsPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RedXmlSearchPathsPropertyTester tester = new RedXmlSearchPathsPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotASearchPath() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                "Property tester is unable to test properties of java.lang.Object. It should be used with "
                        + SearchPath.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(SearchPath.create(""), RedXmlSearchPathsPropertyTester.IS_SYSTEMPATH,
                null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturned_whenSearchPathIsSystemButItWasntExpected() {
        final boolean testResult = testIsSystemProperty(SearchPath.create("", true), false);

        assertThat(testResult).isFalse();
    }

    @Test
    public void trueIsReturned_whenSearchPathIsSystemAndItWasExpected() {
        final boolean testResult = testIsSystemProperty(SearchPath.create("", true), true);

        assertThat(testResult).isTrue();
    }

    @Test
    public void falseIsReturned_whenSearchPathIsNotSystemButItWasntExpected() {
        final boolean testResult = testIsSystemProperty(SearchPath.create(""), true);

        assertThat(testResult).isFalse();
    }

    @Test
    public void trueIsReturned_whenSearchPathIsNotSystemAndItWasExpected() {
        final boolean testResult = testIsSystemProperty(SearchPath.create(""), false);

        assertThat(testResult).isTrue();
    }

    private boolean testIsSystemProperty(final SearchPath path, final boolean expected) {
        return tester.test(path, RedXmlSearchPathsPropertyTester.IS_SYSTEMPATH, null, expected);
    }
}
