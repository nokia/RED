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
        final boolean testResult = tester.test(SearchPath.create(""), RedXmlSearchPathsPropertyTester.IS_SYSTEM_PATH,
                null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void testIsSystemPathProperty() {
        assertThat(isSystemPath(SearchPath.create("", true), false)).isFalse();
        assertThat(isSystemPath(SearchPath.create("", true), true)).isTrue();
        assertThat(isSystemPath(SearchPath.create(""), true)).isFalse();
        assertThat(isSystemPath(SearchPath.create(""), false)).isTrue();
    }

    private boolean isSystemPath(final SearchPath path, final boolean expected) {
        return tester.test(path, RedXmlSearchPathsPropertyTester.IS_SYSTEM_PATH, null, expected);
    }
}
