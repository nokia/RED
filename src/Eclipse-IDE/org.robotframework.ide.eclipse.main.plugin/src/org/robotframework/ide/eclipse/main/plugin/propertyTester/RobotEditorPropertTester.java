package org.robotframework.ide.eclipse.main.plugin.propertyTester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IEditorPart;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPage;

import com.google.common.base.Preconditions;

public class RobotEditorPropertTester extends PropertyTester {

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RobotFormEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RobotFormEditor.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((RobotFormEditor) receiver, property, (Boolean) expectedValue);
        }
        return testProperty((RobotFormEditor) receiver, property, (String) expectedValue);
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final Boolean expectedValue) {
        if (property.equals("editorModelIsEditable")) {
            return editor.provideSuiteModel().isEditable() == expectedValue.booleanValue();
        } else if (property.equals("activeSectionEditorHasSection")) {
            final IEditorPart activeEditor = editor.getActiveEditor();
            final SectionEditorPage activePage = activeEditor instanceof SectionEditorPage ? (SectionEditorPage) activeEditor
                    : null;
            if (activePage != null) {
                return activePage.provideSection(editor.provideSuiteModel()).isPresent() == expectedValue;
            } else {
                return !expectedValue.booleanValue();
            }
        }
        return false;
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final String expectedValue) {
        if (property.equals("editorModelHasSection")) {
            for (final RobotElement section : editor.provideSuiteModel().getChildren()) {
                if (section.getName().equals(expectedValue)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

}
