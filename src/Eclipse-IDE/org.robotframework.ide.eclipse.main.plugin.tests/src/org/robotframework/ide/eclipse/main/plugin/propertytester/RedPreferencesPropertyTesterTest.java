/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;

@ExtendWith(PreferencesExtension.class)
public class RedPreferencesPropertyTesterTest {

    private final RedPreferencesPropertyTester tester = new RedPreferencesPropertyTester();

    @BooleanPreference(key = RedPreferences.TURN_OFF_VALIDATION, value = true)
    @Test
    public void trueIsReturned_whenValidationIsTurnedOff() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        final boolean testResult = tester.test(editor, "isValidationTurnedOff", null, true);

        assertThat(testResult).isTrue();
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        final boolean testResult = tester.test(editor, "unknown_property", null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturned_forUnknownProperty() {
        final RobotFormEditor editor = mock(RobotFormEditor.class);
        assertThat(tester.test(editor, "unknown_property", null, true)).isFalse();
        assertThat(tester.test(editor, "unknown_property", null, false)).isFalse();
    }
}
