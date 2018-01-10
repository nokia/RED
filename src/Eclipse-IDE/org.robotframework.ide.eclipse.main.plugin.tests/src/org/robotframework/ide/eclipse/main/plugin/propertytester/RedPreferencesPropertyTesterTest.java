package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.junit.PreferenceUpdater;

public class RedPreferencesPropertyTesterTest {

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private final RedPreferencesPropertyTester tester = new RedPreferencesPropertyTester();

    @Test
    public void trueIsReturned_whenValidationIsTurnedOff() {
        preferenceUpdater.setValue(RedPreferences.TURN_OFF_VALIDATION, true);

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
