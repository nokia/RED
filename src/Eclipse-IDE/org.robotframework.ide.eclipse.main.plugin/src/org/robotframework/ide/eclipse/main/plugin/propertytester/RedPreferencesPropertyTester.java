package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class RedPreferencesPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (expectedValue instanceof Boolean) {
            return testProperty(property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private static boolean testProperty(final String property, final boolean expected) {
        if ("isValidationTurnedOff".equals(property)) {
            return RedPlugin.getDefault().getPreferences().isValidationTurnedOff() == expected;
        }
        return false;
    }
}
