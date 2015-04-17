package org.robotframework.ide.eclipse.main.plugin.propertyTester;

import org.eclipse.core.expressions.PropertyTester;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

import com.google.common.base.Preconditions;

public class RobotEditorPropertTester extends PropertyTester {

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RobotFormEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RobotFormEditor.class.getName());

        return testProperty((RobotFormEditor) receiver, property, (Boolean) expectedValue);
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final Boolean expectedValue) {
        if (property.equals("editorModelIsEditable")) {
            return editor.provideSuiteModel().isEditable() == expectedValue.booleanValue();
        }
        return false;
    }

}
