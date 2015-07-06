package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IEditorPart;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordDefinitionsTransfer;

import com.google.common.base.Preconditions;

public class RobotEditorPropertyTester extends PropertyTester {

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RobotFormEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RobotFormEditor.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((RobotFormEditor) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return testProperty((RobotFormEditor) receiver, property, (String) expectedValue);
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final boolean expectedValue) {
        if (property.equals("editorModelIsEditable")) {
            return editor.provideSuiteModel().isEditable() == expectedValue;
        } else if (property.equals("activeSectionEditorHasSection")) {
            final IEditorPart activeEditor = editor.getActiveEditor();
            final ISectionEditorPart activePage = activeEditor instanceof ISectionEditorPart ? (ISectionEditorPart) activeEditor
                    : null;
            if (activePage != null) {
                return activePage.provideSection(editor.provideSuiteModel()).isPresent() == expectedValue;
            } else {
                return !expectedValue;
            }
        } else if (property.equals("thereAreKeywordDefinitionElementsInClipboard")) {
            final Clipboard clipboard = editor.getClipboard();
            return KeywordDefinitionsTransfer.hasKeywordDefinitions(clipboard) == expectedValue;
        } else if (property.equals("thereAreKeywordCallElementsInClipboard")) {
            final Clipboard clipboard = editor.getClipboard();
            return KeywordCallsTransfer.hasKeywordCalls(clipboard) == expectedValue;
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
